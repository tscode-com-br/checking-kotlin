package br.com.tscode.checking.platform.background

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import br.com.tscode.checking.data.local.AppPreferencesDataSource
import br.com.tscode.checking.i18n.resolveEffectiveLanguageCode
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

// Primary background engine for automatic activities (§23.3-1, T3B.2).
// foregroundServiceType="location" is declared in AndroidManifest.xml.
// START_STICKY ensures the OS restarts the service after a resource kill.
// The orchestrator (T3B.3) runs inside this service's coroutine scope.
@AndroidEntryPoint
class AutoActivityForegroundService : Service() {

    @Inject lateinit var appPrefs: AppPreferencesDataSource
    @Inject lateinit var orchestrator: BackgroundCheckOrchestrator
    @Inject lateinit var geofenceManager: GeofenceManager

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var timerJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        isRunning = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        // startForeground() must be called quickly — use a device-locale guess (no DataStore read),
        // then refine once the persisted language is read. Both follow the same stored→device→pt rule
        // the UI uses, so the service notification matches the user's language instead of forcing pt.
        startForeground(
            AutoActivityNotifications.NOTIFICATION_ID_SERVICE,
            AutoActivityNotifications.buildServiceNotification(this, isPaused = false, lang = resolveEffectiveLanguageCode(null)),
        )
        scope.launch {
            val lang = resolveEffectiveLanguageCode(appPrefs.language.first())
            updateNotification(isPaused = false, lang = lang)
            val chave = appPrefs.chave.first().ifEmpty { return@launch }
            geofenceManager.register(chave)
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
        isRunning = false
        scope.cancel()
        super.onDestroy()
    }

    // Best-effort restart when the user swipes the app from recents.
    // The WorkManager backstop (T3B.8) is the authoritative restart mechanism.
    override fun onTaskRemoved(rootIntent: Intent?) {
        val restartIntent = Intent(applicationContext, AutoActivityForegroundService::class.java)
        val pendingIntent = PendingIntent.getService(
            applicationContext, 0, restartIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        (getSystemService(ALARM_SERVICE) as AlarmManager).setAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME,
            android.os.SystemClock.elapsedRealtime() + RESTART_DELAY_MS,
            pendingIntent,
        )
        super.onTaskRemoved(rootIntent)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // Called when the pause state changes (pause active/inactive).
    fun updateNotification(isPaused: Boolean, lang: String) {
        AutoActivityNotifications.updateServiceNotification(this, isPaused, lang)
    }

    companion object {
        const val ACTION_STOP = "br.com.tscode.checking.AUTO_ACTIVITY_STOP"
        private const val RESTART_DELAY_MS = 1_000L
        private const val TIMER_INTERVAL_MS = 15 * 60 * 1_000L

        // Visible to AutoActivityController.isRunning() — set only by this service.
        @Volatile var isRunning: Boolean = false
    }
}
