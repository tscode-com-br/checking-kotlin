package br.com.tscode.checking.domain.usecase

import br.com.tscode.checking.core.error.ApiError
import br.com.tscode.checking.core.result.AppResult
import br.com.tscode.checking.core.time.Clock
import br.com.tscode.checking.domain.model.CheckAction
import br.com.tscode.checking.domain.model.HistoryState
import br.com.tscode.checking.domain.model.LocationMatch
import br.com.tscode.checking.domain.model.MatchStatus
import br.com.tscode.checking.domain.model.UserProjects
import br.com.tscode.checking.domain.offline.PendingCheckEvent
import br.com.tscode.checking.domain.repository.CheckRepository
import br.com.tscode.checking.platform.background.offline.OfflineCheckQueue
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

/**
 * P8 — the engine queues a check event for offline replay ONLY on a real connectivity loss
 * (ApiError.Network), and at both failure points: the location match (→ Raw) and the submit
 * (→ Decided). HTTP errors must NOT be queued (the server is reachable; replaying a 4xx forever
 * would be a bug). Replay/decoding is covered by PendingCheckReplayerTest.
 */
class RunAutomaticActivitiesOfflineTest {

    private val chave = "HR70"
    private val projeto = "P80"
    private val projects = UserProjects(projects = listOf(projeto), activeProject = projeto)

    private val captureLocationUseCase = mockk<CaptureLocationUseCase>()
    private val checkRepository = mockk<CheckRepository>()
    private val offlineQueue = mockk<OfflineCheckQueue>()
    private val clock = mockk<Clock> { every { now() } returns Instant.parse("2026-06-16T12:00:00Z") }
    private val useCase = RunAutomaticActivitiesUseCase(captureLocationUseCase, checkRepository, offlineQueue, clock, mockk(relaxed = true))

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
        found = true,
        chave = chave,
        projeto = projeto,
        currentAction = last,
        currentLocal = null,
        hasCurrentDayCheckin = last == CheckAction.CHECKIN,
        lastCheckinAt = if (last == CheckAction.CHECKIN) Instant.now() else null,
        lastCheckoutAt = if (last == CheckAction.CHECKOUT) Instant.now() else null,
        transportEnabled = false,
    )

    @Test
    fun match_network_failure_with_fix_enqueues_raw() = runTest {
        coEvery { captureLocationUseCase(any()) } returns
            LocationCaptureResult.NetworkError(LocationReading(1.5, 103.8, 12.0))
        val slot = slot<PendingCheckEvent>()
        coEvery { offlineQueue.enqueue(capture(slot)) } just Runs

        val result = useCase(chave, projects, history(CheckAction.CHECKIN), 15, 50)

        assertEquals(AutoActivitiesResult.NetworkError, result)
        val raw = slot.captured
        assertTrue("expected Raw, got $raw", raw is PendingCheckEvent.Raw)
        raw as PendingCheckEvent.Raw
        assertEquals(1.5, raw.latitude, 0.0)
        assertEquals(103.8, raw.longitude, 0.0)
        assertEquals(projeto, raw.projeto)
    }

    @Test
    fun match_http_error_does_not_enqueue() = runTest {
        // CaptureLocationUseCase reports a null reading for non-Network failures.
        coEvery { captureLocationUseCase(any()) } returns LocationCaptureResult.NetworkError(null)
        val result = useCase(chave, projects, history(CheckAction.CHECKIN), 15, 50)
        assertEquals(AutoActivitiesResult.NetworkError, result)
        coVerify(exactly = 0) { offlineQueue.enqueue(any()) }
    }

    @Test
    fun submit_network_failure_enqueues_decided() = runTest {
        coEvery { captureLocationUseCase(any()) } returns
            LocationCaptureResult.Matched(match(MatchStatus.MATCHED, "Unidade P80"))
        coEvery { checkRepository.submit(any(), any(), any(), any(), any(), any(), any()) } returns
            AppResult.Failure(ApiError.Network)
        val slot = slot<PendingCheckEvent>()
        coEvery { offlineQueue.enqueue(capture(slot)) } just Runs

        val result = useCase(chave, projects, history(CheckAction.CHECKOUT), 15, 50)

        assertEquals(AutoActivitiesResult.NetworkError, result)
        val decided = slot.captured
        assertTrue("expected Decided, got $decided", decided is PendingCheckEvent.Decided)
        decided as PendingCheckEvent.Decided
        assertEquals("checkin", decided.action) // MATCHED + last=checkout → check-in (S3)
        assertEquals("Unidade P80", decided.local)
    }

    @Test
    fun submit_http_error_does_not_enqueue() = runTest {
        coEvery { captureLocationUseCase(any()) } returns
            LocationCaptureResult.Matched(match(MatchStatus.MATCHED, "Unidade P80"))
        coEvery { checkRepository.submit(any(), any(), any(), any(), any(), any(), any()) } returns
            AppResult.Failure(ApiError.Http(500, "boom"))
        val result = useCase(chave, projects, history(CheckAction.CHECKOUT), 15, 50)
        assertEquals(AutoActivitiesResult.NetworkError, result)
        coVerify(exactly = 0) { offlineQueue.enqueue(any()) }
    }
}
