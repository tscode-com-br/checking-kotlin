package br.com.tscode.checking.domain.model

import java.time.Instant

enum class CheckAction { CHECKIN, CHECKOUT }

enum class InformeType { NORMAL, RETROATIVO }

enum class MatchStatus {
    MATCHED,
    ACCURACY_TOO_LOW,
    NOT_IN_KNOWN_LOCATION,
    OUTSIDE_WORKPLACE,
    NO_KNOWN_LOCATIONS,
}

data class HistoryState(
    val found: Boolean,
    val chave: String,
    val projeto: String?,
    val currentAction: CheckAction?,
    val currentLocal: String?,
    val hasCurrentDayCheckin: Boolean,
    val lastCheckinAt: Instant?,
    val lastCheckoutAt: Instant?,
    val transportEnabled: Boolean,
)

// One row of the per-user check-in/out history (change D). `time` is nullable: it is parsed from the
// server's ISO-8601 string at the repository boundary (same parser as HistoryState timestamps), and a
// null means "unparseable/absent" → the dialog renders "-" for that cell (mirrors HistoryCard).
data class CheckHistoryEntry(
    val action: CheckAction,
    val projeto: String,
    val local: String?,
    val time: Instant?,
    val informe: InformeType,
)

data class LocationMatch(
    val matched: Boolean,
    val resolvedLocal: String?,
    val label: String,
    val status: MatchStatus,
    val message: String,
    val accuracyMeters: Double?,
    val accuracyThresholdMeters: Int,
    val minimumCheckoutDistanceMeters: Int,
    val nearestWorkplaceDistanceMeters: Double?,
)

data class LocationOptions(
    val items: List<String>,
    val accuracyThresholdMeters: Int,
    val mixedZoneIntervalMinutes: Int,
)
