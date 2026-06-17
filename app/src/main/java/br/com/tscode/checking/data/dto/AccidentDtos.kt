package br.com.tscode.checking.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class AccidentZone {
    @SerialName("safety") SAFETY,
    @SerialName("accident") ACCIDENT,
}

@Serializable
enum class AccidentSafetyStatus {
    @SerialName("ok") OK,
    @SerialName("help") HELP,
}

@Serializable
data class AccidentProjectOption(
    val id: Int,
    val name: String,
)

@Serializable
data class AccidentLocationOption(
    val id: Int,
    val name: String,
    val registered: Boolean,
)

// Timestamps as ISO-8601 strings; parsed to domain types at the repository boundary.
@Serializable
data class AccidentVideoLink(
    @SerialName("video_id") val videoId: Int,
    @SerialName("public_url") val publicUrl: String,
    @SerialName("captured_at") val capturedAt: String,
    @SerialName("content_type") val contentType: String,
    @SerialName("size_bytes") val sizeBytes: Int,
)

@Serializable
data class WebAccidentUserReport(
    val zone: AccidentZone? = null,
    val status: AccidentSafetyStatus? = null,
    @SerialName("reported_at") val reportedAt: String? = null,
)

@Serializable
data class WebAccidentActiveItem(
    @SerialName("accident_id") val accidentId: Int,
    @SerialName("accident_number_label") val accidentNumberLabel: String,
    @SerialName("project_id") val projectId: Int,
    @SerialName("project_name") val projectName: String,
    @SerialName("location_name") val locationName: String,
    val description: String? = null,
    @SerialName("awareness_status") val awarenessStatus: String,
    @SerialName("current_user_report") val currentUserReport: WebAccidentUserReport? = null,
)

@Serializable
data class WebAccidentStateResponse(
    @SerialName("is_active") val isActive: Boolean,
    @SerialName("accident_id") val accidentId: Int? = null,
    @SerialName("accident_number_label") val accidentNumberLabel: String? = null,
    @SerialName("project_id") val projectId: Int? = null,
    @SerialName("project_name") val projectName: String? = null,
    @SerialName("location_name") val locationName: String? = null,
    val description: String? = null,
    @SerialName("awareness_status") val awarenessStatus: String? = null,
    @SerialName("current_user_report") val currentUserReport: WebAccidentUserReport? = null,
    @SerialName("active_accidents") val activeAccidents: List<WebAccidentActiveItem>,
)

@Serializable
data class WebAccidentOpenRequest(
    val chave: String,
    @SerialName("project_id") val projectId: Int,
    @SerialName("location_id") val locationId: Int? = null,
    @SerialName("custom_location_name") val customLocationName: String? = null,
    val zone: AccidentZone,
    val status: AccidentSafetyStatus,
    val description: String? = null,
)

@Serializable
data class WebAccidentReportRequest(
    val chave: String,
    val zone: AccidentZone,
    val status: AccidentSafetyStatus,
)

@Serializable
data class WebAccidentAcknowledgeRequest(
    val chave: String,
    @SerialName("accident_id") val accidentId: Int? = null,
)

@Serializable
data class AccidentVideoUploadResponse(
    @SerialName("video_id") val videoId: Int,
    @SerialName("public_url") val publicUrl: String,
    @SerialName("captured_at") val capturedAt: String,
)

@Serializable
data class EmergencyCallResponse(
    @SerialName("call_number") val callNumber: Int,
    @SerialName("call_number_label") val callNumberLabel: String,
    @SerialName("call_sid") val callSid: String? = null,
    @SerialName("call_status") val callStatus: String,
    val message: String,
)
