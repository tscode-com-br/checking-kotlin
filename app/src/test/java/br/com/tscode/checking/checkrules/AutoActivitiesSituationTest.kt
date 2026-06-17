package br.com.tscode.checking.checkrules

import br.com.tscode.checking.core.result.AppResult
import br.com.tscode.checking.domain.checkrules.MixedZoneDecisionSettings
import br.com.tscode.checking.domain.checkrules.shouldAttemptAutomaticMixedZoneLocationEvent
import br.com.tscode.checking.domain.model.CheckAction
import br.com.tscode.checking.domain.model.HistoryState
import br.com.tscode.checking.domain.model.LocationMatch
import br.com.tscode.checking.domain.model.MatchStatus
import br.com.tscode.checking.domain.model.UserProjects
import br.com.tscode.checking.domain.repository.CheckRepository
import br.com.tscode.checking.domain.usecase.AutoActivitiesResult
import br.com.tscode.checking.domain.usecase.CaptureLocationUseCase
import br.com.tscode.checking.domain.usecase.LocationCaptureResult
import br.com.tscode.checking.domain.usecase.RunAutomaticActivitiesUseCase
import br.com.tscode.checking.core.time.Clock
import br.com.tscode.checking.platform.background.offline.OfflineCheckQueue
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

/**
 * Per-Situation coverage of the automatic-activities decision engine (P5-D4). Exercises
 * RunAutomaticActivitiesUseCase end-to-end (location match + last action → action submitted) with
 * mocked location/repository, so it covers the branching in resolveActivity AND the pure decision
 * functions in AutoActivities.kt.
 *
 * The engine MUST mirror the production web app (sistema/app/static/check/automatic-activities.js +
 * app.js): only a real polygon MATCH or OUTSIDE_WORKPLACE acts; NOT_IN_KNOWN_LOCATION ("near, but not
 * inside any registered area") is NEVER an automatic check-in target (so S5 and the "near" sub-case
 * of S3/S7B do nothing — and the app never submits "Localização não Cadastrada", which the server
 * rejects with 422).
 */
class AutoActivitiesSituationTest {

    private val chave = "STSM"
    private val projeto = "P80"
    private val projects = UserProjects(projects = listOf(projeto), activeProject = projeto)

    private val captureLocationUseCase = mockk<CaptureLocationUseCase>()
    private val checkRepository = mockk<CheckRepository>()
    private val offlineCheckQueue = mockk<OfflineCheckQueue>(relaxed = true)
    private val clock = mockk<Clock> { every { now() } returns Instant.parse("2026-06-16T12:00:00Z") }

    private val useCase = RunAutomaticActivitiesUseCase(captureLocationUseCase, checkRepository, offlineCheckQueue, clock)

    private fun match(
        status: MatchStatus,
        resolvedLocal: String? = null,
        nearest: Double? = null,
        minimum: Int = 2000,
    ) = LocationMatch(
        matched = status == MatchStatus.MATCHED,
        resolvedLocal = resolvedLocal,
        label = resolvedLocal ?: "",
        status = status,
        message = "",
        accuracyMeters = 10.0,
        accuracyThresholdMeters = 50,
        minimumCheckoutDistanceMeters = minimum,
        nearestWorkplaceDistanceMeters = nearest,
    )

    private fun history(
        last: CheckAction?,
        currentLocal: String? = null,
        lastCheckinAt: Instant? = null,
        lastCheckoutAt: Instant? = null,
    ) = HistoryState(
        found = true,
        chave = chave,
        projeto = projeto,
        currentAction = last,
        currentLocal = currentLocal,
        hasCurrentDayCheckin = last == CheckAction.CHECKIN,
        lastCheckinAt = lastCheckinAt ?: if (last == CheckAction.CHECKIN) Instant.now() else null,
        lastCheckoutAt = lastCheckoutAt ?: if (last == CheckAction.CHECKOUT) Instant.now() else null,
        transportEnabled = false,
    )

    private suspend fun run(match: LocationMatch, state: HistoryState?): AutoActivitiesResult {
        coEvery { captureLocationUseCase(any()) } returns LocationCaptureResult.Matched(match)
        coEvery { checkRepository.submit(any(), any(), any(), any(), any(), any(), any()) } returns
            AppResult.Success(history(CheckAction.CHECKIN))
        return useCase(chave, projects, state, mixedZoneIntervalMinutes = 15, accuracyThresholdMeters = 50)
    }

    private fun assertSubmitted(result: AutoActivitiesResult, action: CheckAction, local: String?) {
        assertTrue("expected Submitted, got $result", result is AutoActivitiesResult.Submitted)
        result as AutoActivitiesResult.Submitted
        assertEquals(action, result.action)
        assertEquals(local, result.local)
    }

    private suspend fun assertNoSubmit(result: AutoActivitiesResult) {
        assertEquals(AutoActivitiesResult.NoAction, result)
        coVerify(exactly = 0) { checkRepository.submit(any(), any(), any(), any(), any(), any(), any()) }
    }

    // ─── Situation 1: last=check-in, in CheckOut zone OR far → CHECK-OUT ──────────
    @Test
    fun s1_checkin_in_checkout_zone_checks_out() = runTest {
        assertSubmitted(
            run(match(MatchStatus.MATCHED, "Zona de CheckOut"), history(CheckAction.CHECKIN)),
            CheckAction.CHECKOUT, "Zona de CheckOut",
        )
    }

    @Test
    fun s1_checkin_far_checks_out() = runTest {
        assertSubmitted(
            run(match(MatchStatus.OUTSIDE_WORKPLACE, nearest = 5000.0), history(CheckAction.CHECKIN)),
            CheckAction.CHECKOUT, "Fora do Local de Trabalho",
        )
    }

