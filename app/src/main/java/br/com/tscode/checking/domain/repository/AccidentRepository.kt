package br.com.tscode.checking.domain.repository

import br.com.tscode.checking.core.result.AppResult
import br.com.tscode.checking.domain.model.AccidentSafetyStatus
import br.com.tscode.checking.domain.model.AccidentState
import br.com.tscode.checking.domain.model.AccidentZone
import br.com.tscode.checking.domain.model.EmergencyCallResult
import br.com.tscode.checking.domain.model.VideoUploadResult
import java.io.File
import kotlinx.coroutines.flow.Flow

interface AccidentRepository {
    suspend fun getState(chave: String): AppResult<AccidentState>
    suspend fun open(
        chave: String,
        projectId: Int,
        locationId: Int?,
        customLocationName: String?,
        zone: AccidentZone,
        status: AccidentSafetyStatus,
        description: String?,
    ): AppResult<AccidentState>
    suspend fun report(
        chave: String,
        zone: AccidentZone,
        status: AccidentSafetyStatus,
    ): AppResult<AccidentState>
    suspend fun acknowledge(chave: String, accidentId: Int?): AppResult<AccidentState>
    suspend fun emergencyCall(chave: String): AppResult<EmergencyCallResult>
    suspend fun uploadVideo(
        chave: String,
        idempotencyKey: String,
        videoFile: File,
        contentType: String,
        onProgress: (Float) -> Unit,
    ): AppResult<VideoUploadResult>
    suspend fun wizardProjects(chave: String): AppResult<List<Pair<Int, String>>>
    suspend fun wizardLocations(chave: String, projectId: Int): AppResult<List<Triple<Int, String, Boolean>>>
    fun streamCheckEvents(chave: String): Flow<String>
}
