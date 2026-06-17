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
import br.com.tscode.checking.domain.checkrules.isScheduledPauseActiveNow
import br.com.tscode.checking.domain.checkrules.nextPauseStartInstant
import br.com.tscode.checking.domain.checkrules.nextResumeInstant
import br.com.tscode.checking.domain.clientstate.resolvePersistedUserSettings
import br.com.tscode.checking.domain.clientstate.UserSettings
import br.com.tscode.checking.domain.model.HistoryState
import br.com.tscode.checking.domain.model.LocationOptions
import br.com.tscode.checking.domain.model.UserProjects
import br.com.tscode.checking.domain.repository.AccidentRepository
import br.com.tscode.checking.domain.repository.AuthRepository
import br.com.tscode.checking.domain.repository.CheckRepository
import br.com.tscode.checking.domain.usecase.AutoActivitiesResult
import br.com.tscode.checking.domain.usecase.RunAutomaticActivitiesUseCase
import br.com.tscode.checking.i18n.DEFAULT_LANGUAGE
import br.com.tscode.checking.platform.background.diagnostics.EvaluationEntry
import br.com.tscode.checking.platform.background.diagnostics.EvaluationLog
import br.com.tscode.checking.platform.background.diagnostics.EvaluationOutcome
import br.com.tscode.checking.BuildConfig
import br.com.tscode.checking.platform.background.notifications.AutoActivityNotifications
import br.com.tscode.checking.platform.location.LocationCapture
import br.com.tscode.checking.platform.location.LocationMeasurementCollector
import br.com.tscode.checking.platform.location.LocationProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.serialization.json.Json
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

enum class OrchestratorTrigger { TIMER, GEOFENCE, FOREGROUND }