    // ─── Situation 2: last=check-out, in CheckOut zone OR far → no action ─────────
    @Test
    fun s2_checkout_in_checkout_zone_no_action() = runTest {
        assertNoSubmit(run(match(MatchStatus.MATCHED, "Zona de CheckOut"), history(CheckAction.CHECKOUT)))
    }

    @Test
    fun s2_checkout_far_no_action() = runTest {
        assertNoSubmit(run(match(MatchStatus.OUTSIDE_WORKPLACE, nearest = 5000.0), history(CheckAction.CHECKOUT)))
    }

    // ─── Situation 3: last=check-out, in a registered location → CHECK-IN ─────────
    @Test
    fun s3_checkout_in_registered_location_checks_in() = runTest {
        assertSubmitted(
            run(match(MatchStatus.MATCHED, "Unidade P80"), history(CheckAction.CHECKOUT)),
            CheckAction.CHECKIN, "Unidade P80",
        )
    }

    // ─── Situation 3 (near sub-case) / 7B: near but not registered → NO action ────
    // Matches the web app's BRANCH 3 (never a check-in target); never submits "Localização não
    // Cadastrada" (422). This is the corrected Kotlin behaviour.
    @Test
    fun s3near_checkout_near_not_registered_no_action() = runTest {
        assertNoSubmit(
            run(match(MatchStatus.NOT_IN_KNOWN_LOCATION, nearest = 500.0), history(CheckAction.CHECKOUT)),
        )
    }

    // ─── Situation 4: last=check-in, in a registered location → re-CHECK-IN ───────
    @Test
    fun s4_checkin_in_registered_location_rechecks_in() = runTest {
        assertSubmitted(
            run(match(MatchStatus.MATCHED, "Unidade P80"), history(CheckAction.CHECKIN, currentLocal = "Unidade P80")),
            CheckAction.CHECKIN, "Unidade P80",
        )
    }

    // ─── Situation 5: last=check-in, near but not registered → NO action ──────────
    @Test
    fun s5_checkin_near_not_registered_no_action() = runTest {
        assertNoSubmit(
            run(match(MatchStatus.NOT_IN_KNOWN_LOCATION, nearest = 500.0), history(CheckAction.CHECKIN)),
        )
    }

    // ─── Situation 7B: last=check-out, left CheckOut zone, near not registered → NO action ─
    @Test
    fun s7b_checkout_near_not_registered_no_action() = runTest {
        assertNoSubmit(
            run(match(MatchStatus.NOT_IN_KNOWN_LOCATION, nearest = 800.0), history(CheckAction.CHECKOUT)),
        )
    }

    // ─── Situation 8: Zona Mista alternation (last activity NOT in the mixed zone) ─
    @Test
    fun s8_mixed_zone_toggles_from_checkin_to_checkout() = runTest {
        assertSubmitted(
            run(match(MatchStatus.MATCHED, "Zona Mista"), history(CheckAction.CHECKIN, currentLocal = "Unidade P80")),
            CheckAction.CHECKOUT, "Zona Mista",
        )
    }

    @Test
    fun s8_mixed_zone_toggles_from_checkout_to_checkin() = runTest {
        assertSubmitted(
            run(match(MatchStatus.MATCHED, "Zona Mista"), history(CheckAction.CHECKOUT, currentLocal = "Unidade P80")),
            CheckAction.CHECKIN, "Zona Mista",
        )
    }

    // ─── Situation 8: cooldown for CONSECUTIVE mixed-zone reads (deterministic pure fn) ─
    @Test
    fun s8_mixed_zone_cooldown_blocks_consecutive_toggle() {
        val now = Instant.parse("2026-06-16T12:00:00Z")
        val state = history(CheckAction.CHECKIN, currentLocal = "Zona Mista", lastCheckinAt = now.minusSeconds(5 * 60))
        val fire = shouldAttemptAutomaticMixedZoneLocationEvent(
            match(MatchStatus.MATCHED, "Zona Mista"),
            state,
            MixedZoneDecisionSettings(mixedZoneIntervalMinutes = 15, referenceTime = now),
        )
        assertFalse("cooldown (5 min < 15 min) must block a consecutive mixed-zone toggle", fire)
    }

    @Test
    fun s8_mixed_zone_cooldown_expired_allows_toggle() {
        val now = Instant.parse("2026-06-16T12:00:00Z")
        val state = history(CheckAction.CHECKIN, currentLocal = "Zona Mista", lastCheckinAt = now.minusSeconds(20 * 60))
        val fire = shouldAttemptAutomaticMixedZoneLocationEvent(
            match(MatchStatus.MATCHED, "Zona Mista"),
            state,
            MixedZoneDecisionSettings(mixedZoneIntervalMinutes = 15, referenceTime = now),
        )
        assertTrue("cooldown expired (20 min >= 15 min) → toggle allowed", fire)
    }

    // ─── No history (first registration handled by the normal flow, like the web) ─
    @Test
    fun no_history_in_registered_location_checks_in() = runTest {
        assertSubmitted(
            run(match(MatchStatus.MATCHED, "Unidade P80"), history(last = null)),
            CheckAction.CHECKIN, "Unidade P80",
        )
    }

    @Test
    fun no_history_far_no_action() = runTest {
        assertNoSubmit(run(match(MatchStatus.OUTSIDE_WORKPLACE, nearest = 5000.0), history(last = null)))
    }

    @Test
    fun no_history_in_checkout_zone_no_action() = runTest {
        assertNoSubmit(run(match(MatchStatus.MATCHED, "Zona de CheckOut"), history(last = null)))
    }

    @Test
    fun no_history_near_not_registered_no_action() = runTest {
        assertNoSubmit(run(match(MatchStatus.NOT_IN_KNOWN_LOCATION, nearest = 500.0), history(last = null)))
    }
}
