package br.com.tscode.checking.data.repository

import br.com.tscode.checking.core.result.AppResult
import br.com.tscode.checking.data.api.AccidentApi
import br.com.tscode.checking.data.api.EmergencyCallChaveRequest
import br.com.tscode.checking.data.dto.AccidentSafetyStatus as DtoSafetyStatus
import br.com.tscode.checking.data.dto.AccidentZone as DtoZone
import br.com.tscode.checking.data.dto.WebAccidentActiveItem
import br.com.tscode.checking.data.dto.WebAccidentAcknowledgeRequest
import br.com.tscode.checking.data.dto.WebAccidentOpenRequest
import br.com.tscode.checking.data.dto.WebAccidentReportRequest
import br.com.tscode.checking.data.dto.WebAccidentStateResponse
import br.com.tscode.checking.data.dto.WebAccidentUserReport
import br.com.tscode.checking.data.remote.safeApiCall
import br.com.tscode.checking.data.remote.sse.CheckEventStream
import br.com.tscode.checking.domain.model.AccidentActiveItem
import br.com.tscode.checking.domain.model.AccidentSafetyStatus
import br.com.tscode.checking.domain.model.AccidentState
import br.com.tscode.checking.domain.model.AccidentUserReport
import br.com.tscode.checking.domain.model.AccidentZone
import br.com.tscode.checking.domain.model.EmergencyCallResult
import br.com.tscode.checking.domain.model.VideoUploadResult
import br.com.tscode.checking.domain.repository.AccidentRepository
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.Buffer
import okio.BufferedSink
import okio.ForwardingSink
import okio.buffer
import java.io.File
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccidentRepositoryImpl @Inject constructor(
    private val api: AccidentApi,
    private val checkEventStream: CheckEventStream,
) : AccidentRepository {

    override suspend fun getState(chave: String): AppResult<AccidentState> = safeApiCall {
        api.getState(chave).toDomain()
    }

    override suspend fun open(
        chave: String,
        projectId: Int,
        locationId: Int?,
        customLocationName: String?,
        zone: AccidentZone,
        status: AccidentSafetyStatus,
        description: String?,
    ): AppResult<AccidentState> = safeApiCall {
        api.open(
            WebAccidentOpenRequest(
                chave = chave,
                projectId = projectId,
                locationId = locationId,
                customLocationName = customLocationName,
                zone = zone.toDto(),
                status = status.toDto(),
                // Server types description as `str = ""` (non-nullable). Send an empty string
                // when there's no description — never null (which 422s). Matches the web client.
                description = description?.takeIf { it.isNotBlank() } ?: "",
            )
        ).toDomain()
    }

    override suspend fun report(
        chave: String,
        zone: AccidentZone,
        status: AccidentSafetyStatus,
    ): AppResult<AccidentState> = safeApiCall {
        api.report(WebAccidentReportRequest(chave, zone.toDto(), status.toDto())).toDomain()
    }

    override suspend fun acknowledge(chave: String, accidentId: Int?): AppResult<AccidentState> =
        safeApiCall {
            api.acknowledge(WebAccidentAcknowledgeRequest(chave, accidentId)).toDomain()
        }

    override suspend fun emergencyCall(chave: String): AppResult<EmergencyCallResult> =
        safeApiCall {
            val r = api.emergencyCall(EmergencyCallChaveRequest(chave))
            EmergencyCallResult(r.callNumber, r.callNumberLabel, r.callSid, r.callStatus, r.message)
        }

    override suspend fun wizardProjects(chave: String): AppResult<List<Pair<Int, String>>> =
        safeApiCall { api.wizardProjects(chave).map { it.id to it.name } }

    override suspend fun wizardLocations(
        chave: String,
        projectId: Int,
    ): AppResult<List<Triple<Int, String, Boolean>>> =
        safeApiCall { api.wizardLocations(chave, projectId).map { Triple(it.id, it.name, it.registered) } }

    override suspend fun uploadVideo(
        chave: String,
        idempotencyKey: String,
        videoFile: File,
        contentType: String,
        onProgress: (Float) -> Unit,
    ): AppResult<VideoUploadResult> = safeApiCall {
        val fileBody = videoFile.asRequestBody(contentType.toMediaType())
        val progressBody = ProgressRequestBody(fileBody, onProgress)
        val videoPart = MultipartBody.Part.createFormData("video", videoFile.name, progressBody)
        val r = api.uploadVideo(
            chave = chave.toRequestBody("text/plain".toMediaType()),
            idempotencyKey = idempotencyKey.toRequestBody("text/plain".toMediaType()),
            video = videoPart,
        )
        VideoUploadResult(
            videoId = r.videoId,
            publicUrl = r.publicUrl,
            capturedAt = runCatching { Instant.parse(r.capturedAt) }.getOrElse { Instant.now() },
        )
    }

    override fun streamCheckEvents(chave: String): Flow<String> = checkEventStream.events(chave)

    // ---- DTO → domain ----

    private fun WebAccidentStateResponse.toDomain() = AccidentState(
        isActive = isActive,
        accidentId = accidentId,
        accidentNumberLabel = accidentNumberLabel,
        projectId = projectId,
        projectName = projectName,
        locationName = locationName,
        description = description,
        awarenessStatus = awarenessStatus,
        currentUserReport = currentUserReport?.toDomain(),
        activeAccidents = activeAccidents.map { it.toDomain() },
    )

    private fun WebAccidentActiveItem.toDomain() = AccidentActiveItem(
        accidentId = accidentId,
        accidentNumberLabel = accidentNumberLabel,
        projectId = projectId,
        projectName = projectName,
        locationName = locationName,
        description = description,
        awarenessStatus = awarenessStatus,
        currentUserReport = currentUserReport?.toDomain(),
    )

    private fun WebAccidentUserReport.toDomain() = AccidentUserReport(
        zone = zone?.toDomain(),
        status = status?.toDomain(),
        reportedAt = reportedAt?.let { runCatching { Instant.parse(it) }.getOrNull() },
    )

    private fun DtoZone.toDomain() = when (this) {
        DtoZone.SAFETY -> AccidentZone.SAFETY
        DtoZone.ACCIDENT -> AccidentZone.ACCIDENT
    }

    private fun DtoSafetyStatus.toDomain() = when (this) {
        DtoSafetyStatus.OK -> AccidentSafetyStatus.OK
        DtoSafetyStatus.HELP -> AccidentSafetyStatus.HELP
    }

    private fun AccidentZone.toDto() = when (this) {
        AccidentZone.SAFETY -> DtoZone.SAFETY
        AccidentZone.ACCIDENT -> DtoZone.ACCIDENT
    }

    private fun AccidentSafetyStatus.toDto() = when (this) {
        AccidentSafetyStatus.OK -> DtoSafetyStatus.OK
        AccidentSafetyStatus.HELP -> DtoSafetyStatus.HELP
    }
}

private class ProgressRequestBody(
    private val delegate: RequestBody,
    private val onProgress: (Float) -> Unit,
) : RequestBody() {
    override fun contentType() = delegate.contentType()
    override fun contentLength() = delegate.contentLength()
    override fun writeTo(sink: BufferedSink) {
        val countingSink = object : ForwardingSink(sink) {
            private var written = 0L
            override fun write(source: Buffer, byteCount: Long) {
                super.write(source, byteCount)
                written += byteCount
                val total = contentLength()
                if (total > 0L) onProgress(written.toFloat() / total.toFloat())
            }
        }
        delegate.writeTo(countingSink.buffer())
    }
}
