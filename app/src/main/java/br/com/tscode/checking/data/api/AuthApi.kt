package br.com.tscode.checking.data.api

import br.com.tscode.checking.data.dto.WebPasswordActionResponse
import br.com.tscode.checking.data.dto.WebPasswordChangeRequest
import br.com.tscode.checking.data.dto.WebPasswordLoginRequest
import br.com.tscode.checking.data.dto.WebPasswordRegisterRequest
import br.com.tscode.checking.data.dto.WebPasswordStatusResponse
import br.com.tscode.checking.data.dto.WebUserSelfRegistrationRequest
import br.com.tscode.checking.data.dto.WebUserSelfRegistrationResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApi {
    @GET("auth/status")
    suspend fun getStatus(@Query("chave") chave: String): WebPasswordStatusResponse

    @POST("auth/register-password")
    suspend fun registerPassword(@Body body: WebPasswordRegisterRequest): WebPasswordActionResponse

    @POST("auth/register-user")
    suspend fun registerUser(@Body body: WebUserSelfRegistrationRequest): WebUserSelfRegistrationResponse

    @POST("auth/login")
    suspend fun login(@Body body: WebPasswordLoginRequest): WebPasswordActionResponse

    @POST("auth/logout")
    suspend fun logout(): WebPasswordActionResponse

    @POST("auth/change-password")
    suspend fun changePassword(@Body body: WebPasswordChangeRequest): WebPasswordActionResponse
}
