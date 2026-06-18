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
 * TP2 — exhaustively pins change A on the PURE engine via SEQUENCES of reads (the state evolves after
 * each decided action, mirroring how the server records it). No orchestrator, no mocks, no network.
 *
 * Deterministic, monotonic instants are used for the recorded timestamps; none of the cases here are
 * "Zona Mista", so the engine never consults `Instant.now()` (only the mixed-zone cooldown does).
 */
class LocationChangeContinuationTest {

    private val BASE: Instant = Instant.parse("2026-06-18T08:00:00Z")
    private var tick = 0L
    private fun advance(): Instant = BASE.plusSeconds((++tick) * 60)

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

    private fun checkedIn(local: String) = HistoryState(
        found = true, chave = "TP2X", projeto = "P80",
        currentAction = CheckAction.CHECKIN, currentLocal = local,
        hasCurrentDayCheckin = true, lastCheckinAt = advance(), lastCheckoutAt = null,
        transportEnabled = false,
    )

    private fun checkedOut(local: String) = HistoryState(
        found = true, chave = "TP2X", projeto = "P80",
        currentAction = CheckAction.CHECKOUT, currentLocal = local,
        hasCurrentDayCheckin = false, lastCheckinAt = null, lastCheckoutAt = advance(),
        transportEnabled = false,
    )

    /** Model how the server records a decided activity: check-in/out updates the recorded location + time. */
    private fun apply(prev: HistoryState, activity: AutomaticActivity?): HistoryState = when {
        activity == null -> prev
        activity.action == CheckAction.CHECKIN -> prev.copy(
            currentAction = CheckAction.CHECKIN, currentLocal = activity.local,
            lastCheckinAt = advance(), hasCurrentDayCheckin = true,
        )
        else -> prev.copy(
            currentAction = CheckAction.CHECKOUT, currentLocal = activity.local, lastCheckoutAt = advance(),
        )
    }

    private fun decide(match: LocationMatch, state: HistoryState?): AutomaticActivity? =
        resolveAutomaticActivityForMatch(match, state, mixedZoneIntervalMinutes = 15)

    private fun assertActivity(result: AutomaticActivity?, action: CheckAction, local: String?) {
        requireNotNull(result) { "expected an AutomaticActivity, got null" }
        assertEquals(action, result.action)
        assertEquals(local, result.local)
    }

    // 1. Repeated identical MATCHED reads → check-in only on the first (after a different prior location).
    @Test fun `repeated_identical_matched_reads_check_in_only_once`() {
        var s = checkedIn("P80-Refeitorio") // prior at a DIFFERENT area
        val r1 = decide(match(MatchStatus.MATCHED, "P80-Portaria"), s)
        assertActivity(r1, CheckAction.CHECKIN, "P80-Portaria") // moved → check-in
        s = apply(s, r1)
        assertNull(decide(match(MatchStatus.MATCHED, "P80-Portaria"), s)) // same → null
        assertNull(decide(match(MatchStatus.MATCHED, "P80-Portaria"), s)) // same again → null
    }

    // 2. P80-Portaria(in) → P80-Portaria again → P80-Refeitorio: only the move to Refeitorio checks in.
    @Test fun `only_the_move_to_a_new_area_checks_in`() {
        val s = checkedIn("P80-Portaria")
        assertNull(decide(match(MatchStatus.MATCHED, "P80-Portaria"), s)) // same → null
        assertActivity(
            decide(match(MatchStatus.MATCHED, "P80-Refeitorio"), s),
            CheckAction.CHECKIN, "P80-Refeitorio", // moved → check-in
        )
    }

    // 3. Continuation cycle: in@area → near-outside (Não Cadastrada) → still outside (null) → back inside.
    @Test fun `not_in_known_location_continuation_cycle`() {
        var s = checkedIn("P80-Portaria")
        var r = decide(match(MatchStatus.NOT_IN_KNOWN_LOCATION), s)
        assertActivity(r, CheckAction.CHECKIN, "Localização não Cadastrada") // change A continuation
        s = apply(s, r)
        assertNull(decide(match(MatchStatus.NOT_IN_KNOWN_LOCATION), s)) // already Não Cadastrada → null
        r = decide(match(MatchStatus.MATCHED, "P80-Portaria"), s)
        assertActivity(r, CheckAction.CHECKIN, "P80-Portaria") // back inside (changed) → check-in
    }

    // 4. Checked-out + near-but-outside → never check-in out-of-area.
    @Test fun `checkout_then_not_in_known_location_no_action`() {
        assertNull(decide(match(MatchStatus.NOT_IN_KNOWN_LOCATION), checkedOut("Zona de CheckOut")))
    }

    // 5. ACCURACY_TOO_LOW / NO_KNOWN_LOCATIONS → always null, regardless of last action.
    @Test fun `accuracy_too_low_is_always_null`() {
        assertNull(decide(match(MatchStatus.ACCURACY_TOO_LOW), checkedIn("P80-Portaria")))
        assertNull(decide(match(MatchStatus.ACCURACY_TOO_LOW), checkedOut("Zona de CheckOut")))
    }

    @Test fun `no_known_locations_is_always_null`() {
        assertNull(decide(match(MatchStatus.NO_KNOWN_LOCATIONS), checkedIn("P80-Portaria")))
        assertNull(decide(match(MatchStatus.NO_KNOWN_LOCATIONS), checkedOut("Zona de CheckOut")))
    }
}
