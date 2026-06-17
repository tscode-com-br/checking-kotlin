package br.com.tscode.checking.data.repository

import br.com.tscode.checking.BuildConfig
import br.com.tscode.checking.core.result.AppResult
import br.com.tscode.checking.data.api.TransportApi
import br.com.tscode.checking.data.dto.RouteKind as DtoRouteKind
import br.com.tscode.checking.data.dto.TransportOverallStatus as DtoOverallStatus
import br.com.tscode.checking.data.dto.TransportRequestKind as DtoKind
import br.com.tscode.checking.data.dto.TransportRequestStatus as DtoStatus
import br.com.tscode.checking.data.dto.VehicleType as DtoVehicleType
import br.com.tscode.checking.data.dto.WebTransportActionResponse
import br.com.tscode.checking.data.dto.WebTransportAddressUpdateRequest
import br.com.tscode.checking.data.dto.WebTransportRequestAction
import br.com.tscode.checking.data.dto.WebTransportRequestCreate
import br.com.tscode.checking.data.dto.WebTransportRequestItemResponse
import br.com.tscode.checking.data.dto.WebTransportStateResponse
import br.com.tscode.checking.data.remote.safeApiCall
import br.com.tscode.checking.data.remote.sse.sseFlow
import br.com.tscode.checking.di.SseClient
import br.com.tscode.checking.platform.connectivity.NetworkMonitor
import br.com.tscode.checking.domain.model.RouteKind
import br.com.tscode.checking.domain.model.TransportOverallStatus
import br.com.tscode.checking.domain.model.TransportRequest
import br.com.tscode.checking.domain.model.TransportRequestKind
import br.com.tscode.checking.domain.model.TransportRequestStatus
import br.com.tscode.checking.domain.model.TransportState
import br.com.tscode.checking.domain.model.VehicleType
import br.com.tscode.checking.domain.repository.TransportRepository
import kotlinx.coroutines.flow.Flow
import okhttp3.OkHttpClient
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransportRepositoryImpl @Inject constructor(
    private val api: TransportApi,
    @SseClient private val sseClient: OkHttpClient,
    private val networkMonitor: NetworkMonitor,
) : TransportRepository {

    override suspend fun getState(chave: String): AppResult<TransportState> = safeApiCall {
        api.getState(chave).toDomain()
    }

    override suspend fun updateAddress(
        chave: String,
        endRua: String,
        zip: String,
    ): AppResult<TransportState> = safeApiCall {
        api.updateAddress(WebTransportAddressUpdateRequest(chave, endRua, zip)).state.toDomain()
    }

    override suspend fun createRequest(
        chave: String,
        kind: TransportRequestKind,
        requestedTime: String?,
        requestedDate: String?,
        selectedWeekdays: List<Int>?,
    ): AppResult<TransportState> = safeApiCall {
        api.createRequest(
            WebTransportRequestCreate(
                chave = chave,
                requestKind = kind.toDto(),
                requestedTime = requestedTime,
                requestedDate = requestedDate,
                selectedWeekdays = selectedWeekdays,
            )
        ).state.toDomain()
    }

    override suspend fun cancelRequest(chave: String, requestId: Int): AppResult<TransportState> =
        safeApiCall {
            api.cancelRequest(WebTransportRequestAction(chave, requestId)).state.toDomain()
        }

    override suspend fun acknowledgeRequest(chave: String, requestId: Int): AppResult<TransportState> =
        safeApiCall {
            api.acknowledgeRequest(WebTransportRequestAction(chave, requestId)).state.toDomain()
        }

    override fun streamEvents(chave: String): Flow<String> {
        val url = "${BuildConfig.BASE_URL}${BuildConfig.API_PREFIX}/transport/stream?chave=$chave"
        return sseFlow(sseClient, url, networkMonitor)
    }

    // ---- DTO → domain ----

    private fun WebTransportStateResponse.toDomain() = TransportState(
        chave = chave,
        endRua = endRua,
        zip = zip,
        status = status.toDomain(),
        requestId = requestId,
        requestKind = requestKind?.toDomain(),
        routeKind = routeKind?.toDomain(),
        serviceDate = serviceDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
        requestedTime = requestedTime,
        boardingTime = boardingTime,
        confirmationDeadlineTime = confirmationDeadlineTime,
        vehicleType = vehicleType?.toDomain(),
        vehiclePlate = vehiclePlate,
        vehicleColor = vehicleColor,
        toleranceMinutes = toleranceMinutes,
        awarenessRequired = awarenessRequired,
        awarenessConfirmed = awarenessConfirmed,
        requests = requests.map { it.toDomain() },
    )

    private fun WebTransportRequestItemResponse.toDomain() = TransportRequest(
        requestId = requestId,
        requestKind = requestKind.toDomain(),
        status = status.toDomain(),
        isActive = isActive,
        serviceDate = serviceDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
        requestedTime = requestedTime,
        selectedWeekdays = selectedWeekdays,
        routeKind = routeKind?.toDomain(),
        boardingTime = boardingTime,
        confirmationDeadlineTime = confirmationDeadlineTime,
        vehicleType = vehicleType?.toDomain(),
        vehiclePlate = vehiclePlate,
        vehicleColor = vehicleColor,
        toleranceMinutes = toleranceMinutes,
        awarenessRequired = awarenessRequired,
        awarenessConfirmed = awarenessConfirmed,
        responseMessage = responseMessage,
        createdAt = runCatching { Instant.parse(createdAt) }.getOrElse { Instant.EPOCH },
    )

    private fun DtoOverallStatus.toDomain() = when (this) {
        DtoOverallStatus.AVAILABLE -> TransportOverallStatus.AVAILABLE
        DtoOverallStatus.PENDING -> TransportOverallStatus.PENDING
        DtoOverallStatus.CONFIRMED -> TransportOverallStatus.CONFIRMED
        DtoOverallStatus.REALIZED -> TransportOverallStatus.REALIZED
    }

    private fun DtoKind.toDomain() = when (this) {
        DtoKind.REGULAR -> TransportRequestKind.REGULAR
        DtoKind.WEEKEND -> TransportRequestKind.WEEKEND
        DtoKind.EXTRA -> TransportRequestKind.EXTRA
    }

    private fun TransportRequestKind.toDto() = when (this) {
        TransportRequestKind.REGULAR -> DtoKind.REGULAR
        TransportRequestKind.WEEKEND -> DtoKind.WEEKEND
        TransportRequestKind.EXTRA -> DtoKind.EXTRA
    }

    private fun DtoStatus.toDomain() = when (this) {
        DtoStatus.PENDING -> TransportRequestStatus.PENDING
        DtoStatus.CONFIRMED -> TransportRequestStatus.CONFIRMED
        DtoStatus.REJECTED -> TransportRequestStatus.REJECTED
        DtoStatus.CANCELLED -> TransportRequestStatus.CANCELLED
        DtoStatus.REALIZED -> TransportRequestStatus.REALIZED
    }

    private fun DtoRouteKind.toDomain() = when (this) {
        DtoRouteKind.HOME_TO_WORK -> RouteKind.HOME_TO_WORK
        DtoRouteKind.WORK_TO_HOME -> RouteKind.WORK_TO_HOME
    }

    private fun DtoVehicleType.toDomain() = when (this) {
        DtoVehicleType.CARRO -> VehicleType.CARRO
        DtoVehicleType.MINIVAN -> VehicleType.MINIVAN
        DtoVehicleType.VAN -> VehicleType.VAN
        DtoVehicleType.ONIBUS -> VehicleType.ONIBUS
    }
}
