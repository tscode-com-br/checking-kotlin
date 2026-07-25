package br.com.tscode.checking.platform.background

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import br.com.tscode.checking.data.local.AppPreferencesDataSource
import br.com.tscode.checking.i18n.resolveEffectiveLanguageCode
import br.com.tscode.checking.platform.activitylog.ActivityLogger
import br.com.tscode.checking.platform.background.notifications.AutoActivityNotifications
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

internal fun scheduledPauseTriggerForServiceAction(action: String?): OrchestratorTrigger? =
    when (action) {
        AutoActivityForegroundService.ACTION_PAUSE_START -> OrchestratorTrigger.PAUSE_START
        AutoActivityForegroundService.ACTION_PAUSE_END -> OrchestratorTrigger.PAUSE_END
        AutoActivityForegroundService.ACTION_PAUSE_GRACE -> OrchestratorTrigger.PAUSE_GRACE
        else -> null
    }

// Primary background engine for automatic activities (§23.3-1, T3B.2).
// foregroundServiceType="location" is declared in AndroidManifest.xml.
// START_STICKY ensures the OS restarts the service after a resource kill.
// The orchestrator (T3B.3) runs inside this service's coroutine scope.
@AndroidEntryPoint
class AutoActivityForegroundService : Service() {

    @Inject lateinit var appPrefs: AppPreferencesDataSource
    @Inject lateinit var orchestrator: BackgroundCheckOrchestrator
    @Inject lateinit var geofenceManager: GeofenceManager
    @Inject lateinit var activityLogger: ActivityLogger

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var timerJob: Job? = null
    private var isPromotedToForeground = false

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelfResult(startId)
            return START_NOT_STICKY
        }

        if (!isPromotedToForeground) {
            val origin =
                if (scheduledPauseTriggerForServiceAction(intent?.action) != null) {
                    // The exact-alarm action is already an unambiguous, backwards-compatible
                    // identifier. Older PendingIntents therefore remain safe after an app update.
                    AutoActivityServiceStartOrigin.SCHEDULED_PAUSE
                } else {
                    AutoActivityServiceStartOrigin.fromWireValue(
                        intent?.getStringExtra(EXTRA_AUTO_ACTIVITY_START_ORIGIN),
                    )
                }
            val prerequisites = inspectAutoActivityServicePrerequisites(this)
            val blockReason =
                autoActivityServiceStartBlockReason(
                    prerequisites = prerequisites,
                    origin = origin,
                )
            if (shouldWarnBackgroundLocationRequired(prerequisites, origin)) {
                AutoActivityNotifications.postBackgroundLocationRequiredNotification(
                    this,
                    resolveEffectiveLanguageCode(null),
                )
            } else if (prerequisites.backgroundLocationGranted) {
                AutoActivityNotifications.cancelBackgroundLocationRequiredNotification(this)
            }
            if (blockReason != null) {
                return rejectStart(
                    startId = startId,
                    logMessage = "Background service start deferred: ${blockReason.name}.",
                )
            }

            // Promote immediately, before DataStore/GPS/network work. The notification uses a
            // device-locale guess first and is refined asynchronously from the persisted language.
            val promotion =
                attemptAutoActivityForegroundPromotion {
                    val serviceType =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                        } else {
                            0
                        }
                    ServiceCompat.startForeground(
                        this,
                        AutoActivityNotifications.NOTIFICATION_ID_SERVICE,
                        AutoActivityNotifications.buildServiceNotification(
                            this,
                            isPaused = false,
                            lang = resolveEffectiveLanguageCode(null),
                        ),
                        serviceType,
                    )
                }
            if (promotion is AutoActivityForegroundPromotion.Rejected) {
                val latestPrerequisites = inspectAutoActivityServicePrerequisites(this)
                if (
                    promotion.cause is SecurityException &&
                    latestPrerequisites.sdkInt >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                    latestPrerequisites.fineLocationGranted &&
                    latestPrerequisites.locationEnabled &&
                    !latestPrerequisites.backgroundLocationGranted
                ) {
                    AutoActivityNotifications.postBackgroundLocationRequiredNotification(
                        this,
                        resolveEffectiveLanguageCode(null),
                    )
                }
                return rejectStart(
                    startId = startId,
                    logMessage =
                        "Background service promotion rejected: " +
                            "${promotion.cause.javaClass.simpleName}.",
                )
            }

            // This flag must represent a service that actually entered foreground state. Setting it
            // in onCreate() creates a false-positive window when Android rejects startForeground().
            isPromotedToForeground = true
            isRunning = true
            if (prerequisites.backgroundLocationGranted) {
                AutoActivityNotifications.cancelBackgroundLocationRequiredNotification(this)
            }
            activityLogger.logActive("Background service started.") // plan004 — engine awake
        }

        scope.launch {
            val lang = resolveEffectiveLanguageCode(appPrefs.language.first())
            updateNotification(isPaused = false, lang = lang)
            val chave = appPrefs.chave.first().ifEmpty { return@launch }
            geofenceManager.register(chave)
        }
        // Exact-alarm boundaries must force their own evaluation even when the normal 15-minute
        // timer job is already alive. These triggers wait for the orchestrator mutex and therefore
        // cannot be consumed by an overlapping GPS run.
        val boundaryTrigger = scheduledPauseTriggerForServiceAction(intent?.action)
        if (boundaryTrigger != null) {
            scope.launch { orchestrator.runOnce(boundaryTrigger) }
        }
        // Start the 15-min polling loop only once — guard against multiple onStartCommand calls.
        if (timerJob?.isActive != true) {
            timerJob = scope.launch {
                while (true) {
                    // Run IMMEDIATELY on (re)start, then every 15 min — eliminates the old 15-min
                    // dead window where nothing happened right after the FGS came up. The first tick
                    // has no skip-if-unchanged baseline (lastLat/lastLon == null), so shouldSkip()
                    // returns RUN and the evaluation actually executes. Single-flight is guaranteed
                    // by the Mutex inside runOnce(), so this never overlaps a geofence/foreground run.
                    orchestrator.runOnce(OrchestratorTrigger.TIMER)
                    delay(TIMER_INTERVAL_MS)
                }
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        val wasRunning = isPromotedToForeground
        isPromotedToForeground = false
        isRunning = false
        if (wasRunning) {
            activityLogger.logInactive("Background service stopped.") // plan004 — engine asleep
        }
        scope.cancel()
        super.onDestroy()
    }

    // Best-effort restart when the user swipes the app from recents.
    // The WorkManager backstop (T3B.8) is the authoritative restart mechanism.
    override fun onTaskRemoved(rootIntent: Intent?) {
        val prerequisites = inspectAutoActivityServicePrerequisites(applicationContext)
        if (
            shouldWarnBackgroundLocationRequired(
                prerequisites,
                AutoActivityServiceStartOrigin.TASK_REMOVED,
            )
        ) {
            AutoActivityNotifications.postBackgroundLocationRequiredNotification(
                applicationContext,
                resolveEffectiveLanguageCode(null),
            )
        }
        val restartBlockReason =
            autoActivityServiceStartBlockReason(
                prerequisites = prerequisites,
                origin = AutoActivityServiceStartOrigin.TASK_REMOVED,
            )
        if (restartBlockReason == null) {
            val restartIntent =
                Intent(applicationContext, AutoActivityForegroundService::class.java)
                    .putExtra(
                        EXTRA_AUTO_ACTIVITY_START_ORIGIN,
                        AutoActivityServiceStartOrigin.TASK_REMOVED.wireValue,
                    )
            val pendingIntent =
                PendingIntent.getService(
                    applicationContext,
                    0,
                    restartIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            (getSystemService(ALARM_SERVICE) as AlarmManager).setAndAllowWhileIdle(
                AlarmManager.ELAPSED_REALTIME,
                android.os.SystemClock.elapsedRealtime() + RESTART_DELAY_MS,
                pendingIntent,
            )
        } else {
            activityLogger.logWarning(
                "Task-removal service restart deferred: ${restartBlockReason.name}.",
            )
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // Called when the pause state changes (pause active/inactive).
    fun updateNotification(isPaused: Boolean, lang: String) {
        AutoActivityNotifications.updateServiceNotification(this, isPaused, lang)
    }

    private fun rejectStart(
        startId: Int,
        logMessage: String,
    ): Int {
        isPromotedToForeground = false
        isRunning = false
        activityLogger.logWarning(logMessage)
        stopSelfResult(startId)
        return START_NOT_STICKY
    }

    companion object {
        const val ACTION_STOP = "br.com.tscode.checking.AUTO_ACTIVITY_STOP"
        const val ACTION_PAUSE_START = "br.com.tscode.checking.SCHEDULED_PAUSE_START"
        const val ACTION_PAUSE_END = "br.com.tscode.checking.SCHEDULED_PAUSE_END"
        const val ACTION_PAUSE_GRACE = "br.com.tscode.checking.SCHEDULED_PAUSE_GRACE"
        private const val RESTART_DELAY_MS = 1_000L
        private const val TIMER_INTERVAL_MS = 15 * 60 * 1_000L

        // Visible to AutoActivityController.isRunning() — set only by this service.
        @Volatile var isRunning: Boolean = false
    }
}
