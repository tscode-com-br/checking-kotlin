package br.com.tscode.checking.domain.usecase

import br.com.tscode.checking.core.error.ApiError
import br.com.tscode.checking.core.result.AppResult
import br.com.tscode.checking.core.time.Clock
import br.com.tscode.checking.domain.model.CheckAction
import br.com.tscode.checking.domain.model.HistoryState
import br.com.tscode.checking.domain.model.LocationMatch
import br.com.tscode.checking.domain.model.MatchStatus
import br.com.tscode.checking.domain.model.UserProjects
import br.com.tscode.checking.platform.background.offline.OfflineCheckQueue
import io.mockk.MockKMatcherScope
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

/**
 * TP3 — proves the **duplicate check-in is gone** through the LIVE flow (RunAutomaticActivitiesUseCase),
 * via change A / P6.1 (check-in only on location change). The pure-engine "same location → null" lives in
 * TP1 (4a/6a) + TP2; here we run the use-case (decide → submit → thread the returned state) and assert the
 * exact number of submits. Single-flight (concurrent guard) is OrchestratorSingleFlightTest.
 */
class DuplicateEliminationTest {

    private val chave = "DUP1"
    private val projeto = "P80"
    private val projects = UserProjects(projects = listOf(projeto), activeProject = projeto)

    private val captureLocationUseCase = mockk<CaptureLocationUseCase>()
    private val checkRepository = mockk<br.com.tscode.checking.domain.repository.CheckRepository>()
    private val offlineQueue = mockk<OfflineCheckQueue>(relaxed = true)
    private val clock = mockk<Clock> { every { now() } returns Instant.parse("2026-06-18T08:00:00Z") }

    private val useCase = RunAutomaticActivitiesUseCase(captureLocationUseCase, checkRepository, offlineQueue, clock, mockk(relaxed = true))

    private fun match(local: String?, status: MatchStatus = MatchStatus.MATCHED) = LocationMatch(
        matched = status == MatchStatus.MATCHED,
        resolvedLocal = local,
        label = local ?: "",
        status = status,
        message = "",
        accuracyMeters = 10.0,
        accuracyThresholdMeters = 50,
        minimumCheckoutDistanceMeters = 2000,
        nearestWorkplaceDistanceMeters = null,
    )

    private fun checkedIn(local: String) = HistoryState(
        found = true, chave = chave, projeto = projeto,
        currentAction = CheckAction.CHECKIN, currentLocal = local,
        hasCurrentDayCheckin = true, lastCheckinAt = Instant.parse("2026-06-18T07:00:00Z"),
        lastCheckoutAt = null, transportEnabled = false,
    )

    private suspend fun run(match: LocationMatch, state: HistoryState?): AutoActivitiesResult {
        coEvery { captureLocationUseCase(any()) } returns LocationCaptureResult.Matched(match)
        return useCase(chave, projects, state, mixedZoneIntervalMinutes = 15, accuracyThresholdMeters = 50)
    }

    // mockk matcher for "any submit call" — usable inside coEvery/coVerify (suspend + MockKMatcherScope).
    private suspend fun MockKMatcherScope.anySubmit() =
        checkRepository.submit(any(), any(), any(), any(), any(), any(), any())

    // 1. The production bug: geofence EXIT(A)+ENTER(B) = two triggers at the same new location B.
    @Test fun `two_runs_same_new_location_submits_exactly_once`() = runTest {
        coEvery { anySubmit() } returns AppResult.Success(checkedIn("B"))

        val r1 = run(match("B"), checkedIn("A")) // moved A→B → one check-in at B
        assertTrue(r1 is AutoActivitiesResult.Submitted)
        assertEquals("B", (r1 as AutoActivitiesResult.Submitted).local)

        val r2 = run(match("B"), r1.newState)    // second trigger, now checked-in at B → no action
        assertEquals(AutoActivitiesResult.NoAction, r2)

        coVerify(exactly = 1) { anySubmit() }
    }

    // 3. Stationary repeats while checked-in at B → never re-check-in (duplicate can't accumulate).
    @Test fun `stationary_repeats_never_re_check_in`() = runTest {
        val atB = checkedIn("B")
        repeat(5) { assertEquals(AutoActivitiesResult.NoAction, run(match("B"), atB)) }
        coVerify(exactly = 0) { anySubmit() }
    }

    // 4. Genuine moves A → B → C → exactly one check-in per distinct location (fix must NOT over-suppress).
    @Test fun `genuine_moves_check_in_per_distinct_location`() = runTest {
        coEvery { anySubmit() } answers { AppResult.Success(checkedIn(arg<String?>(3) ?: "?")) }
        var state: HistoryState? = checkedIn("A")
        for (loc in listOf("B", "C")) {
            val r = run(match(loc), state)
            assertTrue(r is AutoActivitiesResult.Submitted)
            assertEquals(loc, (r as AutoActivitiesResult.Submitted).local)
            state = r.newState
        }
        coVerify(exactly = 2) { anySubmit() } // B and C (the pre-existing A is not re-submitted)
    }

    // 5. DOCUMENTED edge (NOT a regression, no guard added): a FAILED submit does not advance the cached
    // state, so a later run at the same location re-decides a check-in. The removed 10-min dedup never
    // covered this either; the failed submit is queued offline and dedups by client_event_id on replay.
    @Test fun `failed_submit_leaves_state_unchanged_so_retry_may_re_decide`() = runTest {
        coEvery { anySubmit() } returns AppResult.Failure(ApiError.Network)
        val r1 = run(match("B"), checkedIn("A"))
        assertEquals(AutoActivitiesResult.NetworkError, r1) // submit failed → state NOT advanced

        coEvery { anySubmit() } returns AppResult.Success(checkedIn("B"))
        val r2 = run(match("B"), checkedIn("A")) // still reading the old state (A) → re-decides check-in B
        assertTrue("known edge: a failed submit's state isn't advanced", r2 is AutoActivitiesResult.Submitted)
    }
}
