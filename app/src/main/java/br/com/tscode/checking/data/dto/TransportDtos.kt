package br.com.tscode.checking.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TransportRequestKind {
    @SerialName("regular") REGULAR,
    @SerialName("weekend") WEEKEND,
    @SerialName("extra") EXTRA,
}

@Serializable
enum class TransportRequestStatus {
    @SerialName("pending") PENDING,
    @SerialName("confirmed") CONFIRMED,
    @SerialName("rejected") REJECTED,
    @SerialName("cancelled") CANCELLED,
    @SerialName("realized") REALIZED,
}

@Serializable
enum class RouteKind {
    @SerialName("home_to_work") HOME_TO_WORK,
    @SerialName("work_to_home") WORK_TO_HOME,
}

@Serializable
enum class VehicleType {
    @SerialName("carro") CARRO,
    @SerialName("minivan") MINIVAN,
    @SerialName("van") VAN,
    @SerialName("onibus") ONIBUS,
}

@Serializable
enum class TransportOverallStatus {
    @SerialName("available") AVAILABLE,
    @SerialName("pending") PENDING,
    @SerialName("confirmed") CONFIRMED,
    @SerialName("realized") REALIZED,
}

// Timestamps and dates as ISO-8601 strings; parsed to domain types at the repository boundary.
@Serializable
data class WebTransportRequestItemResponse(
    @SerialName("request_id") val requestId: Int,
    @SerialName("request_kind") val requestKind: TransportRequestKind,
    val status: TransportRequestStatus,
    @SerialName("is_active") val isActive: Boolean,
    @SerialName("service_date") val serviceDate: String? = null,
    @SerialName("requested_time") val requestedTime: String? = null,
    @SerialName("selected_weekdays") val selectedWeekdays: List<Int>,
    @SerialName("route_kind") val routeKind: RouteKind? = null,
    @SerialName("boarding_time") val boardingTime: String? = null,
    @SerialName("confirmation_deadline_time") val confirmationDeadlineTime: String? = null,
    @SerialName("vehicle_type") val vehicleType: VehicleType? = null,
    @SerialName("vehicle_plate") val vehiclePlate: String? = null,
    @SerialName("vehicle_color") val vehicleColor: String? = null,
    @SerialName("tolerance_minutes") val toleranceMinutes: Int? = null,
    @SerialName("awareness_required") val awarenessRequired: Boolean,
    @SerialName("awareness_confirmed") val awarenessConfirmed: Boolean,
    @SerialName("response_message") val responseMessage: String? = null,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class WebTransportStateResponse(
    val chave: String,
    @SerialName("end_rua") val endRua: String? = null,
    val zip: String? = null,
    val status: TransportOverallStatus,
    @SerialName("request_id") val requestId: Int? = null,
    @SerialName("request_kind") val requestKind: TransportRequestKind? = null,
    @SerialName("route_kind") val routeKind: RouteKind? = null,
    @SerialName("service_date") val serviceDate: String? = null,
    @SerialName("requested_time") val requestedTime: String? = null,
    @SerialName("boarding_time") val boardingTime: String? = null,
    @SerialName("confirmation_deadline_time") val confirmationDeadlineTime: String? = null,
    @SerialName("vehicle_type") val vehicleType: VehicleType? = null,
    @SerialName("vehicle_plate") val vehiclePlate: String? = null,
    @SerialName("vehicle_color") val vehicleColor: String? = null,
    @SerialName("tolerance_minutes") val toleranceMinutes: Int? = null,
    @SerialName("awareness_required") val awarenessRequired: Boolean,
    @SerialName("awareness_confirmed") val awarenessConfirmed: Boolean,
    val requests: List<WebTransportRequestItemResponse>,
)

@Serializable
data class WebTransportActionResponse(
    val ok: Boolean,
    val message: String,
    val state: WebTransportStateResponse,
)

@Serializable
data class WebTransportAddressUpdateRequest(
    val chave: String,
    @SerialName("end_rua") val endRua: String,
    val zip: String,
)

@Serializable
data class WebTransportRequestCreate(
    val chave: String,
    @SerialName("request_kind") val requestKind: TransportRequestKind,
    @SerialName("requested_time") val requestedTime: String? = null,
    @SerialName("requested_date") val requestedDate: String? = null,
    @SerialName("selected_weekdays") val selectedWeekdays: List<Int>? = null,
)

@Serializable
data class WebTransportRequestAction(
    val chave: String,
    @SerialName("request_id") val requestId: Int,
)
