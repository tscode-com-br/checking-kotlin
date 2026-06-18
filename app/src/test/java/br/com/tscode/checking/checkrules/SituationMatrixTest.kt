package br.com.tscode.checking.checkrules

import br.com.tscode.checking.domain.checkrules.AutomaticActivity
import br.com.tscode.checking.domain.checkrules.resolveAutomaticActivityForMatch
import br.com.tscode.checking.domain.model.CheckAction
import br.com.tscode.checking.domain.model.HistoryState
import br.com.tscode.checking.domain.model.LocationMatch
import br.com.tscode.checking.domain.model.MatchStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

/**
 * TP1 — SITUATION MATRIX on the PURE engine `resolveAutomaticActivityForMatch(match, state, interval)`.
 *
 * One named test per situation/variant of `regras_checkin_checkout_kotlin.txt`, asserting the exact
 * post-plan002 `AutomaticActivity(action, local)?`. No mocks, no use-case, no network — these encode the
 * agreed behavior (engine changes only come from EP6). All cases assume auto-activities ON; the
 * orchestrator's ON/OFF gate (Situação 9) is asserted in TP4, not here.
 */
class SituationMatrixTest {

    private val MIXED_INTERVAL = 15

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

    private fun state(
        last: CheckAction?,
        currentLocal: String? = null,
        lastCheckinAt: Instant? = null,
        lastCheckoutAt: Instant? = null,
    ) = HistoryState(
        found = true,
        chave = "STM1",
        projeto = "P80",
        currentAction = last,
        currentLocal = currentLocal,
        hasCurrentDayCheckin = last == CheckAction.CHECKIN,
        lastCheckinAt = lastCheckinAt ?: if (last == CheckAction.CHECKIN) Instant.now() else null,
        lastCheckoutAt = lastCheckoutAt ?: if (last == CheckAction.CHECKOUT) Instant.now() else null,
        transportEnabled = false,
    )

    private fun decide(match: LocationMatch, state: HistoryState?): AutomaticActivity? =
        resolveAutomaticActivityForMatch(match, state, MIXED_INTERVAL)

    private fun assertActivity(result: AutomaticActivity?, action: CheckAction, local: String?) {
        requireNotNull(result) { "expected an AutomaticActivity, got null" }
        assertEquals(action, result.action)
        assertEquals(local, result.local)
    }

    // ─── Situação 1: last=check-in → CHECK-OUT (CheckOut zone OR far) ──────────────
    @Test fun `1a_checkin_in_checkout_zone_checks_out`() =
        assertActivity(decide(match(MatchStatus.MATCHED, "Zona de CheckOut"), state(CheckAction.CHECKIN)),
            CheckAction.CHECKOUT, "Zona de CheckOut")

    @Test fun `1b_checkin_far_checks_out`() =
        assertActivity(decide(match(MatchStatus.OUTSIDE_WORKPLACE), state(CheckAction.CHECKIN)),
            CheckAction.CHECKOUT, "Fora do Local de Trabalho")

    // ─── Situação 2: last=check-out, CheckOut zone OR far → no action ─────────────
    @Test fun `2a_checkout_in_checkout_zone_no_action`() =
        assertNull(decide(match(MatchStatus.MATCHED, "Zona de CheckOut"), state(CheckAction.CHECKOUT)))

    @Test fun `2b_checkout_far_no_action`() =
        assertNull(decide(match(MatchStatus.OUTSIDE_WORKPLACE), state(CheckAction.CHECKOUT)))

    // ─── Situação 3: last=check-out → CHECK-IN on entering a registered area ──────
    @Test fun `3a_checkout_enters_registered_area_checks_in`() =
        assertActivity(decide(match(MatchStatus.MATCHED, "P80-Portaria"), state(CheckAction.CHECKOUT)),
            CheckAction.CHECKIN, "P80-Portaria")

    @Test fun `3b_checkout_near_but_outside_no_action`() =
        assertNull(decide(match(MatchStatus.NOT_IN_KNOWN_LOCATION), state(CheckAction.CHECKOUT)))

    // ─── Situação 4: last=check-in, matched → CHECK-IN only on location CHANGE (P6.1) ─
    @Test fun `4a_checkin_same_registered_area_no_action`() =
        assertNull(decide(
            match(MatchStatus.MATCHED, "P80-Portaria"),
            state(CheckAction.CHECKIN, currentLocal = "P80-Portaria"),
        ))

