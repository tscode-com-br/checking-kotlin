package br.com.tscode.checking.platform.background

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import br.com.tscode.checking.core.error.ApiError
import br.com.tscode.checking.core.result.AppResult
import br.com.tscode.checking.core.time.Clock
import br.com.tscode.checking.data.local.AppPreferencesDataSource
import br.com.tscode.checking.data.local.SecurePasswordStore
import br.com.tscode.checking.domain.checkrules.ScheduledPauseSettings
import br.com.tscode.checking.domain.checkrules.currentPauseStartInstant
import br.com.tscode.checking.domain.checkrules.isScheduledPauseActiveNow
import br.com.tscode.checking.domain.checkrules.nextPauseStartInstant
import br.com.tscode.checking.domain.checkrules.nextResumeInstant
import br.com.tscode.checking.domain.checkrules.resolveLastRecordedAction
import br.com.tscode.checking.domain.clientstate.resolvePersistedUserSettings
import br.com.tscode.checking.domain.clientstate.UserSettings
import br.com.tscode.checking.domain.model.HistoryState
import br.com.tscode.checking.domain.model.ActivitySeverity
import br.com.tscode.checking.domain.model.CheckAction
import br.com.tscode.checking.domain.model.LocationOptions
import br.com.tscode.checking.platform.activitylog.ActivityLogger
import br.com.tscode.checking.domain.model.UserProjects
import br.com.tscode.checking.domain.repository.AccidentRepository
import br.com.tscode.checking.domain.repository.AuthRepository
import br.com.tscode.checking.domain.repository.CheckRepository
import br.com.tscode.checking.domain.usecase.AutoActivitiesResult
import br.com.tscode.checking.domain.usecase.RunAutomaticActivitiesUseCase
import br.com.tscode.checking.i18n.resolveEffectiveLanguageCode
import br.com.tscode.checking.platform.activitylog.ApplicationScope
import br.com.tscode.checking.platform.background.diagnostics.EvaluationEntry
import br.com.tscode.checking.platform.background.diagnostics.EvaluationLog
import br.com.tscode.checking.platform.background.diagnostics.EvaluationOutcome
import br.com.tscode.checking.BuildConfig
import br.com.tscode.checking.platform.background.notifications.AutoActivityNotifications
import br.com.tscode.checking.platform.location.LocationCapture
import br.com.tscode.checking.platform.location.LocationMeasurementCollector
import br.com.tscode.checking.platform.location.LocationProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

enum class OrchestratorTrigger {
    TIMER,
    GEOFENCE,
    FOREGROUND,
    ACCURACY_RETRY,
    PAUSE_START,
    PAUSE_END,
    PAUSE_GRACE,
}

private enum class SkipDecision { RUN, SKIP, NO_FIX }

private sealed class ScheduledPauseGateDecision {
    data class Continue(val confirmedState: HistoryState?) : ScheduledPauseGateDecision()
    object AwaitingVerification : ScheduledPauseGateDecision()
    object Grace : ScheduledPauseGateDecision()
    object Active : ScheduledPauseGateDecision()
}

private enum class ScheduledPauseGraceArmResult { WAITING, DUE_NOW, WINDOW_EXPIRED }

// Fallback GPS accuracy gate used only when offline AND no options were ever cached (mirrors the UI's
// `?: 30`). Online, the server's real threshold is always used; offline the reading is queued and the
// server re-evaluates accuracy at replay, so a sane default is enough.
internal const val DEFAULT_ACCURACY_THRESHOLD_METERS = 30

internal fun shouldEvaluateTimerMovement(
    trigger: OrchestratorTrigger,
    hasActiveLowAccuracyEpisode: Boolean,
): Boolean = trigger == OrchestratorTrigger.TIMER && !hasActiveLowAccuracyEpisode

internal fun isEligibleMovementBaseline(
    accuracyMeters: Double,
    accuracyThresholdMeters: Int,
): Boolean = accuracyMeters.isFinite() && accuracyMeters <= accuracyThresholdMeters

// OFFLINE capture resilience (P8). The engine must NOT bail when it can't fetch LocationOptions offline:
// options are only needed for the GPS accuracy gate and (online) the mixed-zone decision — neither is
// reached offline, where the engine's only job is to capture the GPS fix and queue an offline Raw for
// every movement / geofence transition. On a real connectivity loss (ApiError.Network) fall back to the
// last known options (or defaults) and keep capturing; on anything else (Unauthorized → re-login,
// HTTP/unknown) return null so the run bails. Without this, `getLocationOptions() ?: return` bailed once
// the 15-min options cache expired, so only the transitions inside that first window were ever queued →
// only the first offline activity synced and the real check-out time was lost (recorded live, at reconnect).
internal fun offlineFallbackLocationOptions(
    cached: LocationOptions?,
    error: ApiError,
): LocationOptions? =
    if (error is ApiError.Network) {
        cached ?: LocationOptions(
            items = emptyList(),
            accuracyThresholdMeters = DEFAULT_ACCURACY_THRESHOLD_METERS,
            mixedZoneIntervalMinutes = 0,
        )
    } else {
        null
    }

