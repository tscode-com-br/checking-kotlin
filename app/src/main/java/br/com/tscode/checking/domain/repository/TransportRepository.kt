package br.com.tscode.checking.domain.repository

import br.com.tscode.checking.core.result.AppResult
import br.com.tscode.checking.domain.model.TransportRequestKind
import br.com.tscode.checking.domain.model.TransportState
import kotlinx.coroutines.flow.Flow

interface TransportRepository {
    suspend fun getState(chave: String): AppResult<TransportState>
    suspend fun updateAddress(chave: String, endRua: String, zip: String): AppResult<TransportState>
    suspend fun createRequest(
        chave: String,
        kind: TransportRequestKind,
        requestedTime: String?,
        requestedDate: String?,
        selectedWeekdays: List<Int>?,
    ): AppResult<TransportState>
    suspend fun cancelRequest(chave: String, requestId: Int): AppResult<TransportState>
    suspend fun acknowledgeRequest(chave: String, requestId: Int): AppResult<TransportState>
    fun streamEvents(chave: String): Flow<String>
}