    @Test fun `4b_checkin_different_registered_area_rechecks_in`() =
        assertActivity(decide(
            match(MatchStatus.MATCHED, "P80-Refeitorio"),
            state(CheckAction.CHECKIN, currentLocal = "P80-Portaria"),
        ), CheckAction.CHECKIN, "P80-Refeitorio")

    // ─── Situação 5: last=check-in, near but outside → CHECK-IN "Não Cadastrada" (P6.2) ─
    @Test fun `5a_checkin_near_outside_checks_in_unregistered`() =
        assertActivity(decide(
            match(MatchStatus.NOT_IN_KNOWN_LOCATION),
            state(CheckAction.CHECKIN, currentLocal = "P80-Portaria"),
        ), CheckAction.CHECKIN, "Localização não Cadastrada")

    @Test fun `5b_checkin_already_unregistered_no_repeat`() =
        assertNull(decide(
            match(MatchStatus.NOT_IN_KNOWN_LOCATION),
            state(CheckAction.CHECKIN, currentLocal = "Localização não Cadastrada"),
        ))

    // ─── Situação 6: refresh (FOREGROUND) — engine is trigger-agnostic, so == Situação 4 ─
    @Test fun `6a_refresh_same_area_no_action`() =
        assertNull(decide(
            match(MatchStatus.MATCHED, "P80-Portaria"),
            state(CheckAction.CHECKIN, currentLocal = "P80-Portaria"),
        ))

    @Test fun `6b_refresh_different_area_rechecks_in`() =
        assertActivity(decide(
            match(MatchStatus.MATCHED, "P80-Refeitorio"),
            state(CheckAction.CHECKIN, currentLocal = "P80-Portaria"),
        ), CheckAction.CHECKIN, "P80-Refeitorio")

    // ─── Situação 7: leaving CheckOut zone (last=check-out) ────────────────────────
    @Test fun `7A_checkout_enters_registered_area_checks_in`() =
        assertActivity(decide(match(MatchStatus.MATCHED, "P80-Portaria"), state(CheckAction.CHECKOUT)),
            CheckAction.CHECKIN, "P80-Portaria")

    // RESOLVED: a checked-out user in a "near but outside" zone gets NO check-in (follows Situação 3).
    @Test fun `7B_checkout_near_but_outside_no_action`() =
        assertNull(decide(match(MatchStatus.NOT_IN_KNOWN_LOCATION), state(CheckAction.CHECKOUT)))

    // ─── Situação 8: Zona Mista alternation + cooldown ─────────────────────────────
    @Test fun `8a_mixed_zone_last_checkin_cooldown_elapsed_checks_out`() =
        assertActivity(decide(
            match(MatchStatus.MATCHED, "Zona Mista"),
            state(CheckAction.CHECKIN, currentLocal = "Zona Mista",
                lastCheckinAt = Instant.now().minusSeconds(20L * 60)),
        ), CheckAction.CHECKOUT, "Zona Mista")

    @Test fun `8b_mixed_zone_last_checkout_cooldown_elapsed_checks_in`() =
        assertActivity(decide(
            match(MatchStatus.MATCHED, "Zona Mista"),
            state(CheckAction.CHECKOUT, currentLocal = "Zona Mista",
                lastCheckoutAt = Instant.now().minusSeconds(20L * 60)),
        ), CheckAction.CHECKIN, "Zona Mista")

    @Test fun `8c_mixed_zone_within_cooldown_no_action`() =
        assertNull(decide(
            match(MatchStatus.MATCHED, "Zona Mista"),
            state(CheckAction.CHECKIN, currentLocal = "Zona Mista",
                lastCheckinAt = Instant.now().minusSeconds(5L * 60)),
        ))

    // 8d: exception — from a Zona Mista check-in, moving far/CheckOut → immediate check-out (no cooldown).
    @Test fun `8d_mixed_zone_checkin_then_far_checks_out`() =
        assertActivity(decide(
            match(MatchStatus.OUTSIDE_WORKPLACE),
            state(CheckAction.CHECKIN, currentLocal = "Zona Mista"),
        ), CheckAction.CHECKOUT, "Fora do Local de Trabalho")

    // Situação 9 (auto OFF) is NOT a pure-engine case — the orchestrator never invokes the engine when
    // auto-activities is disabled. That ON/OFF gate is asserted in TP4 (VM/orchestrator), not here.
}
