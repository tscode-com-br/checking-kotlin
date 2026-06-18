package br.com.tscode.checking.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class CheckAction {
    @SerialName("checkin") CHECKIN,
    @SerialName("checkout") CHECKOUT,
}

@Serializable
enum class InformeType {
    @SerialName("normal") NORMAL,
    @SerialName("retroativo") RETROATIVO,
}

@Serializable
enum class LocationMatchStatus {
    @SerialName("matched") MATCHED,
    @SerialName("accuracy_too_low") ACCURACY_TOO_LOW,
    @SerialName("not_in_known_location") NOT_IN_KNOWN_LOCATION,
    @SerialName("outside_workplace") OUTSIDE_WORKPLACE,
    @SerialName("no_known_locations") NO_KNOWN_LOCATIONS,
}

@Serializable
data class WebLocationMatchRequest(
    val latitude: Double,
    val longitude: Double,
    @SerialName("accuracy_meters") val accuracyMeters: Double? = null,
)

@Serializable
data class WebLocationMatchResponse(
    val matched: Boolean,
    @SerialName("resolved_local") val resolvedLocal: String? = null,
    val label: String,
    val status: LocationMatchStatus,
    val message: String,
    @SerialName("accuracy_meters") val accuracyMeters: Double? = null,
    @SerialName("accuracy_threshold_meters") val accuracyThresholdMeters: Int,
    @SerialName("minimum_checkout_distance_meters") val minimumCheckoutDistanceMeters: Int,
    @SerialName("nearest_workplace_distance_meters") val nearestWorkplaceDistanceMeters: Double? = null,
)

// Timestamps as ISO-8601 strings; parsed to domain types at the repository boundary.
@Serializable
data class WebCheckHistoryResponse(
    val found: Boolean,
    val chave: String,
    val projeto: String? = null,
    @SerialName("current_action") val currentAction: CheckAction? = null,
    @SerialName("current_local") val currentLocal: String? = null,
    @SerialName("has_current_day_checkin") val hasCurrentDayCheckin: Boolean,
    @SerialName("last_checkin_at") val lastCheckinAt: String? = null,
    @SerialName("last_checkout_at") val lastCheckoutAt: String? = null,
    @SerialName("transport_enabled") val transportEnabled: Boolean,
)

// GET /check/history (change D). `time` is an ISO-8601 string parsed at the repository boundary,
// exactly like the timestamps in WebCheckHistoryResponse above.
@Serializable
data class WebCheckHistoryItemDto(
    val action: CheckAction,
    val projeto: String,
    val local: String? = null,
    val time: String,
    val informe: InformeType,
)

@Serializable
data class WebCheckHistoryListResponseDto(
    val items: List<WebCheckHistoryItemDto> = emptyList(),
)

@Serializable
data class WebLocationOptionsResponse(
    val items: List<String>,
    @SerialName("location_accuracy_threshold_meters") val locationAccuracyThresholdMeters: Int,
    @SerialName("mixed_zone_interval_minutes") val mixedZoneIntervalMinutes: Int,
)

@Serializable
data class WebCheckSubmitRequest(
    val chave: String,
    val projeto: String,
    val action: CheckAction,
    val local: String? = null,
    val informe: InformeType,
    @SerialName("event_time") val eventTime: String,
    @SerialName("client_event_id") val clientEventId: String,
)

@Serializable
data class MobileSyncStateResponse(
    val found: Boolean,
    val chave: String,
    val nome: String? = null,
    val projeto: String? = null,
    @SerialName("current_action") val currentAction: CheckAction? = null,
    @SerialName("current_event_time") val currentEventTime: String? = null,
    @SerialName("current_local") val currentLocal: String? = null,
    @SerialName("last_checkin_at") val lastCheckinAt: String? = null,
    @SerialName("last_checkout_at") val lastCheckoutAt: String? = null,
)

@Serializable
data class MobileSubmitResponse(
    val ok: Boolean,
    val duplicate: Boolean = false,
    // Server returns a boolean (queued_forms: bool), not an Int — a type mismatch here made
    // kotlinx.serialization throw, so every submit (manual and automatic) failed to parse.
    @SerialName("queued_forms") val queuedForms: Boolean = true,
    @SerialName("worker_healthy") val workerHealthy: Boolean = true,
    val message: String = "",
    val state: MobileSyncStateResponse,
)

typealias WebCheckSubmitResponse = MobileSubmitResponse
