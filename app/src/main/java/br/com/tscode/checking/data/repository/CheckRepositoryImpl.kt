package br.com.tscode.checking.data.repository

import br.com.tscode.checking.core.result.AppResult
import br.com.tscode.checking.core.time.Clock
import br.com.tscode.checking.data.api.CheckApi
import br.com.tscode.checking.data.dto.GeofenceCircleDto
import br.com.tscode.checking.data.dto.WebCheckSubmitRequest
import br.com.tscode.checking.data.dto.WebLocationMatchRequest
import br.com.tscode.checking.data.dto.CheckAction as DtoCheckAction
import br.com.tscode.checking.data.dto.InformeType as DtoInformeType
import br.com.tscode.checking.data.dto.LocationMatchStatus
import br.com.tscode.checking.data.dto.MobileSyncStateResponse
import br.com.tscode.checking.data.dto.WebCheckHistoryItemDto
import br.com.tscode.checking.data.dto.WebCheckHistoryResponse
import br.com.tscode.checking.data.dto.WebLocationMatchResponse
import br.com.tscode.checking.data.remote.safeApiCall
import br.com.tscode.checking.data.remote.sse.CheckEventStream
import br.com.tscode.checking.domain.model.CheckAction
import br.com.tscode.checking.domain.model.CheckHistoryEntry
import br.com.tscode.checking.domain.model.GeofenceCircle
import br.com.tscode.checking.domain.model.HistoryState
import br.com.tscode.checking.domain.model.InformeType
import br.com.tscode.checking.domain.model.LocationMatch
import br.com.tscode.checking.domain.model.LocationOptions
import br.com.tscode.checking.domain.model.MatchStatus
import br.com.tscode.checking.domain.repository.CheckRepository
import kotlinx.coroutines.flow.Flow
import java.time.Duration
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CheckRepositoryImpl @Inject constructor(
    private val checkApi: CheckApi,
    private val clock: Clock,
    private val checkEventStream: CheckEventStream,
) : CheckRepository {

    override fun streamEvents(chave: String): Flow<String> = checkEventStream.events(chave)

    // Geofence circle cache — TTL-based, keyed by chave.
    // Two concurrent stale callers may both fetch; benign: last writer wins.
    @Volatile private var geofenceCache: List<GeofenceCircle>? = null
    @Volatile private var geofenceCachedChave: String? = null
    @Volatile private var geofenceCachedAt: Instant = Instant.EPOCH

    override suspend fun getState(chave: String): AppResult<HistoryState> = safeApiCall {
        checkApi.getState(chave).toDomain()
    }

    override suspend fun getHistory(chave: String): AppResult<List<CheckHistoryEntry>> = safeApiCall {
        checkApi.getHistory(chave).items.map { it.toDomain() }
    }

    override suspend fun getLocations(): AppResult<LocationOptions> = safeApiCall {
        val r = checkApi.getLocations()
        LocationOptions(
            items = r.items,
            accuracyThresholdMeters = r.locationAccuracyThresholdMeters,
            mixedZoneIntervalMinutes = r.mixedZoneIntervalMinutes,
        )
    }

    override suspend fun matchLocation(
        lat: Double,
        lon: Double,
        accuracy: Double?,
    ): AppResult<LocationMatch> = safeApiCall {
        checkApi.matchLocation(WebLocationMatchRequest(lat, lon, accuracy)).toDomain()
    }

    override suspend fun submit(
        chave: String,
        projeto: String,
        action: CheckAction,
        local: String?,
        informe: InformeType,
        eventTime: Instant?,
        clientEventId: String?,
    ): AppResult<HistoryState> = safeApiCall {
        val request = WebCheckSubmitRequest(
            chave = chave,
            projeto = projeto,
            action = action.toDto(),
            local = local,
            informe = informe.toDto(),
            eventTime = (eventTime ?: clock.now()).toString(),
            clientEventId = clientEventId ?: UUID.randomUUID().toString(),
        )
        checkApi.submit(request).state.toHistoryState()
    }

    override suspend fun getGeofences(chave: String): AppResult<List<GeofenceCircle>> {
        val now = clock.now()
        val cached = geofenceCache
        if (cached != null &&
            chave == geofenceCachedChave &&
            Duration.between(geofenceCachedAt, now) < GEOFENCE_CACHE_TTL
        ) {
            return AppResult.Success(cached)
        }
        return safeApiCall {
            checkApi.getGeofences(chave).locations.map { it.toDomain() }
        }.also { result ->
            if (result is AppResult.Success) {
                geofenceCache = result.data
                geofenceCachedChave = chave
                geofenceCachedAt = now
            }
        }
    }

    private fun WebCheckHistoryResponse.toDomain() = HistoryState(
        found = found,
        chave = chave,
        projeto = projeto,
        currentAction = currentAction?.toDomain(),
        currentLocal = currentLocal,
        hasCurrentDayCheckin = hasCurrentDayCheckin,
        lastCheckinAt = lastCheckinAt?.parseInstant(),
        lastCheckoutAt = lastCheckoutAt?.parseInstant(),
        transportEnabled = transportEnabled,
    )

    private fun MobileSyncStateResponse.toHistoryState() = HistoryState(
        found = found,
        chave = chave,
        projeto = projeto,
        currentAction = currentAction?.toDomain(),
        currentLocal = currentLocal,
        hasCurrentDayCheckin = currentAction == DtoCheckAction.CHECKIN,
        lastCheckinAt = lastCheckinAt?.parseInstant(),
        lastCheckoutAt = lastCheckoutAt?.parseInstant(),
        transportEnabled = false,
    )

    private fun WebLocationMatchResponse.toDomain() = LocationMatch(
        matched = matched,
        resolvedLocal = resolvedLocal,
        label = label,
        status = status.toDomain(),
        message = message,
        accuracyMeters = accuracyMeters,
        accuracyThresholdMeters = accuracyThresholdMeters,
        minimumCheckoutDistanceMeters = minimumCheckoutDistanceMeters,
        nearestWorkplaceDistanceMeters = nearestWorkplaceDistanceMeters,
    )

    private fun WebCheckHistoryItemDto.toDomain() = CheckHistoryEntry(
        action = action.toDomain(),
        projeto = projeto,
        local = local,
        time = time.parseInstant(),
        informe = informe.toDomain(),
    )

    private fun DtoCheckAction.toDomain() = when (this) {
        DtoCheckAction.CHECKIN -> CheckAction.CHECKIN
        DtoCheckAction.CHECKOUT -> CheckAction.CHECKOUT
    }

    private fun DtoInformeType.toDomain() = when (this) {
        DtoInformeType.NORMAL -> InformeType.NORMAL
        DtoInformeType.RETROATIVO -> InformeType.RETROATIVO
    }

    private fun CheckAction.toDto() = when (this) {
        CheckAction.CHECKIN -> DtoCheckAction.CHECKIN
        CheckAction.CHECKOUT -> DtoCheckAction.CHECKOUT
    }

    private fun InformeType.toDto() = when (this) {
        InformeType.NORMAL -> DtoInformeType.NORMAL
        InformeType.RETROATIVO -> DtoInformeType.RETROATIVO
    }

    private fun LocationMatchStatus.toDomain() = when (this) {
        LocationMatchStatus.MATCHED -> MatchStatus.MATCHED
        LocationMatchStatus.ACCURACY_TOO_LOW -> MatchStatus.ACCURACY_TOO_LOW
        LocationMatchStatus.NOT_IN_KNOWN_LOCATION -> MatchStatus.NOT_IN_KNOWN_LOCATION
        LocationMatchStatus.OUTSIDE_WORKPLACE -> MatchStatus.OUTSIDE_WORKPLACE
        LocationMatchStatus.NO_KNOWN_LOCATIONS -> MatchStatus.NO_KNOWN_LOCATIONS
    }

    private fun GeofenceCircleDto.toDomain() = GeofenceCircle(
        id = id,
        local = local,
        centerLat = centerLat,
        centerLng = centerLng,
        radiusMeters = radiusMeters,
    )

    private fun String.parseInstant(): Instant? = runCatching { Instant.parse(this) }.getOrNull()

    companion object {
        private val GEOFENCE_CACHE_TTL: Duration = Duration.ofHours(1)
    }
}
