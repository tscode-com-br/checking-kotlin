package br.com.tscode.checking.domain.checkrules

import br.com.tscode.checking.domain.model.CheckAction
import br.com.tscode.checking.domain.model.HistoryState
import br.com.tscode.checking.domain.model.LocationMatch
import br.com.tscode.checking.domain.model.MatchStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

// Unit tests for AutoActivities.kt — the ported automatic-activities.js Situation engine.
// Vectors mirror the JS test cases to guarantee parity (T1.4 AC).
class AutoActivitiesTest {

    // ── Helpers ──────────────────────────────────────────────────────────────

    private val T0: Instant = Instant.parse("2024-01-01T08:00:00Z")
    private val T1: Instant = Instant.parse("2024-01-01T09:00:00Z")
    private val T2: Instant = Instant.parse("2024-01-01T10:00:00Z")

    private fun checkedInState(
        local: String = "Office A",
        lastCheckinAt: Instant = T1,
        lastCheckoutAt: Instant? = null,
    ) = HistoryState(
        found = true, chave = "TEST", projeto = null,
        currentAction = CheckAction.CHECKIN, currentLocal = local,
        hasCurrentDayCheckin = true,
        lastCheckinAt = lastCheckinAt, lastCheckoutAt = lastCheckoutAt,
        transportEnabled = false,
    )

    private fun checkedOutState(
        local: String? = null,
        lastCheckinAt: Instant? = T0,
        lastCheckoutAt: Instant = T1,
    ) = HistoryState(
        found = true, chave = "TEST", projeto = null,
        currentAction = CheckAction.CHECKOUT, currentLocal = local,
        hasCurrentDayCheckin = true,
        lastCheckinAt = lastCheckinAt, lastCheckoutAt = lastCheckoutAt,
        transportEnabled = false,
    )

    private fun firstRegistrationState() = HistoryState(
        found = true, chave = "TEST", projeto = null,
        currentAction = null, currentLocal = null,
        hasCurrentDayCheckin = false,
        lastCheckinAt = null, lastCheckoutAt = null,
        transportEnabled = false,
    )

    private fun matched(local: String = "Office A") = LocationMatch(
        matched = true, resolvedLocal = local, label = local,
        status = MatchStatus.MATCHED, message = "",
        accuracyMeters = 15.0, accuracyThresholdMeters = 30,
        minimumCheckoutDistanceMeters = 500, nearestWorkplaceDistanceMeters = null,
    )

    private fun outsideWorkplace(nearestM: Double = 1000.0) = LocationMatch(
        matched = false, resolvedLocal = null, label = "Outside",
        status = MatchStatus.OUTSIDE_WORKPLACE, message = "",
        accuracyMeters = 15.0, accuracyThresholdMeters = 30,
        minimumCheckoutDistanceMeters = 500, nearestWorkplaceDistanceMeters = nearestM,
    )

    private val defaultSettings = MixedZoneDecisionSettings(mixedZoneIntervalMinutes = 30)

    // ── resolveLastRecordedAction ─────────────────────────────────────────────

    @Test
    fun resolveLastRecordedAction_noBothTimestamps_returnsCurrentAction() {
        val state = HistoryState(
            found = true, chave = "T", projeto = null,
            currentAction = CheckAction.CHECKIN, currentLocal = null,
            hasCurrentDayCheckin = true,
            lastCheckinAt = null, lastCheckoutAt = null,
            transportEnabled = false,
        )
        assertEquals(CheckAction.CHECKIN, resolveLastRecordedAction(state))
    }

    @Test
    fun resolveLastRecordedAction_onlyCheckinTimestamp_returnsCheckin() {
        assertEquals(CheckAction.CHECKIN, resolveLastRecordedAction(checkedInState()))
    }

    @Test
    fun resolveLastRecordedAction_checkinNewerThanCheckout_returnsCheckin() {
        val state = checkedInState(lastCheckinAt = T2, lastCheckoutAt = T1)
        assertEquals(CheckAction.CHECKIN, resolveLastRecordedAction(state))
    }

