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
 * TP7 — dedicated CHECK-OUT preservation regression. Change A/D/E touched only the check-IN branches
 * (P6.1/P6.2, verified by git diff); these named tests pin that every check-out path is byte-for-byte
 * unchanged, plus the two invariants: NEVER two consecutive check-outs, and after a check-out the next
 * automatic action is a check-in.
 */
class CheckoutPreservationTest {

    private fun match(status: MatchStatus, resolvedLocal: String? = null) = LocationMatch(
        matched = status == MatchStatus.MATCHED, resolvedLocal = resolvedLocal, label = resolvedLocal ?: "",
        status = status, message = "", accuracyMeters = 10.0, accuracyThresholdMeters = 50,
        minimumCheckoutDistanceMeters = 2000, nearestWorkplaceDistanceMeters = null,
    )

    private fun checkedIn(local: String, at: Instant = Instant.now()) = HistoryState(
        found = true, chave = "CO01", projeto = "P80", currentAction = CheckAction.CHECKIN,
        currentLocal = local, hasCurrentDayCheckin = true, lastCheckinAt = at, lastCheckoutAt = null,
        transportEnabled = false,
    )

    private fun checkedOut(local: String, at: Instant = Instant.now()) = HistoryState(
        found = true, chave = "CO01", projeto = "P80", currentAction = CheckAction.CHECKOUT,
        currentLocal = local, hasCurrentDayCheckin = false, lastCheckinAt = null, lastCheckoutAt = at,
        transportEnabled = false,
    )

    private fun apply(prev: HistoryState, a: AutomaticActivity?): HistoryState = when {
        a == null -> prev
        a.action == CheckAction.CHECKIN -> prev.copy(
            currentAction = CheckAction.CHECKIN, currentLocal = a.local,
            lastCheckinAt = Instant.now().plusSeconds(1), lastCheckoutAt = null,
        )
        else -> prev.copy(
            currentAction = CheckAction.CHECKOUT, currentLocal = a.local,
            lastCheckoutAt = Instant.now().plusSeconds(1), lastCheckinAt = null,
        )
    }

    private fun decide(m: LocationMatch, s: HistoryState?) = resolveAutomaticActivityForMatch(m, s, 15)

    private fun assertActivity(r: AutomaticActivity?, action: CheckAction, local: String?) {
        requireNotNull(r); assertEquals(action, r.action); assertEquals(local, r.local)
    }

    // ── Point cases (matrix 1a/1b/2a/2b/3a/7A/8a/8d) ──────────────────────────────
    @Test fun `checkin_in_checkout_zone_checks_out`() =
        assertActivity(decide(match(MatchStatus.MATCHED, "Zona de CheckOut"), checkedIn("P80-Portaria")),
            CheckAction.CHECKOUT, "Zona de CheckOut")

    @Test fun `checkin_far_checks_out`() =
        assertActivity(decide(match(MatchStatus.OUTSIDE_WORKPLACE), checkedIn("P80-Portaria")),
            CheckAction.CHECKOUT, "Fora do Local de Trabalho")

    @Test fun `no_second_checkout_in_checkout_zone`() =
        assertNull(decide(match(MatchStatus.MATCHED, "Zona de CheckOut"), checkedOut("Zona de CheckOut")))

    @Test fun `no_second_checkout_when_far`() =
        assertNull(decide(match(MatchStatus.OUTSIDE_WORKPLACE), checkedOut("Fora do Local de Trabalho")))

    @Test fun `after_checkout_entering_area_checks_in`() =
        assertActivity(decide(match(MatchStatus.MATCHED, "P80-Portaria"), checkedOut("Zona de CheckOut")),
            CheckAction.CHECKIN, "P80-Portaria")

    @Test fun `mixed_zone_checkin_toggles_to_checkout`() =
        assertActivity(decide(
            match(MatchStatus.MATCHED, "Zona Mista"),
            checkedIn("Zona Mista", at = Instant.now().minusSeconds(20L * 60)), // cooldown elapsed
        ), CheckAction.CHECKOUT, "Zona Mista")

    @Test fun `mixed_zone_checkin_then_far_immediate_checkout`() =
        assertActivity(decide(match(MatchStatus.OUTSIDE_WORKPLACE), checkedIn("Zona Mista")),
            CheckAction.CHECKOUT, "Fora do Local de Trabalho")

    // ── Invariant: NEVER two consecutive check-outs ───────────────────────────────
    @Test fun `never_two_consecutive_checkouts`() {
        var s = checkedIn("P80-Portaria")
        val out = decide(match(MatchStatus.OUTSIDE_WORKPLACE), s)
        assertActivity(out, CheckAction.CHECKOUT, "Fora do Local de Trabalho") // 1st check-out
        s = apply(s, out) // now checked out
        assertNull(decide(match(MatchStatus.OUTSIDE_WORKPLACE), s))             // 2nd far read → no action
        assertNull(decide(match(MatchStatus.MATCHED, "Zona de CheckOut"), s))   // CheckOut zone → no action
    }

    // ── Invariant: after a check-out, the next automatic action is a check-in ─────
    @Test fun `after_checkout_next_action_is_checkin_then_checkout_cycle`() {
        var s = checkedOut("Zona de CheckOut")
        val cin = decide(match(MatchStatus.MATCHED, "P80-Portaria"), s)
        assertActivity(cin, CheckAction.CHECKIN, "P80-Portaria")  // check-out → check-in on entering area
        s = apply(s, cin)
        val cout = decide(match(MatchStatus.OUTSIDE_WORKPLACE), s)
        assertActivity(cout, CheckAction.CHECKOUT, "Fora do Local de Trabalho") // check-out still fires after
    }
}
