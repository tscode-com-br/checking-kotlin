package br.com.tscode.checking.domain.model

import java.time.Instant
import java.time.LocalDate

enum class TransportRequestKind { REGULAR, WEEKEND, EXTRA }
enum class TransportRequestStatus { PENDING, CONFIRMED, REJECTED, CANCELLED, REALIZED }
enum class RouteKind { HOME_TO_WORK, WORK_TO_HOME }
enum class VehicleType { CARRO, MINIVAN, VAN, ONIBUS }
enum class TransportOverallStatus { AVAILABLE, PENDING, CONFIRMED, REALIZED }

data class TransportRequest(
    val requestId: Int,
    val requestKind: TransportRequestKind,
    val status: TransportRequestStatus,
    val isActive: Boolean,
    val serviceDate: LocalDate?,
    val requestedTime: String?,
    val selectedWeekdays: List<Int>,
    val routeKind: RouteKind?,
    val boardingTime: String?,
    val confirmationDeadlineTime: String?,
    val vehicleType: VehicleType?,
    val vehiclePlate: String?,
    val vehicleColor: String?,
    val toleranceMinutes: Int?,
    val awarenessRequired: Boolean,
    val awarenessConfirmed: Boolean,
    val responseMessage: String?,
    val createdAt: Instant,
)

data class TransportState(
    val chave: String,
    val endRua: String?,
    val zip: String?,
    val status: TransportOverallStatus,
    val requestId: Int?,
    val requestKind: TransportRequestKind?,
    val routeKind: RouteKind?,
    val serviceDate: LocalDate?,
    val requestedTime: String?,
    val boardingTime: String?,
    val confirmationDeadlineTime: String?,
    val vehicleType: VehicleType?,
    val vehiclePlate: String?,
    val vehicleColor: String?,
    val toleranceMinutes: Int?,
    val awarenessRequired: Boolean,
    val awarenessConfirmed: Boolean,
    val requests: List<TransportRequest>,
)
