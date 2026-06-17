package br.com.tscode.checking.data.api

import br.com.tscode.checking.data.dto.AccidentLocationOption
import br.com.tscode.checking.data.dto.AccidentProjectOption
import br.com.tscode.checking.data.dto.AccidentVideoUploadResponse
import br.com.tscode.checking.data.dto.EmergencyCallResponse
import br.com.tscode.checking.data.dto.WebAccidentAcknowledgeRequest
import br.com.tscode.checking.data.dto.WebAccidentOpenRequest
import br.com.tscode.checking.data.dto.WebAccidentReportRequest
import br.com.tscode.checking.data.dto.WebAccidentStateResponse
import kotlinx.serialization.Serializable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface AccidentApi {
    @GET("check/accident/state")
    suspend fun getState(@Query("chave") chave: String): WebAccidentStateResponse

    @POST("check/accident/open")
    suspend fun open(@Body body: WebAccidentOpenRequest): WebAccidentStateResponse

    @POST("check/accident/report")
    suspend fun report(@Body body: WebAccidentReportRequest): WebAccidentStateResponse

    @POST("check/accident/acknowledge")
    suspend fun acknowledge(@Body body: WebAccidentAcknowledgeRequest): WebAccidentStateResponse

    @POST("check/accident/emergency-call")
    suspend fun emergencyCall(@Body body: EmergencyCallChaveRequest): EmergencyCallResponse

    @Multipart
    @POST("check/accident/video")
    suspend fun uploadVideo(
        @Part("chave") chave: RequestBody,
        @Part("idempotency_key") idempotencyKey: RequestBody,
        @Part video: MultipartBody.Part,
    ): AccidentVideoUploadResponse

    @GET("check/accident/wizard/projects")
    suspend fun wizardProjects(@Query("chave") chave: String): List<AccidentProjectOption>

    @GET("check/accident/wizard/locations")
    suspend fun wizardLocations(
        @Query("chave") chave: String,
        @Query("project_id") projectId: Int,
    ): List<AccidentLocationOption>
}

@Serializable
data class EmergencyCallChaveRequest(val chave: String)