    @Test
    fun resolveLastRecordedAction_checkoutNewerThanCheckin_returnsCheckout() {
        val state = checkedOutState(lastCheckinAt = T1, lastCheckoutAt = T2)
        assertEquals(CheckAction.CHECKOUT, resolveLastRecordedAction(state))
    }

    @Test
    fun resolveLastRecordedAction_nullState_returnsNull() {
        assertNull(resolveLastRecordedAction(null))
    }

    // ── shouldAttemptAutomaticOutOfRangeCheckout ──────────────────────────────

    @Test
    fun outOfRangeCheckout_checkedIn_returnsTrue() {
        // Situation 2: checked in, now outside workplace → should checkout
        assertTrue(shouldAttemptAutomaticOutOfRangeCheckout(outsideWorkplace(), checkedInState()))
    }

    @Test
    fun outOfRangeCheckout_checkedOut_returnsFalse() {
        // Situation 5-variant: already checked out, outside workplace → no action
        assertFalse(shouldAttemptAutomaticOutOfRangeCheckout(outsideWorkplace(), checkedOutState()))
    }

    @Test
    fun outOfRangeCheckout_firstRegistration_returnsFalse() {
        assertFalse(shouldAttemptAutomaticOutOfRangeCheckout(outsideWorkplace(), firstRegistrationState()))
    }

    @Test
    fun outOfRangeCheckout_matchedLocation_returnsFalse() {
        // Not OUTSIDE_WORKPLACE status → function returns false regardless
        assertFalse(shouldAttemptAutomaticOutOfRangeCheckout(matched(), checkedInState()))
    }

    @Test
    fun outOfRangeCheckout_nullMatch_returnsFalse() {
        assertFalse(shouldAttemptAutomaticOutOfRangeCheckout(null, checkedInState()))
    }

    // ── shouldAttemptAutomaticLocationEvent ───────────────────────────────────

    @Test
    fun locationEvent_checkoutZone_checkedIn_returnsTrue() {
        // Checkout zone and currently checked in → should checkout
        val checkoutZone = matched("Zona de Checkout")
        assertTrue(shouldAttemptAutomaticLocationEvent(checkoutZone, checkedInState(), defaultSettings))
    }

    @Test
    fun locationEvent_checkoutZone_checkedOut_returnsFalse() {
        // Checkout zone and already checked out → no action
        val checkoutZone = matched("Zona de Checkout")
        assertFalse(shouldAttemptAutomaticLocationEvent(checkoutZone, checkedOutState(), defaultSettings))
    }

    @Test
    fun locationEvent_regularLocation_checkedOut_returnsTrue() {
        // Situation 4/8: checked out, now at a known location → should check in
        assertTrue(shouldAttemptAutomaticLocationEvent(matched("Office A"), checkedOutState(), defaultSettings))
    }

    @Test
    fun locationEvent_regularLocation_firstRegistration_returnsTrue() {
        assertTrue(shouldAttemptAutomaticLocationEvent(matched("Office A"), firstRegistrationState(), defaultSettings))
    }

    @Test
    fun locationEvent_regularLocation_checkedIn_returnsTrue() {
        // Checked in, now at a different or same location → still attempt (server resolves idempotency)
        assertTrue(shouldAttemptAutomaticLocationEvent(matched("Office B"), checkedInState("Office A"), defaultSettings))
    }

    @Test
    fun locationEvent_nullMatch_checkedIn_returnsFalse() {
        // null resolvedLocal → normalizeLocationName("") = "" → isEmpty → false
        assertFalse(shouldAttemptAutomaticLocationEvent(null, checkedInState(), defaultSettings))
    }

    @Test
    fun locationEvent_nullMatch_checkedOut_returnsTrue() {
        // checked out + no location → still returns true (server/use-case guards the actual submission)
        assertTrue(shouldAttemptAutomaticLocationEvent(null, checkedOutState(), defaultSettings))
    }

    // ── resolveAutomaticLocationAction ────────────────────────────────────────

    @Test
    fun locationAction_checkoutZone_returnsCheckout() {
        assertEquals(
            CheckAction.CHECKOUT,
            resolveAutomaticLocationAction(matched("Zona de Checkout"), checkedInState()),
        )
    }

