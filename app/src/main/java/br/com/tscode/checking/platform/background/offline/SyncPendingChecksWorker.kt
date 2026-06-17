package br.com.tscode.checking.platform.background.offline

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

// Drains the offline check queue (P8) when connectivity returns. Enqueued (network-constrained) on
// every OfflineCheckQueue.enqueue and once on login, and re-run by WorkManager with backoff.
// All replay logic lives in PendingCheckReplayer (unit tested); this worker is a thin delegator.
@HiltWorker
class SyncPendingChecksWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val replayer: PendingCheckReplayer,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = when (replayer.drain()) {
        PendingCheckReplayer.DrainResult.COMPLETED -> Result.success()
        PendingCheckReplayer.DrainResult.RETRY -> Result.retry()
    }

    companion object {
        private const val WORK_NAME = "sync_pending_checks"

        fun enqueue(context: Context) {
            val request = OneTimeWorkRequestBuilder<SyncPendingChecksWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build(),
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()
            // REPLACE: each enqueue restarts a full drain after the latest event. Cancelling a
            // running drain mid-submit is safe — replays are idempotent (dedup by client_event_id).
            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request,
            )
        }
    }
}
