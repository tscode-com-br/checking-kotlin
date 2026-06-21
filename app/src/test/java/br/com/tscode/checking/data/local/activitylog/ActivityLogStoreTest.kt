package br.com.tscode.checking.data.local.activitylog

import br.com.tscode.checking.domain.model.ActivityActor
import br.com.tscode.checking.domain.model.ActivityKind
import br.com.tscode.checking.domain.model.ActivityLogEntry
import br.com.tscode.checking.domain.model.ActivitySeverity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

// plan004 EP5 — ActivityLog store over a fake DAO (JVM): prune-on-write args + newest-first row↔entry mapping.
class ActivityLogStoreTest {

    private class FakeDao : ActivityLogDao {
        val rows = mutableListOf<ActivityLogRow>()
        var lastDeleteOlderThan: Long? = null
        var lastTrimMax: Int? = null
        private var nextId = 1L

        override suspend fun insert(row: ActivityLogRow): Long {
            val withId = row.copy(id = nextId++)
            rows.add(withId)
            return withId.id
        }

        override suspend fun pageNewestFirst(limit: Int, offset: Int): List<ActivityLogRow> =
            rows.sortedWith(compareByDescending<ActivityLogRow> { it.atEpochMs }.thenByDescending { it.id })
                .drop(offset).take(limit)

        override suspend fun count(): Int = rows.size

        override suspend fun deleteOlderThan(epochMs: Long): Int {
            lastDeleteOlderThan = epochMs
            val before = rows.size
            rows.removeAll { it.atEpochMs < epochMs }
            return before - rows.size
        }

        override suspend fun trimToMax(max: Int): Int {
            lastTrimMax = max
            if (rows.size <= max) return 0
            val keep = rows.sortedWith(compareByDescending<ActivityLogRow> { it.atEpochMs }.thenByDescending { it.id })
                .take(max).toSet()
            val before = rows.size
            rows.retainAll(keep)
            return before - rows.size
        }

        override suspend fun clearAll() {
            rows.clear()
        }
    }

    private fun entry(at: Instant, desc: String = "d") = ActivityLogEntry(
        at = at, actor = ActivityActor.SYS, kind = ActivityKind.ACTIVE,
        severity = ActivitySeverity.INFO, description = desc, location = null,
    )

    @Test
    fun record_inserts_and_prunes_on_write() = runTest {
        val dao = FakeDao()
        val store = ActivityLog(dao)
        val at = Instant.parse("2026-06-19T10:00:00Z")
        store.record(entry(at))
        assertEquals(1, dao.rows.size)
        assertEquals(at.toEpochMilli() - ActivityLog.RETENTION_MS, dao.lastDeleteOlderThan)
        assertEquals(ActivityLog.MAX_ROWS, dao.lastTrimMax)
    }

    @Test
    fun page_maps_rows_to_entries_newest_first() = runTest {
        val dao = FakeDao()
        val store = ActivityLog(dao)
        store.record(entry(Instant.parse("2026-06-19T10:00:00Z"), "older"))
        store.record(entry(Instant.parse("2026-06-19T11:00:00Z"), "newer"))
        val page = store.page(0, 30)
        assertEquals(2, page.size)
        assertEquals("newer", page.first().description) // newest first
        assertEquals(ActivityKind.ACTIVE, page.first().kind)
        assertEquals(ActivitySeverity.INFO, page.first().severity)
        assertEquals(ActivityActor.SYS, page.first().actor)
    }

    // TP2 — pin the retention/cap constants so a future regression (e.g. dropping the `L` and overflowing
    // the 30-day-in-millis value past Int.MAX_VALUE) fails fast and visibly.
    @Test
    fun retention_and_cap_constants_are_pinned() {
        assertEquals(30L, ActivityLog.RETENTION_DAYS)
        assertEquals(2_592_000_000L, ActivityLog.RETENTION_MS) // 30 * 24 * 60 * 60 * 1000, as Long
        assertEquals(5_000, ActivityLog.MAX_ROWS)
        assertEquals(30, ActivityLog.PAGE_SIZE)
    }
}
