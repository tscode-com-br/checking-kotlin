package br.com.tscode.checking.domain.model

import java.time.Instant

enum class AccidentZone { SAFETY, ACCIDENT }
enum class AccidentSafetyStatus { OK, HELP }

data class AccidentUserReport(
    val zone: AccidentZone?,
    val status: AccidentSafetyStatus?,
    val reportedAt: Instant?,
)

data class AccidentActiveItem(
    val accidentId: Int,
    val accidentNumberLabel: String,
    val projectId: Int,
    val projectName: String,
    val locationName: String,
    val description: String?,
    val awarenessStatus: String,
    val currentUserReport: AccidentUserReport?,
)

data class AccidentState(
    val isActive: Boolean,
    val accidentId: Int?,
    val accidentNumberLabel: String?,
    val projectId: Int?,
    val projectName: String?,
    val locationName: String?,
    val description: String?,
    val awarenessStatus: String?,
    val currentUserReport: AccidentUserReport?,
    val activeAccidents: List<AccidentActiveItem>,
)

data class VideoUploadResult(
    val videoId: Int,
    val publicUrl: String,
    val capturedAt: Instant,
)

data class EmergencyCallResult(
    val callNumber: Int,
    val callNumberLabel: String,
    val callSid: String?,
    val callStatus: String,
    val message: String,
)
