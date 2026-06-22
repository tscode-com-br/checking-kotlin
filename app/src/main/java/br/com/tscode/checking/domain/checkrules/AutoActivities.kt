package br.com.tscode.checking.domain.checkrules

import br.com.tscode.checking.domain.model.CheckAction
import br.com.tscode.checking.domain.model.HistoryState
import br.com.tscode.checking.domain.model.LocationMatch
import br.com.tscode.checking.domain.model.MatchStatus
import java.time.Instant

const val AUTOMATIC_CHECKOUT_LOCATION = "Fora do Local de Trabalho"
const val AUTOMATIC_UNREGISTERED_CHECKIN_LOCATION = "Localização não Cadastrada"
const val MIXED_ZONE_LOCATION = "Zona Mista"

fun normalizeLocationName(value: String?): String =
    (value ?: "").trim().replace(Regex("\\s+"), " ").lowercase()

fun isCheckoutZoneLocationName(value: String?): Boolean =
    normalizeLocationName(value) == "zona de checkout"

fun isMixedZoneLocationName(value: String?): Boolean =
    normalizeLocationName(value) == "zona mista"

fun resolveLastRecordedAction(state: HistoryState?): CheckAction? {
    val lastCheckinAt = state?.lastCheckinAt
    val lastCheckoutAt = state?.lastCheckoutAt
    if (lastCheckinAt == null && lastCheckoutAt == null) return state?.currentAction
    if (lastCheckinAt != null && lastCheckoutAt == null) return CheckAction.CHECKIN
    if (lastCheckinAt == null && lastCheckoutAt != null) return CheckAction.CHECKOUT
    return when {
        lastCheckinAt!! > lastCheckoutAt!! -> CheckAction.CHECKIN
        lastCheckoutAt > lastCheckinAt -> CheckAction.CHECKOUT
        else -> state?.currentAction
    }
}

fun resolveRecordedCheckInLocation(state: HistoryState?): String? =
    if (state?.currentAction == CheckAction.CHECKIN) state.currentLocal else null

fun resolveCurrentRecordedLocation(state: HistoryState?): String? = state?.currentLocal

fun resolveRecordedActionTimestamp(state: HistoryState?, action: CheckAction?): Instant? =
    when (action) {
        CheckAction.CHECKIN -> state?.lastCheckinAt
        CheckAction.CHECKOUT -> state?.lastCheckoutAt
        null -> null
    }

data class MixedZoneActivity(
    val action: CheckAction,
    val local: String,
    val timestamp: Instant,
)

fun resolveLastRelevantMixedZoneActivity(state: HistoryState?): MixedZoneActivity? {
    val currentRecordedLocation = resolveCurrentRecordedLocation(state)
    if (!isMixedZoneLocationName(currentRecordedLocation)) return null
    val lastRecordedAction = resolveLastRecordedAction(state) ?: return null
    if (lastRecordedAction != CheckAction.CHECKIN && lastRecordedAction != CheckAction.CHECKOUT) return null
    val timestamp = resolveRecordedActionTimestamp(state, lastRecordedAction) ?: return null
    return MixedZoneActivity(lastRecordedAction, currentRecordedLocation!!, timestamp)
}

fun isLastRelevantActivityInMixedZone(state: HistoryState?): Boolean =
    resolveLastRelevantMixedZoneActivity(state) != null

fun resolveMixedZoneCooldownMilliseconds(mixedZoneIntervalMinutes: Int): Long {
    if (mixedZoneIntervalMinutes < 1) return 0L
    return mixedZoneIntervalMinutes.toLong() * 60 * 1000
}

data class MixedZoneDecisionSettings(
    val mixedZoneIntervalMinutes: Int,
    val referenceTime: Instant? = null,
)

fun isMixedZoneCooldownActive(
    state: HistoryState?,
    mixedZoneIntervalMinutes: Int,
    referenceTime: Instant? = null,
): Boolean {
    val lastMixedZoneActivity = resolveLastRelevantMixedZoneActivity(state) ?: return false
    val cooldownMs = resolveMixedZoneCooldownMilliseconds(mixedZoneIntervalMinutes)
    if (cooldownMs <= 0) return false
    val reference = referenceTime ?: Instant.now()
    return (reference.toEpochMilli() - lastMixedZoneActivity.timestamp.toEpochMilli()) < cooldownMs
}

fun resolveLastRecordedActivityTimestamp(state: HistoryState?): Instant? {
    val lastRecordedAction = resolveLastRecordedAction(state)
    if (lastRecordedAction != CheckAction.CHECKIN && lastRecordedAction != CheckAction.CHECKOUT) return null
    return resolveRecordedActionTimestamp(state, lastRecordedAction)
}

