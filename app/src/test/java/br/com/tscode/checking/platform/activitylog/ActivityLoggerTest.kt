package br.com.tscode.checking.platform.activitylog

import br.com.tscode.checking.core.time.Clock
import br.com.tscode.checking.data.local.activitylog.ActivityLog
import br.com.tscode.checking.data.local.activitylog.ActivityLogDao
import br.com.tscode.checking.data.local.activitylog.ActivityLogRow
import br.com.tscode.checking.domain.model.ActivityActor
import br.com.tscode.checking.domain.model.ActivityKind
import br.com.tscode.checking.domain.model.ActivitySeverity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

// plan004 EP5 — ActivityLogger façade (JVM): exact English descriptions + kind/severity/actor mapping,
// verbose gating, and crash-proofing (a throwing persist must never propagate). Uses a real ActivityLog
// over a fake DAO (no mocking of a final class) + an unconfined scope so the off-thread write runs eagerly.
@OptIn(ExperimentalCoroutinesApi::class)
class ActivityLoggerTest {

    private val fixedNow = Instant.parse("2026-06-19T10:00:00Z")
    private val clock = object : Clock {
        override fun now(): Instant = fixedNow
    }

    private class FakeDao(private val throwOnInsert: Boolean = false) : ActivityLogDao {
        val rows = mutableListOf<ActivityLogRow>()
        override suspend fun insert(row: ActivityLogRow): Long {
            if (throwOnInsert) throw RuntimeException("boom")
            rows.add(row)
            return rows.size.toLong()
        }
        override suspend fun pageNewestFirst(limit: Int, offset: Int): List<ActivityLogRow> = rows.takeLast(limit)
        override suspend fun count(): Int = rows.size
        override suspend fun deleteOlderThan(epochMs: Long): Int = 0
        override suspend fun trimToMax(max: Int): Int = 0
        override suspend fun clearAll() {}
    }

    private fun TestScope.logger(dao: ActivityLogDao) =
        ActivityLogger(clock, ActivityLog(dao), CoroutineScope(UnconfinedTestDispatcher(testScheduler)))

    @Test
    fun checkIn_checkOut_map_exact_descriptions_and_fields() = runTest {
        val dao = FakeDao()
        val logger = logger(dao)

        logger.logCheckIn(ActivityActor.USER, "Gate 3", success = true)
        with(dao.rows.last()) {
            assertEquals("Check-in at Gate 3.", description)
            assertEquals("CHECK_IN", kind)
            assertEquals("SUCCESS", severity)
            assertEquals("USER", actor)
            assertEquals("Gate 3", location)
        }

        logger.logCheckOut(ActivityActor.SYS, "Gate 3", success = false)
        with(dao.rows.last()) {
            assertEquals("Check-out failed at Gate 3.", description)
            assertEquals("CHECK_OUT", kind)
            assertEquals("FAILURE", severity)
            assertEquals("SYS", actor)
        }
    }

    @Test
    fun active_inactive_required_exact_strings_plus_optional_detail() = runTest {
        val dao = FakeDao()
        val logger = logger(dao)

        logger.logActive()
        assertEquals("Checking is now active.", dao.rows.last().description)
        assertEquals("ACTIVE", dao.rows.last().kind)
        assertEquals("INFO", dao.rows.last().severity)

        logger.logInactive()
        assertEquals("Checking is now inactive.", dao.rows.last().description)

        logger.logActive("Scheduled pause ended.")
        assertEquals("Checking is now active. (Scheduled pause ended.)", dao.rows.last().description)
    }

    @Test
    fun verbose_off_mutes_trigger_on_restores() = runTest {
        val dao = FakeDao()
        val logger = logger(dao)

        logger.verbose = false
        logger.logTrigger("TIMER")
        assertEquals(0, dao.rows.size) // muted

        logger.verbose = true
        logger.logTrigger("TIMER")
        assertEquals(1, dao.rows.size)
        assertEquals("Background evaluation (TIMER).", dao.rows.last().description)
    }