// The 7-step background check engine (§23.4, T3B.3).
// Single-flight via a Mutex.  Acquires a wake lock for the duration of each burst.
// Called by the FGS 15-min timer, GeofenceBroadcastReceiver, and CheckViewModel foreground path.
@Singleton
class BackgroundCheckOrchestrator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appPrefs: AppPreferencesDataSource,
    private val checkRepository: CheckRepository,
    private val runAutomaticActivitiesUseCase: RunAutomaticActivitiesUseCase,
    private val locationProvider: LocationProvider,
    private val clock: Clock,
    private val authRepository: AuthRepository,
    private val securePasswordStore: SecurePasswordStore,
    private val accidentRepository: AccidentRepository,
    private val activityLogger: ActivityLogger,
    @ApplicationScope private val applicationScope: CoroutineScope,
) {
    private val mutex = Mutex()
    private val lowAccuracyRetryScheduler = LowAccuracyRetryScheduler(applicationScope)
    // Public cancellation can race a run already suspended inside the location use-case. The
    // generation invalidates that in-flight result so it cannot resurrect a retired episode.
    private val lowAccuracyGeneration = AtomicLong(0L)
    private val scheduledPauseGeneration = AtomicLong(0L)
    @Volatile private var scheduledPauseGraceJob: Job? = null
    private val settingsJson = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    private val wakeLock: PowerManager.WakeLock by lazy {
        (context.getSystemService(Context.POWER_SERVICE) as PowerManager)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "checking:auto_activity_burst")
            .apply { setReferenceCounted(false) }
    }

    // Skip-if-unchanged state (TIMER ticks only)
    @Volatile private var lastLat: Double? = null
    @Volatile private var lastLon: Double? = null


    // Remote-state cache (~45 s) — avoids redundant GET /check/state calls when stationary
    @Volatile private var cachedState: HistoryState? = null
    @Volatile private var cacheChave: String = ""
    @Volatile private var cachedStateAt: Instant = Instant.EPOCH

    // LocationOptions cache (~15 min) — accuracy threshold + mixed-zone interval
    @Volatile private var cachedOptions: LocationOptions? = null
    @Volatile private var cachedOptionsAt: Instant = Instant.EPOCH

    // 401 detection: set by getLocationOptions/getRemoteState; cleared at runOnce entry
    @Volatile private var isSessionExpired = false

    // Last GPS fix accuracy for diagnostics (set in shouldSkip, null if not yet captured)
    @Volatile private var lastCaptureAccuracyMeters: Double? = null

    // Reauth notification coalescing: track last post time to avoid spamming
    @Volatile private var lastReauthNotificationAt: Instant = Instant.EPOCH

    // Entry point — external triggers retain the existing single-flight/drop behavior. Accuracy
    // retries are different: they belong to an already active episode and wait cancellably for the
    // mutex, so a coincident timer/geofence run cannot silently consume the retry.
    suspend fun runOnce(trigger: OrchestratorTrigger) {
        runOnceInternal(trigger, expectedEpisodeKey = null)
    }

    fun cancelLowAccuracyRetry() {
        lowAccuracyGeneration.incrementAndGet()
        finishLowAccuracyEpisode()
    }

    /**
     * Called only after the server has accepted an activity (live submit or a completed offline
     * replay). The returned state, rather than the submitted payload, decides whether checkout is
     * really the latest activity; retroactive/backlog events therefore cannot start a pause early.
     */
    suspend fun onServerConfirmedState(
        chave: String,
        project: String,
        newState: HistoryState,
    ) {
        mutex.withLock {
            if (newState.chave == chave) {
                cachedState = newState
                cacheChave = chave
                cachedStateAt = clock.now()
            }
            reconcileServerConfirmedState(chave, project, newState)
        }
    }

    /**
     * A settings edit must invalidate an in-flight evaluation and force an immediate reconciliation.
     * PAUSE_START is a guaranteed trigger: it waits for the mutex instead of being dropped.
     */
    suspend fun onScheduledPauseConfigurationChanged(chave: String) {
        if (appPrefs.chave.first() != chave) return
        scheduledPauseGeneration.incrementAndGet()
        cancelScheduledPauseGraceWake()
        runOnce(OrchestratorTrigger.PAUSE_START)
    }

    /**
     * Authentication/project/toggle teardown is synchronous for alarms/jobs and generation-based for
     * an evaluation already suspended in I/O. Persisted runtime state is cleared under the mutex.
     */
    fun invalidateScheduledPauseContext() {
        val generation = scheduledPauseGeneration.incrementAndGet()
        cancelScheduledPauseGraceWake()
        cancelWakeBestEffort(REQUEST_CODE_PAUSE_START)
        cancelWakeBestEffort(REQUEST_CODE_RESUME)
        applicationScope.launch {
            mutex.withLock {
                if (scheduledPauseGeneration.get() == generation) {
                    clearScheduledPauseRuntimeAndWakes()
                }
            }
        }
    }

    suspend fun resetScheduledPauseContext() {
        scheduledPauseGeneration.incrementAndGet()
        cancelScheduledPauseGraceWake()
        cancelWakeBestEffort(REQUEST_CODE_PAUSE_START)
        cancelWakeBestEffort(REQUEST_CODE_RESUME)
        mutex.withLock {
            clearScheduledPauseRuntimeAndWakes()
        }
    }

    private suspend fun runAccuracyRetry(key: LowAccuracyEpisodeKey) {
        runOnceInternal(OrchestratorTrigger.ACCURACY_RETRY, expectedEpisodeKey = key)
    }

    private fun finishLowAccuracyEpisode(key: LowAccuracyEpisodeKey? = null) {
        lowAccuracyRetryScheduler.cancel(key)
        dismissLowAccuracyNotification()
    }

    private fun finishLowAccuracyEpisodeUnless(key: LowAccuracyEpisodeKey?) {
        if (lowAccuracyRetryScheduler.cancelUnless(key)) {
            dismissLowAccuracyNotification()
        }
    }

    private fun dismissLowAccuracyNotification() {
        // NotificationManager is always available on Android; keep teardown best-effort for unusual
        // lifecycle/test contexts so cancellation of the Job itself cannot be prevented.
        runCatching { AutoActivityNotifications.cancelLowAccuracyRetryNotification(context) }
    }

    private suspend fun isLowAccuracyEpisodeStillEligible(
        expectedKey: LowAccuracyEpisodeKey,
    ): Boolean {
        if (appPrefs.chave.first() != expectedKey.chave) return false
        val settingsMap: Map<String, UserSettings?> = runCatching {
            settingsJson.decodeFromString<Map<String, UserSettings?>>(
                appPrefs.userSettingsJson.first(),
            )
        }.getOrElse { emptyMap() }
        val settings = resolvePersistedUserSettings(settingsMap, expectedKey.chave)
        if (!settings.automaticActivitiesEnabled ||
            settings.activeProject != expectedKey.project
        ) {
            return false
        }
        val pauseSettings = ScheduledPauseSettings(
            scheduledPauseEnabled = settings.scheduledPauseEnabled,
            scheduledPauseFrom = settings.scheduledPauseFrom,
            scheduledPauseTo = settings.scheduledPauseTo,
            suspendSaturdays = settings.suspendSaturdays,
            suspendSundays = settings.suspendSundays,
        )
        val nowZdt = clock.now().atZone(ZoneId.systemDefault())
        if (!isScheduledPauseActiveNow(nowZdt, pauseSettings)) return true
        val occurrence = scheduledPauseOccurrence(
            chave = expectedKey.chave,
            project = expectedKey.project,
            settings = pauseSettings,
            zoneId = nowZdt.zone.id,
            startAt = currentPauseStartInstant(nowZdt, pauseSettings),
            resumeAt = nextResumeInstant(nowZdt, pauseSettings),
        ) ?: return true
        val runtime = loadScheduledPauseRuntime()
        // While checkout is still pending, GPS and its low-accuracy retry remain operational.
        // Grace means checkout was server-confirmed, and ACTIVE is the real pause; both retire it.
        return runtime?.takeIf { it.occurrence == occurrence }?.phase.let { phase ->
            phase == null ||
                phase == ScheduledPauseRuntimePhase.AWAITING_CHECKOUT
        }
    }

    private suspend fun runOnceInternal(
        trigger: OrchestratorTrigger,
        expectedEpisodeKey: LowAccuracyEpisodeKey?,
    ) {
        if (trigger == OrchestratorTrigger.ACCURACY_RETRY ||
            trigger == OrchestratorTrigger.PAUSE_START ||
            trigger == OrchestratorTrigger.PAUSE_END ||
            trigger == OrchestratorTrigger.PAUSE_GRACE
        ) {
            mutex.lock()
        } else if (!mutex.tryLock()) {
            return
        }
        // One token covers the entire logical run, including a silent-relogin second pass.
        // A cancellation during any suspension must invalidate both passes.
        val evaluationLowAccuracyGeneration = lowAccuracyGeneration.get()
        val evaluationScheduledPauseGeneration = scheduledPauseGeneration.get()
        try {
            // The retry scheduler performs its three-minute delay before entering here. Consequently
            // the wake lock covers only this bounded evaluation burst, never the waiting interval.
            wakeLock.acquire(WAKE_LOCK_TIMEOUT_MS)
            isSessionExpired = false
            runOnceLocked(
                trigger,
                expectedEpisodeKey,
                evaluationLowAccuracyGeneration,
                evaluationScheduledPauseGeneration,
            )
            if (isSessionExpired) {
                val chave = appPrefs.chave.first().ifEmpty { return }
                val lang = resolveEffectiveLanguageCode(appPrefs.language.first())
                if (attemptSilentRelogin(chave, lang)) {
                    isSessionExpired = false
                    runOnceLocked(
                        trigger,
                        expectedEpisodeKey,
                        evaluationLowAccuracyGeneration,
                        evaluationScheduledPauseGeneration,
                    ) // retry once after successful re-login
                }
            }
        } finally {
            if (wakeLock.isHeld) wakeLock.release()
            mutex.unlock()
        }
    }

    // Accident-only background check — INDEPENDENT of the automatic-activities engine.
    // Driven by AccidentWatchWorker (a dedicated periodic WorkManager job scheduled whenever
    // the user is authenticated), so accident pushes fire even when automatic activities are
    // OFF (the FGS isn't running then). Single-flight via the same mutex as runOnce.
    suspend fun runAccidentCheck() {
        if (!mutex.tryLock()) return
        try {
            val chave = appPrefs.chave.first()
            if (chave.isEmpty()) return
            val lang = resolveEffectiveLanguageCode(appPrefs.language.first())
            val rawJson = appPrefs.userSettingsJson.first()
            val settingsMap: Map<String, UserSettings?> = runCatching {
                settingsJson.decodeFromString<Map<String, UserSettings?>>(rawJson)
            }.getOrElse { emptyMap() }
            val userSettings = resolvePersistedUserSettings(settingsMap, chave)
            if (!userSettings.notifyAccident) return
            isSessionExpired = false
            maybeNotifyAccident(chave, notifyAccident = true, lang = lang)
            if (isSessionExpired && attemptSilentRelogin(chave, lang)) {
                isSessionExpired = false
                maybeNotifyAccident(chave, notifyAccident = true, lang = lang)
            }
        } finally {
            mutex.unlock()
        }
    }

    @Suppress("ReturnCount")
    private suspend fun runOnceLocked(
        trigger: OrchestratorTrigger,
        expectedEpisodeKey: LowAccuracyEpisodeKey?,
        evaluationLowAccuracyGeneration: Long,
        evaluationScheduledPauseGeneration: Long,
    ) {
        // Step 1: Auth — chave must be present (session cookie persisted by OkHttp).
        // 401 during network calls sets isSessionExpired; runOnce() handles the retry.
        val chave = appPrefs.chave.first()
        if (chave.isEmpty()) {
            finishLowAccuracyEpisode()
            clearScheduledPauseRuntimeAndWakes()
            return
        }
        val lang = resolveEffectiveLanguageCode(appPrefs.language.first())
        activityLogger.logTrigger(trigger.name) // plan004 — background evaluation fired (verbose-gated)

        // Step 2: Toggle + scheduled pause
        val rawJson = appPrefs.userSettingsJson.first()
        val settingsMap: Map<String, UserSettings?> = runCatching {
            settingsJson.decodeFromString<Map<String, UserSettings?>>(rawJson)
        }.getOrElse { emptyMap() }
        val userSettings = resolvePersistedUserSettings(settingsMap, chave)
        val currentEpisodeKey = userSettings.activeProject
            .takeIf { it.isNotEmpty() }
            ?.let { LowAccuracyEpisodeKey(chave = chave, project = it) }

        // A delayed retry is valid only for the exact authenticated user/project that created it.
        // Normal triggers also retire an old episode as soon as a key or project change is observed.
        finishLowAccuracyEpisodeUnless(currentEpisodeKey)
        if (expectedEpisodeKey != null && expectedEpisodeKey != currentEpisodeKey) {
            finishLowAccuracyEpisode(expectedEpisodeKey)
            return
        }

        // Accident push — checked before the toggle/pause gates so the user is alerted to an
        // accident in any project regardless of automatic-activities state.
        maybeNotifyAccident(chave, userSettings.notifyAccident, lang)

        if (!userSettings.automaticActivitiesEnabled) {
            finishLowAccuracyEpisode(currentEpisodeKey)
            clearScheduledPauseRuntimeAndWakes()
            EvaluationLog.record(EvaluationEntry(clock.now(), trigger, null, null, null, EvaluationOutcome.TOGGLE_OFF))
            activityLogger.logSystem("Automatic activities are OFF.", ActivitySeverity.WARNING) // plan004
            return
        }
        if (userSettings.activeProject.isEmpty()) {
            finishLowAccuracyEpisode(currentEpisodeKey)
            clearScheduledPauseRuntimeAndWakes()
            return
        }

        val pauseSettings = ScheduledPauseSettings(
            scheduledPauseEnabled = userSettings.scheduledPauseEnabled,
            scheduledPauseFrom = userSettings.scheduledPauseFrom,
            scheduledPauseTo = userSettings.scheduledPauseTo,
            suspendSaturdays = userSettings.suspendSaturdays,
            suspendSundays = userSettings.suspendSundays,
        )
        val nowZdt = clock.now().atZone(ZoneId.systemDefault())
        val pauseGate = evaluateScheduledPauseGate(
            chave = chave,
            userSettings = userSettings,
            pauseSettings = pauseSettings,
            nowZdt = nowZdt,
            lang = lang,
            evaluationGeneration = evaluationScheduledPauseGeneration,
        )
        val pauseConfirmedState = when (pauseGate) {
            ScheduledPauseGateDecision.AwaitingVerification -> {
                // Unknown is not equivalent to "no history": neither start the pause nor enter the
                // situation matrix until the server can confirm whether a check-in is open.
                EvaluationLog.record(
                    EvaluationEntry(
                        clock.now(),
                        trigger,
                        null,
                        null,
                        null,
                        EvaluationOutcome.NETWORK_ERROR,
                    ),
                )
                return
            }
            ScheduledPauseGateDecision.Active -> {
                finishLowAccuracyEpisode(currentEpisodeKey)
                EvaluationLog.record(
                    EvaluationEntry(clock.now(), trigger, null, null, null, EvaluationOutcome.PAUSED),
                )
                return
            }
            ScheduledPauseGateDecision.Grace -> {
                // Checkout is server-confirmed; GPS is quiet during grace (or the terminal
                // too-close-to-resume remainder), without posting pause transition notifications.
                finishLowAccuracyEpisode(currentEpisodeKey)
                EvaluationLog.record(
                    EvaluationEntry(clock.now(), trigger, null, null, null, EvaluationOutcome.NO_ACTION),
                )
                return
            }
            is ScheduledPauseGateDecision.Continue -> pauseGate.confirmedState
        }

        // Fetch location options (accuracy threshold + mixed-zone interval) — cached 15 min
        val options = getLocationOptions() ?: run {
            finishLowAccuracyEpisode(currentEpisodeKey)
            return
        }

        // Step 3: Skip-if-unchanged (TIMER ticks only — geofence/foreground always run)
        val hasActiveLowAccuracyEpisode =
            currentEpisodeKey != null && lowAccuracyRetryScheduler.isActiveFor(currentEpisodeKey)
        if (shouldEvaluateTimerMovement(trigger, hasActiveLowAccuracyEpisode)) {
            lastCaptureAccuracyMeters = null
            val skipDecision = shouldSkip(options.accuracyThresholdMeters)
            if (BuildConfig.DEBUG) {
                lastCaptureAccuracyMeters?.let { LocationMeasurementCollector.record(trigger, it) }
            }
            if (skipDecision == SkipDecision.SKIP) {
                EvaluationLog.record(EvaluationEntry(clock.now(), trigger, lastCaptureAccuracyMeters, null, null, EvaluationOutcome.SKIP))
                activityLogger.logSystem("Auto-check skipped (no movement).") // plan004
                return
            }
        }

        // Steps 4–6: GPS → POST /check/location → GET /check/state → Situation engine → submit
        val currentState = pauseConfirmedState ?: getRemoteState(chave)
        val userProjects = UserProjects(
            projects = userSettings.projects,
            activeProject = userSettings.activeProject,
        )

        // A manual submit/toggle/project transition may have completed while options/state were
        // loading. Do not let that stale run reach the decision matrix or submit an automatic event.
        if (evaluationLowAccuracyGeneration != lowAccuracyGeneration.get()) return
        if (evaluationScheduledPauseGeneration != scheduledPauseGeneration.get()) return

        val result = runAutomaticActivitiesUseCase(
            chave = chave,
            userProjects = userProjects,
            currentState = currentState,
            mixedZoneIntervalMinutes = options.mixedZoneIntervalMinutes,
            accuracyThresholdMeters = options.accuracyThresholdMeters,
        )

        when (result) {
            is AutoActivitiesResult.AccuracyTooLow -> {
                lastCaptureAccuracyMeters = result.accuracyMeters
                val episodeKey = currentEpisodeKey
                if (episodeKey != null &&
                    evaluationLowAccuracyGeneration == lowAccuracyGeneration.get() &&
                    isLowAccuracyEpisodeStillEligible(episodeKey)
                ) {
                    // startOrKeep returns true only for the first low reading of this episode.
                    // Repeated external triggers keep the original deadline and cannot spam.
                    val isNewEpisode =
                        lowAccuracyRetryScheduler.startOrKeep(episodeKey) { retryKey ->
                            runAccuracyRetry(retryKey)
                        }
                    val canPostNotification =
                        evaluationLowAccuracyGeneration == lowAccuracyGeneration.get() &&
                            isLowAccuracyEpisodeStillEligible(episodeKey)
                    if (!canPostNotification) {
                        // Cancellation raced startOrKeep after the pre-check.
                        finishLowAccuracyEpisode(episodeKey)
                    } else if (isNewEpisode) {
                        AutoActivityNotifications.postLowAccuracyRetryNotification(
                            context = context,
                            expectedAction = result.expectedAction,
                            lang = lang,
                        )
                        if (evaluationLowAccuracyGeneration != lowAccuracyGeneration.get()) {
                            // Cancellation raced notification delivery after the second check.
                            finishLowAccuracyEpisode(episodeKey)
                        }
                    }
                } else {
                    // The run started under stale user/project/toggle/pause state.
                    finishLowAccuracyEpisode(currentEpisodeKey)
                }
            }
            AutoActivitiesResult.CaptureTimeout -> {
                // A timeout is deliberately neutral: keep an existing episode alive, but never
                // create one without an explicit ACCURACY_TOO_LOW server result.
            }
            is AutoActivitiesResult.Submitted -> {
                finishLowAccuracyEpisode(currentEpisodeKey)
                // Update state cache on success so the next tick has a fresh baseline.
                cachedState = result.newState
                cacheChave = chave
                cachedStateAt = clock.now()
                // This is an online server success. Queued events deliberately do not arrive here;
                // replay notifies us only after its final confirmed state is known.
                reconcileServerConfirmedState(
                    chave = chave,
                    project = userSettings.activeProject,
                    newState = result.newState,
                )
            }
            AutoActivitiesResult.NoAction,
            AutoActivitiesResult.NetworkError,
            AutoActivitiesResult.NotConfigured,
            AutoActivitiesResult.NoPermission,
            -> finishLowAccuracyEpisode(currentEpisodeKey)
        }

        // plan004 — orchestrator-owned outcome. Submitted/NetworkError/NotConfigured are logged by the
        // use-case; only the "nothing to do" outcome is logged here (once per completed run).
        if (result is AutoActivitiesResult.NoAction) {
            activityLogger.logSystem("No action needed (already checked in/out).")
        }

        // Diagnostics: record the outcome for every completed run.
        EvaluationLog.record(
            EvaluationEntry(
                at = clock.now(),
                trigger = trigger,
                accuracyMeters = lastCaptureAccuracyMeters,
                resolvedLocal = (result as? AutoActivitiesResult.Submitted)?.local,
                decidedAction = (result as? AutoActivitiesResult.Submitted)?.action?.name,
                outcome = when (result) {
                    is AutoActivitiesResult.Submitted -> EvaluationOutcome.SUBMITTED
                    is AutoActivitiesResult.AccuracyTooLow -> EvaluationOutcome.NO_ACTION
                    AutoActivitiesResult.CaptureTimeout -> EvaluationOutcome.NO_ACTION
                    AutoActivitiesResult.NoPermission -> EvaluationOutcome.NO_ACTION
                    AutoActivitiesResult.NoAction -> EvaluationOutcome.NO_ACTION
                    AutoActivitiesResult.NotConfigured -> EvaluationOutcome.NO_ACTION
                    AutoActivitiesResult.NetworkError -> EvaluationOutcome.NETWORK_ERROR
                },
            )
        )

        // Step 7: Post activity notification (skip for FOREGROUND — UI is visible; and only
        // when the user opted into activity notifications).
        if (result is AutoActivitiesResult.Submitted && trigger != OrchestratorTrigger.FOREGROUND &&
            userSettings.notifyActivities
        ) {
            AutoActivityNotifications.postActivityNotification(context, result.action, result.local, lang)
        }

        // Restore the "active" service notification text in case it was showing "paused"
        if (AutoActivityForegroundService.isRunning && result != AutoActivitiesResult.NetworkError) {
            AutoActivityNotifications.updateServiceNotification(context, isPaused = false, lang = lang)
        }
    }

    private suspend fun evaluateScheduledPauseGate(
        chave: String,
        userSettings: UserSettings,
        pauseSettings: ScheduledPauseSettings,
        nowZdt: java.time.ZonedDateTime,
        lang: String,
        evaluationGeneration: Long,
    ): ScheduledPauseGateDecision {
        var runtime = loadScheduledPauseRuntime()
        val project = userSettings.activeProject
        val isWindowActive = isScheduledPauseActiveNow(nowZdt, pauseSettings)

        if (!isWindowActive) {
            val shouldNotifyEnd =
                runtime?.phase == ScheduledPauseRuntimePhase.ACTIVE &&
                    runtime.occurrence.chave == chave &&
                    runtime.occurrence.project == project
            if (runtime != null || legacyPauseFlag()) {
                clearScheduledPauseRuntime()
                if (shouldNotifyEnd) {
                    if (userSettings.notifyScheduledPause) {
                        AutoActivityNotifications.postScheduledPauseTransition(
                            context,
                            started = false,
                            lang = lang,
                        )
                    }
                    activityLogger.logActive("Scheduled pause ended.")
                }
            }
            cancelScheduledPauseGraceWake()
            cancelWakeBestEffort(REQUEST_CODE_RESUME)
            scheduleStartAlarm(nextPauseStartInstant(nowZdt, pauseSettings))
            if (AutoActivityForegroundService.isRunning) {
                AutoActivityNotifications.updateServiceNotification(context, isPaused = false, lang = lang)
            }
            return ScheduledPauseGateDecision.Continue(confirmedState = null)
        }

        val occurrence = scheduledPauseOccurrence(
            chave = chave,
            project = project,
            settings = pauseSettings,
            zoneId = nowZdt.zone.id,
            startAt = currentPauseStartInstant(nowZdt, pauseSettings),
            resumeAt = nextResumeInstant(nowZdt, pauseSettings),
        ) ?: return ScheduledPauseGateDecision.Continue(confirmedState = null)
        cancelWakeBestEffort(REQUEST_CODE_PAUSE_START)

        // A different user/project/configuration/window cannot inherit the old runtime phase.
        if (runtime != null && runtime.occurrence != occurrence) {
            val shouldNotifyEnd =
                runtime.phase == ScheduledPauseRuntimePhase.ACTIVE &&
                    runtime.occurrence.chave == chave &&
                    runtime.occurrence.project == project
            clearScheduledPauseRuntime()
            cancelScheduledPauseGraceWake()
            cancelWakeBestEffort(REQUEST_CODE_RESUME)
            if (shouldNotifyEnd) {
                if (userSettings.notifyScheduledPause) {
                    AutoActivityNotifications.postScheduledPauseTransition(
                        context,
                        started = false,
                        lang = lang,
                    )
                }
                activityLogger.logActive("Scheduled pause ended after configuration change.")
            }
            runtime = null
        }

        if (runtime?.phase == ScheduledPauseRuntimePhase.ACTIVE) {
            cancelScheduledPauseGraceWake()
            handleScheduledPause(pauseSettings, lang)
            return ScheduledPauseGateDecision.Active
        }

        if (runtime?.phase == ScheduledPauseRuntimePhase.SKIPPED) {
            cancelScheduledPauseGraceWake()
            scheduleResumeAlarm(Instant.ofEpochMilli(occurrence.resumeAtEpochMs))
            // The occurrence ended before ten full seconds could elapse. Suppress further
            // automatic activity/retry until resume, but never emit pause start/end notifications.
            return ScheduledPauseGateDecision.Grace
        }

        if (runtime?.phase == ScheduledPauseRuntimePhase.GRACE) {
            val activateAt = runtime.activateAtEpochMs
            if (activateAt != null && clock.now().toEpochMilli() < activateAt) {
                scheduleScheduledPauseGraceWake(runtime)
                scheduleResumeAlarm(Instant.ofEpochMilli(occurrence.resumeAtEpochMs))
                if (AutoActivityForegroundService.isRunning) {
                    AutoActivityNotifications.updateServiceNotification(
                        context,
                        isPaused = false,
                        lang = lang,
                    )
                }
                return ScheduledPauseGateDecision.Grace
            }
        }

        // At the boundary, while awaiting checkout, and when grace expires, force a fresh server
        // read. A cached or unavailable state must never strand an actually checked-in user.
        val freshStateResult = getFreshRemoteState(chave)
        if (evaluationGeneration != scheduledPauseGeneration.get()) {
            return ScheduledPauseGateDecision.Continue(confirmedState = null)
        }
        if (!isScheduledPauseContextCurrent(chave, project, occurrence)) {
            return ScheduledPauseGateDecision.Continue(confirmedState = null)
        }

        val confirmedState = (freshStateResult as? AppResult.Success)?.data
        if (confirmedState == null) {
            if (runtime?.phase == ScheduledPauseRuntimePhase.GRACE) {
                // Grace is due, but the final state cannot be revalidated. Keep the original
                // deadline and retry soon; do not start a pause on an uncertain server state.
                schedulePauseConfirmationRetry(occurrence)
                scheduleResumeAlarm(Instant.ofEpochMilli(occurrence.resumeAtEpochMs))
                return ScheduledPauseGateDecision.Grace
            }
            persistScheduledPauseRuntime(
                ScheduledPauseRuntimeState(
                    occurrence = occurrence,
                    phase = ScheduledPauseRuntimePhase.AWAITING_CHECKOUT,
                ),
            )
            schedulePauseConfirmationRetry(occurrence)
            scheduleResumeAlarm(Instant.ofEpochMilli(occurrence.resumeAtEpochMs))
            return ScheduledPauseGateDecision.AwaitingVerification
        }

        return if (resolveLastRecordedAction(confirmedState) == CheckAction.CHECKIN) {
            // The pause remains pending, including across process death. Automatic checkout and a
            // low-accuracy retry are still allowed in this phase.
            persistScheduledPauseRuntime(
                ScheduledPauseRuntimeState(
                    occurrence = occurrence,
                    phase = ScheduledPauseRuntimePhase.AWAITING_CHECKOUT,
                ),
            )
            cancelScheduledPauseGraceWake()
            scheduleResumeAlarm(Instant.ofEpochMilli(occurrence.resumeAtEpochMs))
            if (AutoActivityForegroundService.isRunning) {
                AutoActivityNotifications.updateServiceNotification(context, isPaused = false, lang = lang)
            }
            ScheduledPauseGateDecision.Continue(confirmedState)
        } else if (runtime?.phase == ScheduledPauseRuntimePhase.AWAITING_CHECKOUT &&
            resolveLastRecordedAction(confirmedState) == CheckAction.CHECKOUT
        ) {
            // Checkout may have been accepted by another client while this process was dead. This
            // fresh GET is authoritative; its last-checkout timestamp anchors the ten seconds.
            when (prepareScheduledPauseGrace(occurrence, confirmedState.lastCheckoutAt)) {
                ScheduledPauseGraceArmResult.WAITING -> ScheduledPauseGateDecision.Grace
                ScheduledPauseGraceArmResult.DUE_NOW -> {
                    activateScheduledPause(
                        occurrence = occurrence,
                        pauseSettings = pauseSettings,
                        notifyTransition = userSettings.notifyScheduledPause,
                        lang = lang,
                    )
                    ScheduledPauseGateDecision.Active
                }
                ScheduledPauseGraceArmResult.WINDOW_EXPIRED ->
                    ScheduledPauseGateDecision.Grace
            }
        } else {
            val checkoutAt = confirmedState.lastCheckoutAt
            if (resolveLastRecordedAction(confirmedState) == CheckAction.CHECKOUT &&
                checkoutOccurredInside(occurrence, checkoutAt)
            ) {
                // The exact boundary wake may have been delayed by the OS. A checkout recorded
                // after this occurrence began still receives its remaining ten-second grace.
                when (prepareScheduledPauseGrace(occurrence, checkoutAt)) {
                    ScheduledPauseGraceArmResult.WAITING -> ScheduledPauseGateDecision.Grace
                    ScheduledPauseGraceArmResult.DUE_NOW -> {
                        activateScheduledPause(
                            occurrence = occurrence,
                            pauseSettings = pauseSettings,
                            notifyTransition = userSettings.notifyScheduledPause,
                            lang = lang,
                        )
                        ScheduledPauseGateDecision.Active
                    }
                    ScheduledPauseGraceArmResult.WINDOW_EXPIRED ->
                        ScheduledPauseGateDecision.Grace
                }
            } else {
                // Checkout predating the occurrence, or no open check-in/history, uses normal
                // boundary semantics and starts immediately.
                activateScheduledPause(
                    occurrence = occurrence,
                    pauseSettings = pauseSettings,
                    notifyTransition = userSettings.notifyScheduledPause,
                    lang = lang,
                )
                ScheduledPauseGateDecision.Active
            }
        }
    }

    private suspend fun reconcileServerConfirmedState(
        chave: String,
        project: String,
        newState: HistoryState,
    ) {
        if (newState.chave != chave) return
        if (newState.projeto != null && newState.projeto != project) return
        if (appPrefs.chave.first() != chave) return

        val settings = loadUserSettings(chave)
        if (!settings.automaticActivitiesEnabled || settings.activeProject != project) {
            clearScheduledPauseRuntimeFor(chave, project)
            return
        }
        val pauseSettings = settings.toScheduledPauseSettings()
        val nowZdt = clock.now().atZone(ZoneId.systemDefault())
        if (!isScheduledPauseActiveNow(nowZdt, pauseSettings)) {
            clearScheduledPauseRuntimeFor(chave, project)
            return
        }
        val occurrence = scheduledPauseOccurrence(
            chave = chave,
            project = project,
            settings = pauseSettings,
            zoneId = nowZdt.zone.id,
            startAt = currentPauseStartInstant(nowZdt, pauseSettings),
            resumeAt = nextResumeInstant(nowZdt, pauseSettings),
        ) ?: return

        var runtime = loadScheduledPauseRuntime()
        if (runtime != null && runtime.occurrence != occurrence) {
            clearScheduledPauseRuntime()
            runtime = null
        }
        val lastAction = resolveLastRecordedAction(newState)
        if (runtime == null) {
            val canCreateDeferredOccurrence =
                lastAction == CheckAction.CHECKIN ||
                    (lastAction == CheckAction.CHECKOUT &&
                        checkoutOccurredInside(occurrence, newState.lastCheckoutAt))
            if (!canCreateDeferredOccurrence) return
            runtime = ScheduledPauseRuntimeState(
                occurrence = occurrence,
                phase = ScheduledPauseRuntimePhase.AWAITING_CHECKOUT,
            )
            persistScheduledPauseRuntime(runtime)
        }
        if (runtime.phase == ScheduledPauseRuntimePhase.SKIPPED) {
            scheduleResumeAlarm(Instant.ofEpochMilli(occurrence.resumeAtEpochMs))
            return
        }
        if (lastAction == CheckAction.CHECKIN) {
            val isAlreadyActive = runtime.phase == ScheduledPauseRuntimePhase.ACTIVE
            // The exception is decided at the boundary. Once a normal pause is ACTIVE, a later
            // check-in from another client does not retroactively reopen that decision.
            if (isAlreadyActive) return
            persistScheduledPauseRuntime(
                ScheduledPauseRuntimeState(
                    occurrence = occurrence,
                    phase = ScheduledPauseRuntimePhase.AWAITING_CHECKOUT,
                ),
            )
            cancelScheduledPauseGraceWake()
            scheduleResumeAlarm(Instant.ofEpochMilli(occurrence.resumeAtEpochMs))
            return
        }
        if (lastAction != CheckAction.CHECKOUT) return

        when (runtime.phase) {
            ScheduledPauseRuntimePhase.ACTIVE -> return
            ScheduledPauseRuntimePhase.GRACE -> {
                // Duplicate server callbacks do not extend the original deadline.
                scheduleScheduledPauseGraceWake(runtime)
                scheduleResumeAlarm(Instant.ofEpochMilli(occurrence.resumeAtEpochMs))
                return
            }
            ScheduledPauseRuntimePhase.AWAITING_CHECKOUT -> Unit
            ScheduledPauseRuntimePhase.SKIPPED -> return
        }

        when (prepareScheduledPauseGrace(occurrence, newState.lastCheckoutAt)) {
            ScheduledPauseGraceArmResult.WAITING,
            ScheduledPauseGraceArmResult.WINDOW_EXPIRED,
            -> Unit
            ScheduledPauseGraceArmResult.DUE_NOW -> {
                activateScheduledPause(
                    occurrence = occurrence,
                    pauseSettings = pauseSettings,
                    notifyTransition = settings.notifyScheduledPause,
                    lang = resolveEffectiveLanguageCode(appPrefs.language.first()),
                )
            }
        }
    }

    private suspend fun prepareScheduledPauseGrace(
        occurrence: ScheduledPauseOccurrence,
        confirmedCheckoutAt: Instant?,
    ): ScheduledPauseGraceArmResult {
        val now = clock.now()
        val activateAt = (confirmedCheckoutAt ?: now)
            .plusSeconds(SCHEDULED_PAUSE_GRACE_SECONDS)
        if (activateAt.toEpochMilli() >= occurrence.resumeAtEpochMs) {
            persistScheduledPauseRuntime(
                ScheduledPauseRuntimeState(
                    occurrence = occurrence,
                    phase = ScheduledPauseRuntimePhase.SKIPPED,
                ),
            )
            cancelScheduledPauseGraceWake()
            scheduleResumeAlarm(Instant.ofEpochMilli(occurrence.resumeAtEpochMs))
            return ScheduledPauseGraceArmResult.WINDOW_EXPIRED
        }
        if (!activateAt.isAfter(now)) {
            return ScheduledPauseGraceArmResult.DUE_NOW
        }
        val grace = ScheduledPauseRuntimeState(
            occurrence = occurrence,
            phase = ScheduledPauseRuntimePhase.GRACE,
            activateAtEpochMs = activateAt.toEpochMilli(),
        )
        persistScheduledPauseRuntime(grace)
        finishLowAccuracyEpisode(
            LowAccuracyEpisodeKey(occurrence.chave, occurrence.project),
        )
        scheduleScheduledPauseGraceWake(grace)
        scheduleResumeAlarm(Instant.ofEpochMilli(occurrence.resumeAtEpochMs))
        return ScheduledPauseGraceArmResult.WAITING
    }

    private fun checkoutOccurredInside(
        occurrence: ScheduledPauseOccurrence,
        checkoutAt: Instant?,
    ): Boolean {
        val at = checkoutAt?.toEpochMilli() ?: return false
        return at >= occurrence.startAtEpochMs && at < occurrence.resumeAtEpochMs
    }

    private suspend fun activateScheduledPause(
        occurrence: ScheduledPauseOccurrence,
        pauseSettings: ScheduledPauseSettings,
        notifyTransition: Boolean,
        lang: String,
    ) {
        persistScheduledPauseRuntime(
            ScheduledPauseRuntimeState(
                occurrence = occurrence,
                phase = ScheduledPauseRuntimePhase.ACTIVE,
            ),
        )
        appPrefs.setFlag(FLAG_PAUSE_ACTIVE, true)
        cancelScheduledPauseGraceWake()
        cancelWakeBestEffort(REQUEST_CODE_PAUSE_START)
        if (notifyTransition) {
            AutoActivityNotifications.postScheduledPauseTransition(context, started = true, lang = lang)
        }
        activityLogger.logInactive("Scheduled pause started.")
        handleScheduledPause(pauseSettings, lang)
    }

    private suspend fun isScheduledPauseContextCurrent(
        chave: String,
        project: String,
        expectedOccurrence: ScheduledPauseOccurrence,
    ): Boolean {
        if (appPrefs.chave.first() != chave) return false
        val settings = loadUserSettings(chave)
        if (!settings.automaticActivitiesEnabled || settings.activeProject != project) return false
        val pauseSettings = settings.toScheduledPauseSettings()
        val nowZdt = clock.now().atZone(ZoneId.systemDefault())
        if (!isScheduledPauseActiveNow(nowZdt, pauseSettings)) return false
        return scheduledPauseOccurrence(
            chave = chave,
            project = project,
            settings = pauseSettings,
            zoneId = nowZdt.zone.id,
            startAt = currentPauseStartInstant(nowZdt, pauseSettings),
            resumeAt = nextResumeInstant(nowZdt, pauseSettings),
        ) == expectedOccurrence
    }

    private suspend fun loadUserSettings(chave: String): UserSettings {
        val rawJson = appPrefs.userSettingsJson.first()
        val settingsMap: Map<String, UserSettings?> = runCatching {
            settingsJson.decodeFromString<Map<String, UserSettings?>>(rawJson)
        }.getOrElse { emptyMap() }
        return resolvePersistedUserSettings(settingsMap, chave)
    }

    private fun UserSettings.toScheduledPauseSettings() = ScheduledPauseSettings(
        scheduledPauseEnabled = scheduledPauseEnabled,
        scheduledPauseFrom = scheduledPauseFrom,
        scheduledPauseTo = scheduledPauseTo,
        suspendSaturdays = suspendSaturdays,
        suspendSundays = suspendSundays,
    )

    private suspend fun loadScheduledPauseRuntime(): ScheduledPauseRuntimeState? {
        val raw = runCatching { appPrefs.scheduledPauseRuntimeJson.first() }.getOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: return null
        return try {
            settingsJson.decodeFromString<ScheduledPauseRuntimeState>(raw)
        } catch (_: Exception) {
            // Corruption/old incompatible schema is a one-time migration event, not a permanent
            // "null on every run" state.
            appPrefs.setScheduledPauseRuntimeJson("")
            appPrefs.setFlag(FLAG_PAUSE_ACTIVE, false)
            null
        }
    }

    private suspend fun persistScheduledPauseRuntime(state: ScheduledPauseRuntimeState) {
        appPrefs.setScheduledPauseRuntimeJson(settingsJson.encodeToString(state))
        if (state.phase != ScheduledPauseRuntimePhase.ACTIVE) {
            appPrefs.setFlag(FLAG_PAUSE_ACTIVE, false)
        }
    }

    private suspend fun clearScheduledPauseRuntime() {
        appPrefs.setScheduledPauseRuntimeJson("")
        appPrefs.setFlag(FLAG_PAUSE_ACTIVE, false)
    }

    private suspend fun clearScheduledPauseRuntimeAndWakes() {
        clearScheduledPauseRuntime()
        cancelScheduledPauseGraceWake()
        cancelWakeBestEffort(REQUEST_CODE_PAUSE_START)
        cancelWakeBestEffort(REQUEST_CODE_RESUME)
    }

    private suspend fun clearScheduledPauseRuntimeFor(chave: String, project: String) {
        val runtime = loadScheduledPauseRuntime() ?: return
        if (runtime.occurrence.chave != chave || runtime.occurrence.project != project) return
        clearScheduledPauseRuntime()
        cancelScheduledPauseGraceWake()
        cancelWakeBestEffort(REQUEST_CODE_RESUME)
    }

    private suspend fun legacyPauseFlag(): Boolean =
        runCatching { appPrefs.getFlag(FLAG_PAUSE_ACTIVE).first() }.getOrDefault(false)

    private fun scheduleScheduledPauseGraceWake(state: ScheduledPauseRuntimeState) {
        val activateAt = state.activateAtEpochMs ?: return
        if (activateAt >= state.occurrence.resumeAtEpochMs) {
            cancelScheduledPauseGraceWake()
            return
        }
        scheduledPauseGraceJob?.cancel()
        scheduleExactWake(REQUEST_CODE_PAUSE_GRACE, activateAt)
        val waitMillis = (activateAt - clock.now().toEpochMilli()).coerceAtLeast(0L)
        scheduledPauseGraceJob = applicationScope.launch {
            delay(waitMillis)
            // Clear the reference before entering the orchestrator so activation does not cancel
            // the coroutine that currently owns the guaranteed trigger.
            scheduledPauseGraceJob = null
            runOnce(OrchestratorTrigger.PAUSE_GRACE)
        }
    }

    private fun schedulePauseConfirmationRetry(occurrence: ScheduledPauseOccurrence) {
        val retryAt = clock.now().plusSeconds(SCHEDULED_PAUSE_CONFIRM_RETRY_SECONDS)
        if (retryAt.toEpochMilli() >= occurrence.resumeAtEpochMs) {
            cancelScheduledPauseGraceWake()
            return
        }
        scheduledPauseGraceJob?.cancel()
        scheduleExactWake(REQUEST_CODE_PAUSE_GRACE, retryAt.toEpochMilli())
        scheduledPauseGraceJob = applicationScope.launch {
            delay(SCHEDULED_PAUSE_CONFIRM_RETRY_SECONDS * 1_000L)
            scheduledPauseGraceJob = null
            runOnce(OrchestratorTrigger.PAUSE_GRACE)
        }
    }

    private fun cancelScheduledPauseGraceWake() {
        scheduledPauseGraceJob?.cancel()
        scheduledPauseGraceJob = null
        cancelWakeBestEffort(REQUEST_CODE_PAUSE_GRACE)
    }

    private suspend fun getFreshRemoteState(chave: String): AppResult<HistoryState> {
        val result = checkRepository.getState(chave)
        when (result) {
            is AppResult.Success -> {
                cachedState = result.data
                cacheChave = chave
                cachedStateAt = clock.now()
            }
            is AppResult.Failure -> {
                if (result.error is ApiError.Unauthorized) isSessionExpired = true
            }
        }
        return result
    }

    // Called while a scheduled pause is active: refresh the service notification and (re)schedule
    // the exact-alarm resume so the engine wakes the moment the pause ends.
    private fun handleScheduledPause(settings: ScheduledPauseSettings, lang: String) {
        if (AutoActivityForegroundService.isRunning) {
            AutoActivityNotifications.updateServiceNotification(context, isPaused = true, lang = lang)
        }
        val resumeInstant = nextResumeInstant(clock.now().atZone(ZoneId.systemDefault()), settings)
        if (resumeInstant != null) {
            scheduleResumeAlarm(resumeInstant)
        }
    }

    // Polls accident state and posts a push when a NEW accident appears (any project).
    // The "seen" id set is PERSISTED (AppPreferences), so an accident reported while the
    // process was dead is still detected on the next run, and the same accident is never
    // notified twice across restarts.
    private suspend fun maybeNotifyAccident(chave: String, notifyAccident: Boolean, lang: String) {
        if (!notifyAccident) return
        when (val r = accidentRepository.getState(chave)) {
            is AppResult.Success -> {
                val activeIds = r.data.activeAccidents.map { it.accidentId }.toSet()
                val seen = appPrefs.seenAccidentIds.first()
                val newIds = activeIds - seen
                if (newIds.isNotEmpty()) {
                    AutoActivityNotifications.postAccidentNotification(context, lang)
                }
                if (activeIds != seen) appPrefs.setSeenAccidentIds(activeIds)
            }
            is AppResult.Failure -> {
                if (r.error is ApiError.Unauthorized) isSessionExpired = true
            }
        }
    }

    // Quick location capture for the skip-if-unchanged check.
    // Returns SKIP if the device has not moved beyond the threshold since the last evaluation.
    // Stores the new fix as the baseline for the next comparison.
    private suspend fun shouldSkip(accuracyThresholdMeters: Int): SkipDecision {
        val capture = locationProvider.capture(accuracyThresholdMeters)
        if (capture !is LocationCapture.Success) return SkipDecision.NO_FIX

        lastCaptureAccuracyMeters = capture.accuracyMeters
        if (!isEligibleMovementBaseline(capture.accuracyMeters, accuracyThresholdMeters)) {
            // An unreliable fix is never movement evidence. In particular, do not replace the last
            // precise baseline with it and do not emit SKIP, otherwise future timer ticks can keep
            // suppressing the full server accuracy evaluation indefinitely.
            return SkipDecision.NO_FIX
        }

        val prevLat = lastLat
        val prevLon = lastLon
        lastLat = capture.lat
        lastLon = capture.lon

        if (prevLat == null || prevLon == null) return SkipDecision.RUN

        val results = FloatArray(1)
        android.location.Location.distanceBetween(prevLat, prevLon, capture.lat, capture.lon, results)
        val distanceMeters = results[0]
        val threshold = maxOf(SKIP_THRESHOLD_METERS, 2.0 * capture.accuracyMeters)
        return if (distanceMeters < threshold) SkipDecision.SKIP else SkipDecision.RUN
    }

    private suspend fun getLocationOptions(): LocationOptions? {
        val now = clock.now()
        val cached = cachedOptions
        if (cached != null && Duration.between(cachedOptionsAt, now) < LOCATION_OPTIONS_TTL) {
            return cached
        }
        return when (val r = checkRepository.getLocations()) {
            is AppResult.Success -> r.data.also {
                cachedOptions = it
                cachedOptionsAt = now
            }
            is AppResult.Failure -> {
                // Session expired → mark so runOnce() can silently re-login and retry (then bail below).
                if (r.error is ApiError.Unauthorized) isSessionExpired = true
                offlineFallbackLocationOptions(cached, r.error)
            }
        }
    }

    private suspend fun getRemoteState(chave: String): HistoryState? {
        val now = clock.now()
        if (chave == cacheChave && cachedState != null &&
            Duration.between(cachedStateAt, now) < STATE_CACHE_TTL
        ) {
            return cachedState
        }
        return when (val r = checkRepository.getState(chave)) {
            is AppResult.Success -> r.data.also {
                cachedState = it
                cacheChave = chave
                cachedStateAt = now
            }
            is AppResult.Failure -> {
                if (r.error is ApiError.Unauthorized) isSessionExpired = true
                null
            }
        }
    }

    // Schedules an exact alarm to re-trigger the engine when the pause ENDS.
    private fun scheduleResumeAlarm(resumeAt: Instant) {
        scheduleExactWake(REQUEST_CODE_RESUME, resumeAt.toEpochMilli())
    }

    // Schedules an exact alarm to re-trigger the engine when the next pause STARTS, so the
    // pause-start notification fires precisely. Cancels a stale alarm when startAt is null.
    private fun scheduleStartAlarm(startAt: Instant?) {
        if (startAt == null) {
            cancelWake(REQUEST_CODE_PAUSE_START)
        } else {
            scheduleExactWake(REQUEST_CODE_PAUSE_START, startAt.toEpochMilli())
        }
    }

    // Wakes the FGS (which calls runOnce) at the given time. Uses an exact alarm when allowed,
    // degrading to an inexact-while-idle alarm if exact alarms aren't permitted (so it still
    // fires, just less precisely, when the user hasn't granted the exact-alarm permission).
    private fun scheduleExactWake(requestCode: Int, atMillis: Long) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = wakePendingIntent(requestCode)
        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || am.canScheduleExactAlarms()
        if (canExact) {
            runCatching { am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, pendingIntent) }
                .onFailure { am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, pendingIntent) }
        } else {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, pendingIntent)
        }
    }

    private fun cancelWake(requestCode: Int) {
        (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager)
            .cancel(wakePendingIntent(requestCode))
    }

    private fun cancelWakeBestEffort(requestCode: Int) {
        runCatching { cancelWake(requestCode) }
    }

    private fun wakePendingIntent(requestCode: Int): PendingIntent =
        PendingIntent.getService(
            context, requestCode,
            Intent(
                when (requestCode) {
                    REQUEST_CODE_PAUSE_START -> AutoActivityForegroundService.ACTION_PAUSE_START
                    REQUEST_CODE_RESUME -> AutoActivityForegroundService.ACTION_PAUSE_END
                    REQUEST_CODE_PAUSE_GRACE -> AutoActivityForegroundService.ACTION_PAUSE_GRACE
                    else -> null
                },
                null,
                context,
                AutoActivityForegroundService::class.java,
            ),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    // Attempts a silent re-login using the stored password (§23.5, T3B.7).
    // Returns true if login succeeded (caller should retry the run).
    // Returns false if no password is stored or the server rejects it
    // (in both cases posts a coalesced reauth notification).
    private suspend fun attemptSilentRelogin(chave: String, lang: String): Boolean {
        val password = securePasswordStore.getPassword(chave)
        if (password.isEmpty()) {
            postReauthNotificationCoalesced(lang)
            activityLogger.logError("Re-authentication required.") // plan004
            return false
        }
        return when (authRepository.login(chave, password)) {
            is AppResult.Success -> {
                activityLogger.logAuth("Session refreshed.") // plan004
                true
            }
            is AppResult.Failure -> {
                postReauthNotificationCoalesced(lang)
                activityLogger.logError("Re-authentication required.") // plan004
                false
            }
        }
    }

    // Posts the reauth notification at most once per REAUTH_NOTIFICATION_COOLDOWN window.
    private fun postReauthNotificationCoalesced(lang: String) {
        val now = clock.now()
        if (Duration.between(lastReauthNotificationAt, now) > REAUTH_NOTIFICATION_COOLDOWN) {
            AutoActivityNotifications.postReauthNotification(context, lang)
            lastReauthNotificationAt = now
        }
    }

    companion object {
        private const val WAKE_LOCK_TIMEOUT_MS = 60_000L
        private const val SKIP_THRESHOLD_METERS = 50.0
        private const val REQUEST_CODE_RESUME = 1001
        private const val REQUEST_CODE_PAUSE_START = 1002
        private const val REQUEST_CODE_PAUSE_GRACE = 1003
        private const val SCHEDULED_PAUSE_GRACE_SECONDS = 10L
        private const val SCHEDULED_PAUSE_CONFIRM_RETRY_SECONDS = 10L
        // Persisted "currently paused" flag (survives process death) — AppPreferences.getFlag.
        private const val FLAG_PAUSE_ACTIVE = "scheduled_pause_active"
        private val STATE_CACHE_TTL: Duration = Duration.ofSeconds(45)
        private val LOCATION_OPTIONS_TTL: Duration = Duration.ofMinutes(15)
        private val REAUTH_NOTIFICATION_COOLDOWN: Duration = Duration.ofHours(1)
    }
}
