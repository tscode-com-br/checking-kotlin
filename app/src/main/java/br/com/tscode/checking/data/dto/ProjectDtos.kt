package br.com.tscode.checking.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProjectRow(
    val id: Int,
    val name: String,
    @SerialName("country_code") val countryCode: String,
    @SerialName("country_name") val countryName: String,
    @SerialName("timezone_name") val timezoneName: String,
    @SerialName("timezone_label") val timezoneLabel: String,
    val address: String,
    @SerialName("zip_code") val zipCode: String,
    @SerialName("forms_enabled") val formsEnabled: Boolean,
    @SerialName("transport_enabled") val transportEnabled: Boolean,
    @SerialName("emergency_phone") val emergencyPhone: String,
    // Fields below are NOT populated by the public GET /projects endpoint;
    // included for safe full deserialization only — do not use in domain logic.
    @SerialName("twilio_account_sid") val twilioAccountSid: String = "",
    @SerialName("twilio_auth_token") val twilioAuthToken: String = "",
    @SerialName("twilio_phone_number") val twilioPhoneNumber: String = "",
    @SerialName("mobile_admin") val mobileAdmin: String = "",
    @SerialName("email_local_emergency") val emailLocalEmergency: String = "",
    @SerialName("emergency_call_message") val emergencyCallMessage: String = "",
    @SerialName("inactivity_days_threshold") val inactivityDaysThreshold: Int = 60,
    @SerialName("mixed_zone_interval_minutes") val mixedZoneIntervalMinutes: Int = 30,
)

@Serializable
data class WebUserProjectsResponse(
    val projects: List<String>,
    @SerialName("active_project") val activeProject: String,
)

@Serializable
data class WebUserProjectsUpdateRequest(
    val projects: List<String>,
)

@Serializable
data class WebUserProjectsUpdateResponse(
    val projects: List<String>,
    @SerialName("active_project") val activeProject: String,
    val ok: Boolean,
    val message: String,
)

@Serializable
data class WebProjectUpdateRequest(
    val project: String,
)

@Serializable
data class WebProjectUpdateResponse(
    val projects: List<String>,
    @SerialName("active_project") val activeProject: String,
    val ok: Boolean,
    val message: String,
    val project: String,
)