    @Test
    fun crashProof_persistThrows_neverPropagates() = runTest {
        val dao = FakeDao(throwOnInsert = true)
        val logger = logger(dao)
        // Must NOT throw even though the DAO insert throws (golden rule 2). Nothing persisted, no crash.
        logger.logCheckIn(ActivityActor.USER, "Gate 3", success = true)
        logger.logError("any error")
        assertEquals(0, dao.rows.size)
    }

    // ── TP3.1 — exhaustive per-helper mapping (the remaining helpers beyond check-in/out + active/inactive) ─

    @Test
    fun offline_sync_helpers_map_exact_descriptions_and_fields() = runTest {
        val dao = FakeDao()
        val logger = logger(dao)

        logger.logQueuedOffline(ActivityActor.USER, ActivityKind.CHECK_IN, "Gate 3")
        with(dao.rows.last()) {
            assertEquals("Check-in queued (offline) at Gate 3.", description)
            assertEquals("SYNC", kind); assertEquals("WARNING", severity); assertEquals("USER", actor)
            assertEquals("Gate 3", location)
        }
        logger.logQueuedOffline(ActivityActor.SYS, ActivityKind.CHECK_OUT, "Gate 3")
        with(dao.rows.last()) {
            assertEquals("Check-out queued (offline) at Gate 3.", description); assertEquals("SYS", actor)
        }
        logger.logSyncing(3)
        with(dao.rows.last()) {
            assertEquals("Syncing 3 queued event(s).", description)
            assertEquals("SYNC", kind); assertEquals("INFO", severity); assertEquals("SYS", actor)
        }
        logger.logSynced(ActivityKind.CHECK_IN, "Gate 3")
        with(dao.rows.last()) {
            assertEquals("Queued check-in synced at Gate 3.", description)
            assertEquals("SYNC", kind); assertEquals("SUCCESS", severity)
        }
        logger.logSyncDropped(ActivityKind.CHECK_OUT)
        with(dao.rows.last()) {
            assertEquals("Queued check-out dropped (invalid).", description)
            assertEquals("SYNC", kind); assertEquals("FAILURE", severity)
        }
    }

    @Test
    fun background_suite_helpers_map_kind_severity_actor() = runTest {
        val dao = FakeDao()
        val logger = logger(dao)

        logger.logTrigger("TIMER") // verbose default on
        with(dao.rows.last()) {
            assertEquals("Background evaluation (TIMER).", description)
            assertEquals("TRIGGER", kind); assertEquals("INFO", severity); assertEquals("SYS", actor)
        }
        logger.logLocation("Location fixed (±12m) → Gate 3.", "Gate 3")
        with(dao.rows.last()) {
            assertEquals("LOCATION", kind); assertEquals("INFO", severity); assertEquals("Gate 3", location)
        }
        logger.logLocation("Location accuracy too low (±80m).", null, ActivitySeverity.WARNING)
        with(dao.rows.last()) { assertEquals("LOCATION", kind); assertEquals("WARNING", severity) }
        logger.logAuth("Session refreshed.")
        with(dao.rows.last()) { assertEquals("AUTH", kind); assertEquals("INFO", severity); assertEquals("SYS", actor) }
        logger.logSystem("App started.")
        with(dao.rows.last()) { assertEquals("SYSTEM", kind); assertEquals("INFO", severity) }
        logger.logSystem("Automatic activities are OFF.", ActivitySeverity.WARNING)
        with(dao.rows.last()) { assertEquals("SYSTEM", kind); assertEquals("WARNING", severity) }
        logger.logWarning("Location permission revoked — auto disabled.")
        with(dao.rows.last()) { assertEquals("SYSTEM", kind); assertEquals("WARNING", severity) }
        logger.logError("Sign-in failed.")
        with(dao.rows.last()) { assertEquals("ERROR", kind); assertEquals("FAILURE", severity); assertEquals("SYS", actor) }
    }

