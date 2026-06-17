package br.com.tscode.checking.domain.clientstate

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val SINGAPORE: ZoneId = ZoneId.of("Asia/Singapore")
private val DAY_KEY_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

fun sanitizeSettingsChave(value: String?): String =
    (value ?: "")
        .uppercase()
        .replace(Regex("[^A-Z0-9]"), "")
        .take(4)

data class NotificationMessageSplit(val primary: String, val secondary: String)

fun splitNotificationMessage(message: String?, maxPrimaryLength: Int = 62): NotificationMessageSplit {
    val limit = if (maxPrimaryLength > 8) maxPrimaryLength else 62
    val rawText = (message ?: "").trim()
    if (rawText.isEmpty()) return NotificationMessageSplit("", "")

    val explicitLines = rawText.split(Regex("\r?\n")).map { it.trim() }.filter { it.isNotEmpty() }
    if (explicitLines.size > 1) {
        return NotificationMessageSplit(
            primary = explicitLines[0],
            secondary = explicitLines.drop(1).joinToString(" "),
        )
    }

    val normalized = rawText.replace(Regex("\\s+"), " ")
    if (normalized.length <= limit) return NotificationMessageSplit(normalized, "")

    var splitIndex = normalized.lastIndexOf(' ', limit)
    if (splitIndex < (limit * 0.55).toInt()) {
        splitIndex = normalized.indexOf(' ', limit)
    }
    if (splitIndex == -1) splitIndex = limit

    return NotificationMessageSplit(
        primary = normalized.substring(0, splitIndex).trim(),
        secondary = normalized.substring(splitIndex).trim(),
    )
}

fun normalizeProjectValue(
    projectValue: String?,
    allowedProjects: List<String>,
    fallbackProject: String,
): String {
    val normalized = (projectValue ?: "").trim().uppercase()
    return if (allowedProjects.contains(normalized)) normalized else fallbackProject
}

fun normalizeProjectValues(
    values: List<String>,
    allowedProjects: List<String>,
    fallbackProjects: List<String>,
): List<String> {
    val normalizedAllowed = allowedProjects.map { it.trim().uppercase() }.filter { it.isNotEmpty() }
    val allowedSet = normalizedAllowed.toSet()

    val result = mutableListOf<String>()
    val seen = mutableSetOf<String>()

    for (raw in values) {
        val n = raw.trim().uppercase()
        if (n.isEmpty() || seen.contains(n)) continue
        if (normalizedAllowed.isNotEmpty() && !allowedSet.contains(n)) continue
        seen.add(n)
        result.add(n)
    }

    if (result.isNotEmpty()) return result
    return fallbackProjects
        .map { it.trim().uppercase() }
        .filter { it.isNotEmpty() }
        .distinct()
}

fun resolveFallbackProjects(defaults: UserSettingsDefaults): List<String> {
    val allowed = defaults.allowedProjects.map { it.trim().uppercase() }.filter { it.isNotEmpty() }
    val rawDefaults = if (defaults.projects.isNotEmpty()) defaults.projects
    else if (defaults.project.isNotEmpty()) listOf(defaults.project)
    else emptyList()
    val normalized = rawDefaults.map { it.trim().uppercase() }.filter { it.isNotEmpty() }.distinct()
    val filtered = normalized.filter { allowed.contains(it) }

    if (filtered.isNotEmpty()) return filtered
    if (allowed.isNotEmpty()) return allowed
    return normalized
}

fun resolveFallbackProject(defaults: UserSettingsDefaults): String =
    resolveFallbackProjects(defaults).firstOrNull() ?: ""

fun resolveFallbackActiveProject(
    defaults: UserSettingsDefaults,
    fallbackProjects: List<String>,
): String {
    val normalized = fallbackProjects.map { it.trim().uppercase() }.filter { it.isNotEmpty() }
    val rawDefault = ((defaults.activeProject.takeIf { it.isNotEmpty() } ?: defaults.project))
        .trim().uppercase()
    return if (rawDefault.isNotEmpty() && normalized.contains(rawDefault)) rawDefault
    else normalized.firstOrNull() ?: rawDefault
}

fun shouldAttemptSilentLocationLookup(
    permissionState: String?,
    @Suppress("UNUSED_PARAMETER") hasPersistedGrant: Boolean,
): Boolean = permissionState != "denied"

data class ManualLocationSelectionInput(
    val automaticActivitiesEnabled: Boolean,
    val gpsLocationPermissionGranted: Boolean,
    val accuracyTooLowFallbackActive: Boolean,
)

fun shouldOfferManualLocationSelection(input: ManualLocationSelectionInput): Boolean {
    if (!input.automaticActivitiesEnabled) return true
    return !input.gpsLocationPermissionGranted || input.accuracyTooLowFallbackActive
}

fun autofillPetrobrasEmailDomain(value: String?): String {
    val raw = value ?: ""
    val atIndex = raw.indexOf('@')
    if (atIndex == -1) return raw
    val localPart = raw.substring(0, atIndex)
    val domainPart = raw.substring(atIndex + 1)
    if (localPart.isEmpty() || domainPart.isNotEmpty()) return raw
    return "$localPart@petrobras.com.br"
}

fun resolveCalendarDayKey(instant: Instant?, zoneId: ZoneId = SINGAPORE): String {
    if (instant == null) return ""
    return instant.atZone(zoneId).format(DAY_KEY_FMT)
}

fun hasCurrentDayCheckIn(
    hasCurrentDayCheckinField: Boolean?,
    lastCheckinAt: Instant?,
    referenceInstant: Instant = Instant.now(),
    zoneId: ZoneId = SINGAPORE,
): Boolean {
    if (hasCurrentDayCheckinField != null) return hasCurrentDayCheckinField
    if (lastCheckinAt == null) return false
    val checkinDayKey = resolveCalendarDayKey(lastCheckinAt, zoneId)
    val referenceDayKey = resolveCalendarDayKey(referenceInstant, zoneId)
    return checkinDayKey.isNotEmpty() && referenceDayKey.isNotEmpty() && checkinDayKey == referenceDayKey
}

fun formatTransportVehicleType(value: String?): String =
    when ((value ?: "").trim().lowercase()) {
        "carro" -> "carro"
        "minivan" -> "minivan"
        "van" -> "van"
        "onibus" -> "ônibus"
        else -> (value ?: "").trim()
    }

fun resolveAuthenticationPromptMessage(authenticated: Boolean, hasPassword: Boolean): String =
    when {
        authenticated -> ""
        hasPassword -> "Digite sua senha para iniciar."
        else -> "Digite sua chave e crie uma senha."
    }
