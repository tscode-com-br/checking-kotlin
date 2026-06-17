package br.com.tscode.checking.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Bounding circle for a project location — coarse wake-up geometry only.
// Precise match is done by POST /check/location (Approach A, §23.2).
@Serializable
data class GeofenceCircleDto(
    val id: Int,
    val local: String,
    @SerialName("center_lat") val centerLat: Double,
    @SerialName("center_lng") val centerLng: Double,
    @SerialName("radius_meters") val radiusMeters: Double,
)

@Serializable
data class WebGeofencesResponse(
    val locations: List<GeofenceCircleDto>,
)
