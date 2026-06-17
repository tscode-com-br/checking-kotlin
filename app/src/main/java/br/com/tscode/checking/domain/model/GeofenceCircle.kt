package br.com.tscode.checking.domain.model

// Coarse bounding circle for a project location.
// Used only to register native geofence wake-ups (Approach A, §23.2).
// Precise matching is always server-side via POST /check/location.
data class GeofenceCircle(
    val id: Int,
    val local: String,
    val centerLat: Double,
    val centerLng: Double,
    val radiusMeters: Double,
)
