package br.com.tscode.checking.domain.usecase

import br.com.tscode.checking.core.time.Clock
import br.com.tscode.checking.domain.model.CheckAction
import br.com.tscode.checking.domain.model.HistoryState
import br.com.tscode.checking.domain.model.LocationMatch
import br.com.tscode.checking.domain.model.MatchStatus
import br.com.tscode.checking.domain.model.UserProjects
import br.com.tscode.checking.domain.repository.CheckRepository
import br.com.tscode.checking.platform.activitylog.ActivityLogger
import br.com.tscode.checking.platform.background.offline.OfflineCheckQueue
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class RunAutomaticActivitiesLowAccuracyTest {

    private val captureLocation = mockk<CaptureLocationUseCase>()
    private val checkRepository = mockk<CheckRepository>(relaxed = true)
    private val useCase = RunAutomaticActivitiesUseCase(
        captureLocationUseCase = captureLocation,
        checkRepository = checkRepository,
        offlineQueue = mockk<OfflineCheckQueue>(relaxed = true),
        clock = mockk<Clock>(relaxed = true),
        activityLogger = mockk<ActivityLogger>(relaxed = true),
    )
    private val projects = UserProjects(listOf("P80"), "P80")

    private fun lowAccuracy() = LocationCaptureResult.Matched(
        LocationMatch(
            matched = false,
            resolvedLocal = null,
            label = "",
            status = MatchStatus.ACCURACY_TOO_LOW,
            message = "",
            accuracyMeters = 87.5,
            accuracyThresholdMeters = 30,
            minimumCheckoutDistanceMeters = 2_000,
            nearestWorkplaceDistanceMeters = null,
        ),
    )

    private fun state(lastAction: CheckAction) = HistoryState(
        found = true,
        chave = "HR70",
        projeto = "P80",
        currentAction = lastAction,
        currentLocal = "Unidade",
        hasCurrentDayCheckin = lastAction == CheckAction.CHECKIN,
        lastCheckinAt = if (lastAction == CheckAction.CHECKIN) Instant.parse("2026-01-01T10:00:00Z") else null,
        lastCheckoutAt = if (lastAction == CheckAction.CHECKOUT) Instant.parse("2026-01-01T10:00:00Z") else null,
        transportEnabled = false,
    )

    @Test
    fun `low accuracy with no history returns dedicated check-in result and never submits`() = runTest {
        coEvery { captureLocation(any()) } returns lowAccuracy()

        val result = useCase("HR70", projects, null, 15, 30)

        val low = result as AutoActivitiesResult.AccuracyTooLow
        assertEquals(CheckAction.CHECKIN, low.expectedAction)
        assertEquals(87.5, low.accuracyMeters)
        assertEquals(30, low.thresholdMeters)
        coVerify(exactly = 0) {
            checkRepository.submit(any(), any(), any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `low accuracy after checkout has deterministic check-in title`() = runTest {
        coEvery { captureLocation(any()) } returns lowAccuracy()

        val result = useCase("HR70", projects, state(CheckAction.CHECKOUT), 15, 30)

        assertEquals(
            CheckAction.CHECKIN,
            (result as AutoActivitiesResult.AccuracyTooLow).expectedAction,
        )
    }

    @Test
    fun `low accuracy after checkin keeps expected action ambiguous`() = runTest {
        coEvery { captureLocation(any()) } returns lowAccuracy()

        val result = useCase("HR70", projects, state(CheckAction.CHECKIN), 15, 30)

        assertNull((result as AutoActivitiesResult.AccuracyTooLow).expectedAction)
        coVerify(exactly = 0) {
            checkRepository.submit(any(), any(), any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `capture timeout remains distinguishable from unavailable permission`() = runTest {
        coEvery { captureLocation(any()) } returns LocationCaptureResult.Timeout
        assertEquals(
            AutoActivitiesResult.CaptureTimeout,
            useCase("HR70", projects, null, 15, 30),
        )

        coEvery { captureLocation(any()) } returns LocationCaptureResult.NoPermission
        assertEquals(
            AutoActivitiesResult.NoPermission,
            useCase("HR70", projects, null, 15, 30),
        )
    }
}
