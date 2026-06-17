package br.com.tscode.checking.data.repository

import br.com.tscode.checking.core.result.AppResult
import br.com.tscode.checking.data.api.AuthApi
import br.com.tscode.checking.data.api.CheckApi
import br.com.tscode.checking.data.dto.WebPasswordChangeRequest
import br.com.tscode.checking.data.dto.WebPasswordLoginRequest
import br.com.tscode.checking.data.dto.WebPasswordRegisterRequest
import br.com.tscode.checking.data.dto.WebUserSelfRegistrationRequest
import br.com.tscode.checking.data.dto.CheckAction as DtoCheckAction
import br.com.tscode.checking.data.local.PersistentCookieJar
import br.com.tscode.checking.data.remote.safeApiCall
import br.com.tscode.checking.domain.model.AuthStatus
import br.com.tscode.checking.domain.model.CheckAction
import br.com.tscode.checking.domain.model.HistoryState
import br.com.tscode.checking.domain.repository.AuthRepository
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val checkApi: CheckApi,
    private val cookieJar: PersistentCookieJar,
) : AuthRepository {

    override suspend fun getStatus(chave: String): AppResult<AuthStatus> = safeApiCall {
        val r = authApi.getStatus(chave)
        AuthStatus(
            found = r.found,
            chave = r.chave,
            hasPassword = r.hasPassword,
            authenticated = r.authenticated,
            message = r.message,
        )
    }

    override suspend fun login(chave: String, password: String): AppResult<AuthStatus> = safeApiCall {
        val r = authApi.login(WebPasswordLoginRequest(chave, password))
        AuthStatus(
            found = true,
            chave = chave,
            hasPassword = r.hasPassword,
            authenticated = r.authenticated,
            message = r.message,
        )
    }

    override suspend fun logout(): AppResult<Unit> {
        safeApiCall { try { authApi.logout() } catch (_: Exception) { } }
        cookieJar.clear()
        return AppResult.Success(Unit)
    }

    override suspend fun registerPassword(
        chave: String,
        project: String?,
        password: String,
    ): AppResult<AuthStatus> = safeApiCall {
        val r = authApi.registerPassword(WebPasswordRegisterRequest(chave, project, password))
        AuthStatus(
            found = true,
            chave = chave,
            hasPassword = r.hasPassword,
            authenticated = r.authenticated,
            message = r.message,
        )
    }

    override suspend fun changePassword(
        chave: String,
        oldPassword: String,
        newPassword: String,
    ): AppResult<AuthStatus> = safeApiCall {
        val r = authApi.changePassword(WebPasswordChangeRequest(chave, oldPassword, newPassword))
        AuthStatus(
            found = true,
            chave = chave,
            hasPassword = r.hasPassword,
            authenticated = r.authenticated,
            message = r.message,
        )
    }

    override suspend fun selfRegister(
        chave: String,
        nome: String,
        projetos: List<String>,
        email: String?,
        password: String,
        confirmPassword: String,
    ): AppResult<AuthStatus> = safeApiCall {
        val r = authApi.registerUser(
            WebUserSelfRegistrationRequest(
                chave = chave,
                nome = nome,
                projetos = projetos,
                email = email?.takeIf { it.isNotBlank() },
                senha = password,
                confirmarSenha = confirmPassword,
            )
        )
        AuthStatus(
            found = true,
            chave = chave,
            hasPassword = r.hasPassword,
            authenticated = r.authenticated,
            message = r.message,
        )
    }

    override suspend fun getHistory(chave: String): AppResult<HistoryState> = safeApiCall {
        val r = checkApi.getState(chave)
        HistoryState(
            found = r.found,
            chave = r.chave,
            projeto = r.projeto,
            currentAction = when (r.currentAction) {
                DtoCheckAction.CHECKIN -> CheckAction.CHECKIN
                DtoCheckAction.CHECKOUT -> CheckAction.CHECKOUT
                null -> null
            },
            currentLocal = r.currentLocal,
            hasCurrentDayCheckin = r.hasCurrentDayCheckin,
            lastCheckinAt = r.lastCheckinAt?.let { runCatching { Instant.parse(it) }.getOrNull() },
            lastCheckoutAt = r.lastCheckoutAt?.let { runCatching { Instant.parse(it) }.getOrNull() },
            transportEnabled = r.transportEnabled,
        )
    }
}
