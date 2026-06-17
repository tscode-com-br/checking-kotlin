package br.com.tscode.checking.domain.repository

import br.com.tscode.checking.core.result.AppResult
import br.com.tscode.checking.domain.model.AuthStatus
import br.com.tscode.checking.domain.model.HistoryState

interface AuthRepository {
    suspend fun getStatus(chave: String): AppResult<AuthStatus>
    suspend fun login(chave: String, password: String): AppResult<AuthStatus>
    suspend fun logout(): AppResult<Unit>
    suspend fun registerPassword(chave: String, project: String?, password: String): AppResult<AuthStatus>
    suspend fun changePassword(chave: String, oldPassword: String, newPassword: String): AppResult<AuthStatus>
    suspend fun selfRegister(
        chave: String,
        nome: String,
        projetos: List<String>,
        email: String?,
        password: String,
        confirmPassword: String,
    ): AppResult<AuthStatus>
    suspend fun getHistory(chave: String): AppResult<HistoryState>
}
