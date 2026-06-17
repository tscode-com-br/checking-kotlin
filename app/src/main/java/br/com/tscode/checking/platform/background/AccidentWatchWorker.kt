package br.com.tscode.checking.platform.background

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

// Dedicated periodic accident watcher — INDEPENDENT of the automatic-activities engine.
// Scheduled whenever the user is authenticated (regardless of the automatic-activities
// toggle), so the user is alerted to an accident in any project even when they only use
// manual check-in. Runs every 15 min (Android's WorkManager floor); in Doze it may be
// deferred to a maintenance window. (Truly instant delivery would require a server push.)
@HiltWorker
class AccidentWatchWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val orchestrator: BackgroundCheckOrchestrator,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        orchestrator.runAccidentCheck()
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "accident_watch"
        private const val INTERVAL_MINUTES = 15L

        // Idempotent — KEEP preserves the cadence across repeated enqueue() calls.
        // WorkManager persists this across process death and reboot on its own.
        fun enqueue(context: Context) {
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<AccidentWatchWorker>(
                    INTERVAL_MINUTES, TimeUnit.MINUTES,
                ).build(),
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