    @Test
    fun unknownLocation_fallback_inDescriptions() = runTest {
        val dao = FakeDao()
        val logger = logger(dao)
        logger.logCheckIn(ActivityActor.SYS, null, success = true)
        assertEquals("Check-in at an unknown location.", dao.rows.last().description)
        logger.logCheckOut(ActivityActor.USER, "   ", success = true) // blank → fallback
        assertEquals("Check-out at an unknown location.", dao.rows.last().description)
        logger.logQueuedOffline(ActivityActor.SYS, ActivityKind.CHECK_IN, null)
        assertEquals("Check-in queued (offline) at an unknown location.", dao.rows.last().description)
    }

    // ── TP3.4 — verbose gates ONLY the high-frequency trigger; core rows always log ──────────────────────
    @Test
    fun verboseOff_mutesOnlyTrigger_coreStillLogs() = runTest {
        val dao = FakeDao()
        val logger = logger(dao)
        logger.verbose = false
        logger.logTrigger("TIMER") // muted
        assertEquals(0, dao.rows.size)
        logger.logCheckIn(ActivityActor.USER, "Gate 3", success = true) // core — always logs
        logger.logActive()
        logger.logError("boom")
        assertEquals(3, dao.rows.size)
    }

    // TP3.3 — the EXACT calls CheckViewModel.onSubmit makes on the MANUAL (USER) path map to USER rows.
    // The full VM-through-onSubmit end-to-end needs an AUTHENTICATED state (canSubmit requires it), which
    // invokes Android statics (PermissionLadder/AutoActivityController) — disproportionate/fragile for a JVM
    // unit test (see CheckViewModelForegroundTest's documented boundary). So the manual path is covered by:
    // (a) this logger-seam mapping of its exact calls; (b) crashProof_* (a throwing record never propagates,
    // so onSubmit's logCheckOut can never break a submit); (c) the EP6 compile-verified onSubmit wiring;
    // (d) the AUTOMATIC path end-to-end (RunAutomaticActivitiesLoggingTest); (e) on-device manual smoke (TP4).
    @Test
    fun manual_submit_calls_map_to_user_rows() = runTest {
        val dao = FakeDao()
        val logger = logger(dao)

        logger.logCheckOut(ActivityActor.USER, "Desconhecido", success = true)
        with(dao.rows.last()) {
            assertEquals("Check-out at Desconhecido.", description)
            assertEquals("CHECK_OUT", kind); assertEquals("SUCCESS", severity); assertEquals("USER", actor)
        }
        logger.logCheckOut(ActivityActor.USER, "Desconhecido", success = false)
        with(dao.rows.last()) {
            assertEquals("Check-out failed at Desconhecido.", description)
            assertEquals("FAILURE", severity); assertEquals("USER", actor)
        }
        logger.logQueuedOffline(ActivityActor.USER, ActivityKind.CHECK_OUT, "Desconhecido")
        with(dao.rows.last()) {
            assertEquals("Check-out queued (offline) at Desconhecido.", description)
            assertEquals("SYNC", kind); assertEquals("WARNING", severity); assertEquals("USER", actor)
        }
        logger.logError("Session expired — sign in again.")
        with(dao.rows.last()) {
            assertEquals("Session expired — sign in again.", description)
            assertEquals("ERROR", kind); assertEquals("FAILURE", severity)
        }
    }

    // ── TP3.2 — a throwing persist must NEVER propagate out of ANY helper (golden rule 2) ────────────────
    @Test
    fun crashProof_allHelpers_neverPropagate() = runTest {
        val dao = FakeDao(throwOnInsert = true)
        val logger = logger(dao)
        logger.logQueuedOffline(ActivityActor.SYS, ActivityKind.CHECK_IN, "x")
        logger.logSyncing(2)
        logger.logSynced(ActivityKind.CHECK_OUT, "x")
        logger.logSyncDropped(ActivityKind.CHECK_IN)
        logger.logLocation("x")
        logger.logAuth("x")
        logger.logSystem("x")
        logger.logWarning("x")
        logger.logActive("x")
        logger.logInactive("x")
        assertEquals(0, dao.rows.size)
    }
}
