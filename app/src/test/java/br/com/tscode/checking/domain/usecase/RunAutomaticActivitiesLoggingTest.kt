package br.com.tscode.checking.domain.usecase

import br.com.tscode.checking.core.error.ApiError
import br.com.tscode.checking.core.result.AppResult
import br.com.tscode.checking.core.time.Clock
import br.com.tscode.checking.data.local.activitylog.ActivityLog
import br.com.tscode.checking.data.local.activitylog.ActivityLogDao
import br.com.tscode.checking.data.local.activitylog.ActivityLogRow
import br.com.tscode.checking.domain.model.CheckAction
import br.com.tscode.checking.domain.model.HistoryState
import br.com.tscode.checking.domain.model.LocationMatch
import br.com.tscode.checking.domain.model.MatchStatus
import br.com.tscode.checking.domain.model.UserProjects
import br.com.tscode.checking.platform.activitylog.ActivityLogger
import br.com.tscode.checking.platform.background.offline.OfflineCheckQueue
import br.com.tscode.checking.domain.repository.CheckRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

/**
 * plan004 EP6 — automatic check-in/out instrumentation, end-to-end through the REAL ActivityLogger +
 * store over a capturing DAO (no mocking of the final logger). Asserts the right activity row is recorded
 * for success / offline-queue / failure, AND that a logging/persistence failure NEVER changes the
 * use-case result (golden rule 2). Mirrors RunAutomaticActivitiesOfflineTest's location/match setup.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RunAutomaticActivitiesLoggingTest {

    private val chave = "HR70"
    private val projeto = "P80"
    private val projects = UserProjects(projects = listOf(projeto), activeProject = projeto)

    private val captureLocationUseCase = mockk<CaptureLocationUseCase>()
    private val checkRepository = mockk<CheckRepository>()
    private val offlineQueue = mockk<OfflineCheckQueue>(relaxed = true)
    private val clock = mockk<Clock> { every { now() } returns Instant.parse("2026-06-16T12:00:00Z") }

    private class CapturingDao(private val throwOnInsert: Boolean = false) : ActivityLogDao {
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

    private fun match(status: MatchStatus, resolvedLocal: String? = null) = LocationMatch(
        matched = status == MatchStatus.MATCHED,
        resolvedLocal = resolvedLocal,
        label = resolvedLocal ?: "",
        status = status,
        message = "",
        accuracyMeters = 10.0,
        accuracyThresholdMeters = 50,
        minimumCheckoutDistanceMeters = 2000,
        nearestWorkplaceDistanceMeters = null,
    )

    private fun history(last: CheckAction?) = HistoryState(
        found = true, chave = chave, projeto = projeto, currentAction = last, currentLocal = null,
        hasCurrentDayCheckin = last == CheckAction.CHECKIN,
        lastCheckinAt = if (last == CheckAction.CHECKIN) Instant.now() else null,
        lastCheckoutAt = if (last == CheckAction.CHECKOUT) Instant.now() else null,
        transportEnabled = false,
    )

    private fun TestScope.useCaseWith(dao: CapturingDao): RunAutomaticActivitiesUseCase {
        val logger = ActivityLogger(clock, ActivityLog(dao), CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
        return RunAutomaticActivitiesUseCase(captureLocationUseCase, checkRepository, offlineQueue, clock, logger)
    }

    @Test
    fun automatic_success_logs_check_in_success_row() = runTest {
        val dao = CapturingDao()
        val useCase = useCaseWith(dao)
        coEvery { captureLocationUseCase(any()) } returns
            LocationCaptureResult.Matched(match(MatchStatus.MATCHED, "Unidade P80"))
        coEvery { checkRepository.submit(any(), any(), any(), any(), any(), any(), any()) } returns
            AppResult.Success(history(CheckAction.CHECKIN))

        val result = useCase(chave, projects, history(CheckAction.CHECKOUT), 15, 50)

        assertTrue(result is AutoActivitiesResult.Submitted)
        with(dao.rows.last()) {
            assertEquals("CHECK_IN", kind)
            assertEquals("SUCCESS", severity)
            assertEquals("SYS", actor)
            assertEquals("Check-in at Unidade P80.", description)
        }
    }

    @Test
    fun automatic_network_failure_logs_queued_row() = runTest {
        val dao = CapturingDao()
        val useCase = useCaseWith(dao)
        coEvery { captureLocationUseCase(any()) } returns
            LocationCaptureResult.Matched(match(MatchStatus.MATCHED, "Unidade P80"))
        coEvery { checkRepository.submit(any(), any(), any(), any(), any(), any(), any()) } returns
            AppResult.Failure(ApiError.Network)

        val result = useCase(chave, projects, history(CheckAction.CHECKOUT), 15, 50)

        assertEquals(AutoActivitiesResult.NetworkError, result)
        with(dao.rows.last()) {
            assertEquals("SYNC", kind)
            assertEquals("WARNING", severity)
            assertEquals("SYS", actor)
            assertTrue(description.contains("queued (offline)"))
        }
    }

    @Test
    fun automatic_http_failure_logs_check_in_failure_row() = runTest {
        val dao = CapturingDao()
        val useCase = useCaseWith(dao)
        coEvery { captureLocationUseCase(any()) } returns
            LocationCaptureResult.Matched(match(MatchStatus.MATCHED, "Unidade P80"))
        coEvery { checkRepository.submit(any(), any(), any(), any(), any(), any(), any()) } returns
            AppResult.Failure(ApiError.Http(500, "boom"))

        val result = useCase(chave, projects, history(CheckAction.CHECKOUT), 15, 50)

        assertEquals(AutoActivitiesResult.NetworkError, result)
        with(dao.rows.last()) {
            assertEquals("CHECK_IN", kind)
            assertEquals("FAILURE", severity)
            assertEquals("Check-in failed at Unidade P80.", description)
        }
    }

    @Test
    fun not_configured_logs_no_active_project_warning() = runTest {
        // plan004 (EP7) — no active project → SYSTEM/WARNING row, no GPS/submit attempted.
        val dao = CapturingDao()
        val useCase = useCaseWith(dao)
        val noProject = UserProjects(projects = emptyList(), activeProject = "")

        val result = useCase(chave, noProject, history(CheckAction.CHECKOUT), 15, 50)

        assertEquals(AutoActivitiesResult.NotConfigured, result)
        with(dao.rows.last()) {
            assertEquals("SYSTEM", kind)
            assertEquals("WARNING", severity)
            assertEquals("SYS", actor)
            assertEquals("No active project — skipped.", description)
        }
    }

    @Test
    fun network_during_capture_logs_raw_reading_queued() = runTest {
        // plan004 (EP7) — server unreachable during capture, a GPS fix was obtained → raw reading queued
        // for replay + a LOCATION/WARNING row (no action kind decided yet).
        val dao = CapturingDao()
        val useCase = useCaseWith(dao)
        coEvery { captureLocationUseCase(any()) } returns
            LocationCaptureResult.NetworkError(LocationReading(1.3, 103.8, 10.0))

        val result = useCase(chave, projects, history(CheckAction.CHECKOUT), 15, 50)

        assertEquals(AutoActivitiesResult.NetworkError, result)
        coVerify { offlineQueue.enqueue(any()) }
        with(dao.rows.last()) {
            assertEquals("LOCATION", kind)
            assertEquals("WARNING", severity)
            assertEquals("SYS", actor)
            assertEquals("Location reading queued offline — will sync on reconnect.", description)
        }
    }

    @Test
    fun loggingFailure_neverChanges_the_check_in_result() = runTest {
        val dao = CapturingDao(throwOnInsert = true) // persistence throws
        val useCase = useCaseWith(dao)
        coEvery { captureLocationUseCase(any()) } returns
            LocationCaptureResult.Matched(match(MatchStatus.MATCHED, "Unidade P80"))
        coEvery { checkRepository.submit(any(), any(), any(), any(), any(), any(), any()) } returns
            AppResult.Success(history(CheckAction.CHECKIN))

        val result = useCase(chave, projects, history(CheckAction.CHECKOUT), 15, 50)

        // The logging crash is swallowed; the automatic check-in still succeeds (golden rule 2).
        assertTrue("logging failure must not change the result", result is AutoActivitiesResult.Submitted)
        assertEquals(0, dao.rows.size)
    }
}