// temp006: cooldown da 'Zona Mista' baseado na ÚLTIMA atividade registrada (check-in OU check-out, em
// QUALQUER localização) — diferente de isMixedZoneCooldownActive, que exige que o currentLocal já seja a
// própria 'Zona Mista'. Usado para suprimir o toggle espúrio por drift de GPS no Branch B.
fun isMixedZoneCooldownActiveForLastActivity(
    state: HistoryState?,
    mixedZoneIntervalMinutes: Int,
    referenceTime: Instant? = null,
): Boolean {
    val lastTimestamp = resolveLastRecordedActivityTimestamp(state) ?: return false
    val cooldownMs = resolveMixedZoneCooldownMilliseconds(mixedZoneIntervalMinutes)
    if (cooldownMs <= 0) return false
    val reference = referenceTime ?: Instant.now()
    return (reference.toEpochMilli() - lastTimestamp.toEpochMilli()) < cooldownMs
}

fun resolveAutomaticCheckInLocation(locationMatch: LocationMatch?): String? =
    locationMatch?.resolvedLocal?.trim()?.takeIf { it.isNotEmpty() }

fun isOperationalAutomaticCheckInLocation(
    locationMatch: LocationMatch?,
    automaticLocal: String?,
): Boolean {
    val resolvedLocal = locationMatch?.resolvedLocal?.trim() ?: ""
    val candidateLocal = (automaticLocal ?: "").trim()
    return resolvedLocal.isNotEmpty() && candidateLocal == resolvedLocal
}

fun shouldAttemptAutomaticMixedZoneLocationEvent(
    locationMatch: LocationMatch?,
    remoteState: HistoryState?,
    settings: MixedZoneDecisionSettings,
): Boolean {
    val resolvedLocal = locationMatch?.resolvedLocal
    if (!isMixedZoneLocationName(resolvedLocal)) return false

    val lastRecordedAction = resolveLastRecordedAction(remoteState)
    val currentRecordedLocation = resolveCurrentRecordedLocation(remoteState)
    val lastCheckInLocation = resolveRecordedCheckInLocation(remoteState)
    val cooldownMs = resolveMixedZoneCooldownMilliseconds(settings.mixedZoneIntervalMinutes)

    if (normalizeLocationName(resolvedLocal).isNotEmpty() &&
        normalizeLocationName(resolvedLocal) == normalizeLocationName(currentRecordedLocation)
    ) {
        if (!isLastRelevantActivityInMixedZone(remoteState) || cooldownMs <= 0) return false
        return !isMixedZoneCooldownActive(remoteState, settings.mixedZoneIntervalMinutes, settings.referenceTime)
    }

    // temp006 — gate de cooldown no Branch B (estado registrado em OUTRA localização ≠ 'Zona Mista'):
    // suprime a alternância automática se a ÚLTIMA atividade registrada (check-in OU check-out, em QUALQUER
    // localização) ocorreu há menos que o intervalo. Evita o toggle espúrio por drift de GPS entre a 'Zona
    // Mista' e localizações adjacentes (ex.: 'Escritório Principal'). Saídas/entradas genuínas resolvem para
    // OUTRO resolvedLocal (Zona de CheckOut / OUTSIDE_WORKPLACE / outra área cadastrada) e seguem pelos ramos
    // separados (exceções imediatas da Situação 8), que permanecem inalterados.
    if (isMixedZoneCooldownActiveForLastActivity(remoteState, settings.mixedZoneIntervalMinutes, settings.referenceTime)) {
        return false
    }

    if (lastRecordedAction != CheckAction.CHECKIN) return true

    return normalizeLocationName(resolvedLocal) != normalizeLocationName(lastCheckInLocation)
}

// Situations 1-4, 6-8: decides whether to fire an automatic event for a *matched* location.
fun shouldAttemptAutomaticLocationEvent(
    locationMatch: LocationMatch?,
    remoteState: HistoryState?,
    settings: MixedZoneDecisionSettings,
): Boolean {
    val resolvedLocal = locationMatch?.resolvedLocal
    val lastRecordedAction = resolveLastRecordedAction(remoteState)

    if (isCheckoutZoneLocationName(resolvedLocal)) {
        return lastRecordedAction == CheckAction.CHECKIN
    }

    if (isMixedZoneLocationName(resolvedLocal)) {
        return shouldAttemptAutomaticMixedZoneLocationEvent(locationMatch, remoteState, settings)
    }

    if (lastRecordedAction != CheckAction.CHECKIN) return true

    // Change A (P6.1): a re-check-in at a MATCHED area fires ONLY when the resolved location differs
    // from the last recorded check-in location — suppress same-location re-check-in (the root-cause fix
    // for the duplicate check-in). Mirrors the mixed-zone "different location" check above. The
    // last-was-check-out case and the checkout-zone/mixed-zone branches are untouched.
    val lastCheckInLocation = resolveRecordedCheckInLocation(remoteState)
    return normalizeLocationName(resolvedLocal).isNotEmpty() &&
        normalizeLocationName(resolvedLocal) != normalizeLocationName(lastCheckInLocation)
}

