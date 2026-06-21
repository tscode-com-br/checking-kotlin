package br.com.tscode.checking.platform.background

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import br.com.tscode.checking.domain.model.ActivitySeverity
import br.com.tscode.checking.platform.activitylog.ActivityLogger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

// WorkManager periodic backstop (§23.3-3, T3B.8).
// Runs every 15 min (Android's floor) — the same interval as the FGS internal timer.
// Purpose: (1) restart the FGS if it was killed; (2) run the same orchestrator routine
// so the 15-min check fires even when the FGS is dead.
// The FGS timer is the primary driver; this worker is the safety net.
@HiltWorker
class AutoActivityWatchdogWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val orchestrator: BackgroundCheckOrchestrator,
    private val activityLogger: ActivityLogger,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        // Restart the FGS if it was killed by the OS.
        if (!AutoActivityForegroundService.isRunning) {
            AutoActivityController.start(applicationContext)
            activityLogger.logSystem("Watchdog restarted the background service.", ActivitySeverity.WARNING) // plan004
        } else {
            activityLogger.logSystem("Watchdog check: service healthy.") // plan004
        }
        // Run the same backstop evaluation the FGS timer would have run.
        orchestrator.runOnce(OrchestratorTrigger.TIMER)
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "auto_activity_watchdog"
        private const val INTERVAL_MINUTES = 15L

        // Enqueue the periodic watchdog, or keep an existing one.
        // KEEP = don't reset the timer if a work request is already scheduled,
        // preserving the cadence across multiple start() calls.
        fun enqueue(context: Context) {
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<AutoActivityWatchdogWorker>(
                    INTERVAL_MINUTES, TimeUnit.MINUTES,
                ).build(),
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