private enum class SkipDecision { RUN, SKIP, NO_FIX }

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
) {
    private val mutex = Mutex()
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

    // Entry point — single-flight.  Returns immediately if another run is in progress.
    // On 401, attempts a silent re-login (§23.5) and retries the run once.
    suspend fun runOnce(trigger: OrchestratorTrigger) {
        if (!mutex.tryLock()) return
        try {
            wakeLock.acquire(WAKE_LOCK_TIMEOUT_MS)
            isSessionExpired = false
            runOnceLocked(trigger)
            if (isSessionExpired) {
                val chave = appPrefs.chave.first().ifEmpty { return }
                val lang = appPrefs.language.first().ifEmpty { DEFAULT_LANGUAGE }
                if (attemptSilentRelogin(chave, lang)) {
                    isSessionExpired = false
                    runOnceLocked(trigger) // retry once after successful re-login
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
            val lang = appPrefs.language.first().ifEmpty { DEFAULT_LANGUAGE }
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
    private suspend fun runOnceLocked(trigger: OrchestratorTrigger) {
        // Step 1: Auth — chave must be present (session cookie persisted by OkHttp).
        // 401 during network calls sets isSessionExpired; runOnce() handles the retry.
        val chave = appPrefs.chave.first().ifEmpty { return }
        val lang = appPrefs.language.first().ifEmpty { DEFAULT_LANGUAGE }

        // Step 2: Toggle + scheduled pause
        val rawJson = appPrefs.userSettingsJson.first()
        val settingsMap: Map<String, UserSettings?> = runCatching {
            settingsJson.decodeFromString<Map<String, UserSettings?>>(rawJson)
        }.getOrElse { emptyMap() }
        val userSettings = resolvePersistedUserSettings(settingsMap, chave)

        // Accident push — checked before the toggle/pause gates so the user is alerted to an
        // accident in any project regardless of automatic-activities state.
        maybeNotifyAccident(chave, userSettings.notifyAccident, lang)

        if (!userSettings.automaticActivitiesEnabled) {
            EvaluationLog.record(EvaluationEntry(clock.now(), trigger, null, null, null, EvaluationOutcome.TOGGLE_OFF))
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
        // "Was paused" is PERSISTED, so a transition isn't lost if the process was killed
        // (e.g. the exact alarm wakes a fresh process to fire the pause-end notification).
        val wasPaused = appPrefs.getFlag(FLAG_PAUSE_ACTIVE).first()
        if (isScheduledPauseActiveNow(nowZdt, pauseSettings)) {
            // Transition INTO pause — notify once and persist the paused state.
            if (!wasPaused) {
                if (userSettings.notifyScheduledPause) {
                    AutoActivityNotifications.postScheduledPauseTransition(context, started = true, lang = lang)
                }
                appPrefs.setFlag(FLAG_PAUSE_ACTIVE, true)
            }
            handleScheduledPause(pauseSettings, lang)
            EvaluationLog.record(EvaluationEntry(clock.now(), trigger, null, null, null, EvaluationOutcome.PAUSED))
            return
        }
        // Not paused now — if we were paused, the pause just ended: notify once and clear state.
        if (wasPaused) {
            if (userSettings.notifyScheduledPause) {
                AutoActivityNotifications.postScheduledPauseTransition(context, started = false, lang = lang)
            }
            appPrefs.setFlag(FLAG_PAUSE_ACTIVE, false)
        }
        // Schedule an exact alarm for the NEXT pause start so it fires precisely, not lazily on
        // the next 15-min tick. (Cancels any stale start alarm when no pause is configured.)
        scheduleStartAlarm(nextPauseStartInstant(nowZdt, pauseSettings))

        // Fetch location options (accuracy threshold + mixed-zone interval) — cached 15 min
        val options = getLocationOptions() ?: return

        // Step 3: Skip-if-unchanged (TIMER ticks only — geofence/foreground always run)
        if (trigger == OrchestratorTrigger.TIMER) {
            lastCaptureAccuracyMeters = null
            val skipDecision = shouldSkip(options.accuracyThresholdMeters)
            if (BuildConfig.DEBUG) {
                lastCaptureAccuracyMeters?.let { LocationMeasurementCollector.record(trigger, it) }
            }
            if (skipDecision == SkipDecision.SKIP) {
                EvaluationLog.record(EvaluationEntry(clock.now(), trigger, lastCaptureAccuracyMeters, null, null, EvaluationOutcome.SKIP))
                return
            }
        }

        // Steps 4–6: GPS → POST /check/location → GET /check/state → Situation engine → submit
        val currentState = getRemoteState(chave)
        val userProjects = UserProjects(
            projects = userSettings.projects,
            activeProject = userSettings.activeProject,
        )

        val result = runAutomaticActivitiesUseCase(
            chave = chave,
            userProjects = userProjects,
            currentState = currentState,
            mixedZoneIntervalMinutes = options.mixedZoneIntervalMinutes,
            accuracyThresholdMeters = options.accuracyThresholdMeters,
        )

        // Update state cache on success so the next tick has a fresh baseline
        if (result is AutoActivitiesResult.Submitted) {
            cachedState = result.newState
            cacheChave = chave
            cachedStateAt = clock.now()
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
                if (r.error is ApiError.Unauthorized) isSessionExpired = true
                null
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

    private fun wakePendingIntent(requestCode: Int): PendingIntent =
        PendingIntent.getService(
            context, requestCode,
            Intent(context, AutoActivityForegroundService::class.java),
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
            return false
        }
        return when (authRepository.login(chave, password)) {
            is AppResult.Success -> true
            is AppResult.Failure -> {
                postReauthNotificationCoalesced(lang)
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
        // Persisted "currently paused" flag (survives process death) — AppPreferences.getFlag.
        private const val FLAG_PAUSE_ACTIVE = "scheduled_pause_active"
        private val STATE_CACHE_TTL: Duration = Duration.ofSeconds(45)
        private val LOCATION_OPTIONS_TTL: Duration = Duration.ofMinutes(15)
        private val REAUTH_NOTIFICATION_COOLDOWN: Duration = Duration.ofHours(1)
    }
}
