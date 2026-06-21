package br.com.tscode.checking.data.local.activitylog

import br.com.tscode.checking.domain.model.ActivityActor
import br.com.tscode.checking.domain.model.ActivityKind
import br.com.tscode.checking.domain.model.ActivityLogEntry
import br.com.tscode.checking.domain.model.ActivitySeverity
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * plan004 §3.3 — the Activities log store. Wraps [ActivityLogDao]: records entries (pruning on write to
 * "30 days OR 5,000 rows"), pages newest-first in blocks of 30, counts, clears. Maps row↔domain. Suspend
 * functions only — threading is the caller's (the ActivityLogger persists off the app scope; Room runs its
 * DAO on its own executor). The DB is isolated from all existing storage.
 */
@Singleton
class ActivityLog @Inject constructor(
    private val dao: ActivityLogDao,
) {
    suspend fun record(entry: ActivityLogEntry) {
        dao.insert(entry.toRow())
        // Prune on write: keep the last 30 days AND at most 5,000 rows (whichever is smaller).
        dao.deleteOlderThan(entry.at.toEpochMilli() - RETENTION_MS)
        dao.trimToMax(MAX_ROWS)
    }

    suspend fun page(offset: Int, limit: Int = PAGE_SIZE): List<ActivityLogEntry> =
        dao.pageNewestFirst(limit, offset).map { it.toEntry() }

    suspend fun count(): Int = dao.count()

    suspend fun clear() = dao.clearAll()

    companion object {
        const val PAGE_SIZE = 30
        const val MAX_ROWS = 5_000
        const val RETENTION_DAYS = 30L
        const val RETENTION_MS = RETENTION_DAYS * 24L * 60L * 60L * 1_000L
    }
}

private fun ActivityLogEntry.toRow() = ActivityLogRow(
    atEpochMs = at.toEpochMilli(),
    actor = actor.name,
    kind = kind.name,
    severity = severity.name,
    description = description,
    location = location,
)

private fun ActivityLogRow.toEntry() = ActivityLogEntry(
    at = Instant.ofEpochMilli(atEpochMs),
    actor = ActivityActor.valueOf(actor),
    kind = ActivityKind.valueOf(kind),
    severity = ActivitySeverity.valueOf(severity),
    description = description,
    location = location,
)
