package br.com.tscode.checking.domain.usecase

import br.com.tscode.checking.core.error.ApiError
import br.com.tscode.checking.core.result.AppResult
import br.com.tscode.checking.domain.model.LocationMatch
import br.com.tscode.checking.domain.repository.CheckRepository
import br.com.tscode.checking.platform.location.LocationCapture
import br.com.tscode.checking.platform.location.LocationProvider
import javax.inject.Inject

// A GPS fix (lat/lon/accuracy) captured locally — carried on NetworkError so the offline engine
// can queue it for replay (P8) instead of discarding the user's position.
data class LocationReading(val lat: Double, val lon: Double, val accuracyMeters: Double?)

sealed class LocationCaptureResult {
    data class Matched(val match: LocationMatch) : LocationCaptureResult()
    object NoPermission : LocationCaptureResult()
    object Timeout : LocationCaptureResult()
    // reading is non-null ONLY when the failure was a real connectivity loss (ApiError.Network) and
    // a GPS fix was obtained — i.e. the case the offline queue should capture. HTTP errors carry null.
    data class NetworkError(val reading: LocationReading?) : LocationCaptureResult()
}

class CaptureLocationUseCase @Inject constructor(
    private val locationProvider: LocationProvider,
    private val checkRepository: CheckRepository,
) {
    suspend operator fun invoke(accuracyThresholdMeters: Int): LocationCaptureResult {
        return when (val capture = locationProvider.capture(accuracyThresholdMeters)) {
            is LocationCapture.Success -> {
                when (val result = checkRepository.matchLocation(capture.lat, capture.lon, capture.accuracyMeters)) {
                    is AppResult.Success -> LocationCaptureResult.Matched(result.data)
                    is AppResult.Failure -> LocationCaptureResult.NetworkError(
                        if (result.error is ApiError.Network) {
                            LocationReading(capture.lat, capture.lon, capture.accuracyMeters)
                        } else {
                            null
                        },
                    )
                }
            }
            LocationCapture.Timeout -> LocationCaptureResult.Timeout
            LocationCapture.Unavailable -> LocationCaptureResult.NoPermission
        }
    }
}
