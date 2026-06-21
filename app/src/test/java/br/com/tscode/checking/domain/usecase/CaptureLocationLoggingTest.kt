package br.com.tscode.checking.domain.usecase

import br.com.tscode.checking.core.result.AppResult
import br.com.tscode.checking.core.time.Clock
import br.com.tscode.checking.data.local.activitylog.ActivityLog
import br.com.tscode.checking.data.local.activitylog.ActivityLogDao
import br.com.tscode.checking.data.local.activitylog.ActivityLogRow
import br.com.tscode.checking.domain.model.LocationMatch
import br.com.tscode.checking.domain.model.MatchStatus
import br.com.tscode.checking.domain.repository.CheckRepository
import br.com.tscode.checking.platform.activitylog.ActivityLogger
import br.com.tscode.checking.platform.location.LocationCapture
import br.com.tscode.checking.platform.location.LocationProvider
import io.mockk.coEvery
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
 * plan004 EP7 — CaptureLocationUseCase location-fix instrumentation through the REAL ActivityLogger +
 * store over a capturing DAO (no mocking of the final logger). It is the single chokepoint for BOTH
 * manual and automatic GPS captures, so the row is recorded exactly once. Asserts the location-fix INFO
 * row, the accuracy-too-low WARNING row, and that a persistence failure NEVER changes the capture result
 * (golden rule 2).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CaptureLocationLoggingTest {

    private val locationProvider = mockk<LocationProvider>()
    private val checkRepository = mockk<CheckRepository>()
    private val clock = mockk<Clock> { every { now() } returns Instant.parse("2026-06-20T08:00:00Z") }

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

    private fun match(status: MatchStatus, local: String? = null) = LocationMatch(
        matched = status == MatchStatus.MATCHED, resolvedLocal = local, label = local ?: "",
        status = status, message = "", accuracyMeters = 10.0, accuracyThresholdMeters = 50,
        minimumCheckoutDistanceMeters = 2000, nearestWorkplaceDistanceMeters = null,
    )

    private fun TestScope.useCaseWith(dao: CapturingDao): CaptureLocationUseCase {
        val logger = ActivityLogger(clock, ActivityLog(dao), CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
        return CaptureLocationUseCase(locationProvider, checkRepository, logger)
    }

    @Test
    fun matched_fix_logs_location_fixed_info_row() = runTest {
        val dao = CapturingDao()
        val useCase = useCaseWith(dao)
        coEvery { locationProvider.capture(any()) } returns LocationCapture.Success(1.3, 103.8, 12.7)
        coEvery { checkRepository.matchLocation(any(), any(), any()) } returns
            AppResult.Success(match(MatchStatus.MATCHED, "Unidade P80"))

        val result = useCase(50)

        assertTrue(result is LocationCaptureResult.Matched)
        with(dao.rows.last()) {
            assertEquals("LOCATION", kind)
            assertEquals("INFO", severity)
            assertEquals("SYS", actor)
            assertEquals("Location fixed (±12m) → Unidade P80.", description)
            assertEquals("Unidade P80", location)
        }
    }

    @Test
    fun accuracy_too_low_logs_warning_row() = runTest {
        val dao = CapturingDao()
        val useCase = useCaseWith(dao)
        coEvery { locationProvider.capture(any()) } returns LocationCapture.Success(1.3, 103.8, 80.4)
        coEvery { checkRepository.matchLocation(any(), any(), any()) } returns
            AppResult.Success(match(MatchStatus.ACCURACY_TOO_LOW))

        useCase(50)

        with(dao.rows.last()) {
            assertEquals("LOCATION", kind)
            assertEquals("WARNING", severity)
            assertEquals("Location accuracy too low (±80m).", description)
        }
    }

    @Test
    fun loggingFailure_neverChanges_the_capture_result() = runTest {
        val dao = CapturingDao(throwOnInsert = true) // persistence throws
        val useCase = useCaseWith(dao)
        coEvery { locationProvider.capture(any()) } returns LocationCapture.Success(1.3, 103.8, 12.7)
        coEvery { checkRepository.matchLocation(any(), any(), any()) } returns
            AppResult.Success(match(MatchStatus.MATCHED, "Unidade P80"))

        val result = useCase(50)

        assertTrue("logging failure must not change the capture result", result is LocationCaptureResult.Matched)
        assertEquals(0, dao.rows.size)
    }
}
