package br.com.tscode.checking.platform.background.offline

import android.content.Context
import br.com.tscode.checking.data.local.AppPreferencesDataSource
import br.com.tscode.checking.domain.offline.PendingCheckEvent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

// Persistent FIFO of check events captured while offline (P8). Backed by a JSON list in DataStore
// (no Room — the queue is tiny and the app already serializes JSON blobs to DataStore). Mutations
// are serialized by a Mutex so a manual enqueue can't race the sync worker's removals.
//
// Every enqueue schedules SyncPendingChecksWorker (network-constrained), so events drain as soon
// as connectivity returns — including across process death and reboot (WorkManager persists it).
@Singleton
class OfflineCheckQueue @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appPrefs: AppPreferencesDataSource,
) {
    private val mutex = Mutex()
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun enqueue(event: PendingCheckEvent) {
        mutex.withLock {
            val list = readList().toMutableList()
            // Replace any prior entry with the same id (re-enqueue is idempotent).
            list.removeAll { it.clientEventId == event.clientEventId }
            list.add(event)
            // Cap the queue (keep the most recent by capture time) so a device offline for days
            // can't grow it without bound. Oldest unsynced events are dropped first.
            val capped = list.sortedBy { it.capturedAtEpochMs }.takeLast(MAX_EVENTS)
            writeList(capped)
        }
        SyncPendingChecksWorker.enqueue(context)
    }

    // Snapshot in capture order (oldest first) — replays must preserve real-world ordering so the
    // server state evolves correctly (e.g. a check-in then its later check-out).
    suspend fun peekAll(): List<PendingCheckEvent> =
        mutex.withLock { readList().sortedBy { it.capturedAtEpochMs } }

    suspend fun remove(clientEventId: String) {
        mutex.withLock {
            writeList(readList().filterNot { it.clientEventId == clientEventId })
        }
    }

    suspend fun size(): Int = mutex.withLock { readList().size }

    private suspend fun readList(): List<PendingCheckEvent> {
        val raw = appPrefs.pendingChecksJson.first()
        if (raw.isEmpty()) return emptyList()
        return runCatching { json.decodeFromString<List<PendingCheckEvent>>(raw) }.getOrElse { emptyList() }
    }

    private suspend fun writeList(list: List<PendingCheckEvent>) {
        appPrefs.setPendingChecksJson(if (list.isEmpty()) "" else json.encodeToString(list))
    }

    companion object {
        private const val MAX_EVENTS = 200
    }
}