    @Test
    fun locationAction_regularZone_returnsCheckin() {
        assertEquals(
            CheckAction.CHECKIN,
            resolveAutomaticLocationAction(matched("Office A"), checkedOutState()),
        )
    }

    @Test
    fun locationAction_mixedZone_checkedIn_returnsCheckout() {
        assertEquals(
            CheckAction.CHECKOUT,
            resolveAutomaticLocationAction(matched("Zona Mista"), checkedInState()),
        )
    }

    @Test
    fun locationAction_mixedZone_checkedOut_returnsCheckin() {
        assertEquals(
            CheckAction.CHECKIN,
            resolveAutomaticLocationAction(matched("Zona Mista"), checkedOutState()),
        )
    }

    // NOTE: tests for resolveFirstRegistrationAutomaticActivity were removed in P5 — that
    // special-case was deleted to mirror the web app (no first-registration path; no-history is
    // handled by the normal flow). The no-history behaviour is now covered end-to-end in
    // AutoActivitiesSituationTest (no_history_* tests).

    // ── isMixedZoneCooldownActive ─────────────────────────────────────────────

    @Test
    fun mixedZoneCooldown_withinWindow_returnsTrue() {
        val checkinAt = T1
        val state = HistoryState(
            found = true, chave = "T", projeto = null,
            currentAction = CheckAction.CHECKIN, currentLocal = "Zona Mista",
            hasCurrentDayCheckin = true,
            lastCheckinAt = checkinAt, lastCheckoutAt = null,
            transportEnabled = false,
        )
        // Reference 20 min after checkin, cooldown = 30 min → still in cooldown
        val reference = checkinAt.plusSeconds(20 * 60)
        assertTrue(isMixedZoneCooldownActive(state, mixedZoneIntervalMinutes = 30, referenceTime = reference))
    }

    @Test
    fun mixedZoneCooldown_afterWindow_returnsFalse() {
        val checkinAt = T1
        val state = HistoryState(
            found = true, chave = "T", projeto = null,
            currentAction = CheckAction.CHECKIN, currentLocal = "Zona Mista",
            hasCurrentDayCheckin = true,
            lastCheckinAt = checkinAt, lastCheckoutAt = null,
            transportEnabled = false,
        )
        // Reference 40 min after checkin, cooldown = 30 min → outside cooldown
        val reference = checkinAt.plusSeconds(40 * 60)
        assertFalse(isMixedZoneCooldownActive(state, mixedZoneIntervalMinutes = 30, referenceTime = reference))
    }

    @Test
    fun mixedZoneCooldown_notInMixedZone_returnsFalse() {
        assertFalse(isMixedZoneCooldownActive(checkedInState("Office A"), 30))
    }

    @Test
    fun mixedZoneCooldown_zeroCooldown_returnsFalse() {
        val state = checkedInState("Zona Mista")
        assertFalse(isMixedZoneCooldownActive(state, mixedZoneIntervalMinutes = 0))
    }

    // ── normalizeLocationName ────────────────────────────────────────────────

    @Test
    fun normalizeLocationName_trimsCaseAndExtraSpaces() {
        assertEquals("zona mista", normalizeLocationName("  Zona  Mista  "))
    }

    @Test
    fun normalizeLocationName_nullReturnsEmpty() {
        assertEquals("", normalizeLocationName(null))
    }

    // ── isCheckoutZoneLocationName / isMixedZoneLocationName ────────────────

    @Test
    fun isCheckoutZoneLocationName_matchesCaseInsensitive() {
        assertTrue(isCheckoutZoneLocationName("ZONA DE CHECKOUT"))
        assertTrue(isCheckoutZoneLocationName("Zona de Checkout"))
        assertFalse(isCheckoutZoneLocationName("Office A"))
    }

    @Test
    fun isMixedZoneLocationName_matchesCaseInsensitive() {
        assertTrue(isMixedZoneLocationName("ZONA MISTA"))
        assertTrue(isMixedZoneLocationName("Zona Mista"))
        assertFalse(isMixedZoneLocationName("Office A"))
    }
}