// Situation 1 (out-of-range variant) + Situation 2: fires checkout when outside_workplace.
fun shouldAttemptAutomaticOutOfRangeCheckout(
    locationMatch: LocationMatch?,
    remoteState: HistoryState?,
): Boolean {
    if (locationMatch == null || locationMatch.status != MatchStatus.OUTSIDE_WORKPLACE) return false
    return resolveLastRecordedAction(remoteState) == CheckAction.CHECKIN
}

// Situation 5: nearby but not in a known location — never a valid automatic check-in target.
@Suppress("UNUSED_PARAMETER")
fun shouldAttemptAutomaticNearbyWorkplaceCheckIn(
    locationMatch: LocationMatch?,
    remoteState: HistoryState?,
): Boolean = false

// Resolves the check action for an automatic event given a matched location and current state.
// Mixed zone toggles from the last recorded action; checkout zone → checkout; else → checkin.
fun resolveAutomaticLocationAction(
    locationMatch: LocationMatch?,
    remoteState: HistoryState?,
): CheckAction {
    val resolvedLocal = locationMatch?.resolvedLocal
    if (isMixedZoneLocationName(resolvedLocal)) {
        return if (resolveLastRecordedAction(remoteState) == CheckAction.CHECKIN) {
            CheckAction.CHECKOUT
        } else {
            CheckAction.CHECKIN
        }
    }
    return if (isCheckoutZoneLocationName(resolvedLocal)) CheckAction.CHECKOUT else CheckAction.CHECKIN
}

data class AutomaticActivity(
    val action: CheckAction,
    val local: String?,
)

// Decides the automatic activity (if any) for a location match. SINGLE SOURCE OF TRUTH for the
// situation engine — used by both the live flow (RunAutomaticActivitiesUseCase) and the offline
// replay (SyncPendingChecksWorker, P8). Mirrors the web app's three branches
// (sistema/app/static/check/automatic-activities.js):
//   - OUTSIDE_WORKPLACE → check-out only if the last action was a check-in (Situations 1-far, 2).
//   - MATCHED           → check-in/out/toggle per the matched area (Situations 1-CheckOut,3,4,6,7,8);
//     a re-check-in fires only on a location CHANGE (change A / P6.1).
//   - NOT_IN_KNOWN_LOCATION → "near but not inside any registered area". If the user is currently
//     checked IN at a registered area, record a check-in at "Localização não Cadastrada" as a CHANGE
//     (change A continuation / P6.2) — skipped if the last check-in was already that, and never for a
//     checked-out user. This requires the app's Phase-5 backend relaxation (X-Client: checking-android),
//     since /api/web/check still rejects this local (HTTP 422) for the web app and any check-out.
//   - NO_KNOWN_LOCATIONS / ACCURACY_TOO_LOW → no action (covers Situation 5 and low-accuracy).
fun resolveAutomaticActivityForMatch(
    match: LocationMatch,
    currentState: HistoryState?,
    mixedZoneIntervalMinutes: Int,
): AutomaticActivity? {
    val settings = MixedZoneDecisionSettings(mixedZoneIntervalMinutes)

    if (match.status == MatchStatus.OUTSIDE_WORKPLACE) {
        return if (shouldAttemptAutomaticOutOfRangeCheckout(match, currentState)) {
            AutomaticActivity(action = CheckAction.CHECKOUT, local = AUTOMATIC_CHECKOUT_LOCATION)
        } else {
            null
        }
    }

    if (match.status == MatchStatus.MATCHED) {
        if (!shouldAttemptAutomaticLocationEvent(match, currentState, settings)) return null
        val action = resolveAutomaticLocationAction(match, currentState)
        return AutomaticActivity(action = action, local = match.resolvedLocal)
    }

    if (match.status == MatchStatus.NOT_IN_KNOWN_LOCATION) {
        // Change A continuation (P6.2): only as a CHANGE, and only for a currently-checked-in user.
        val lastCheckInLocation = resolveRecordedCheckInLocation(currentState)
        return if (resolveLastRecordedAction(currentState) == CheckAction.CHECKIN &&
            normalizeLocationName(lastCheckInLocation) !=
            normalizeLocationName(AUTOMATIC_UNREGISTERED_CHECKIN_LOCATION)
        ) {
            AutomaticActivity(action = CheckAction.CHECKIN, local = AUTOMATIC_UNREGISTERED_CHECKIN_LOCATION)
        } else {
            null
        }
    }

    return null
}

// NOTE: there is intentionally NO first-registration special-case. The web reference
// (sistema/app/static/check/automatic-activities.js + app.js) has none: a no-history user is
// handled by the normal flow, where resolveLastRecordedAction()==null means a matched non-checkout
// area yields the first check-in, while NOT_IN_KNOWN_LOCATION / OUTSIDE_WORKPLACE / "Zona de
// CheckOut" produce no action (you cannot check out a user who never checked in). See
// resolveAutomaticActivityForMatch above.
