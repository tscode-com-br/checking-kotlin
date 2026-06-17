package br.com.tscode.checking.data.remote

import br.com.tscode.checking.core.error.ApiError
import br.com.tscode.checking.core.result.AppResult
import retrofit2.HttpException
import java.io.IOException

suspend fun <T> safeApiCall(call: suspend () -> T): AppResult<T> = try {
    AppResult.Success(call())
} catch (e: HttpException) {
    val detail = try { e.response()?.errorBody()?.string() } catch (_: Exception) { null }
    when (e.code()) {
        401, 403 -> AppResult.Failure(ApiError.Unauthorized)
        409 -> AppResult.Failure(ApiError.Conflict)
        else -> AppResult.Failure(ApiError.Http(e.code(), detail))
    }
} catch (e: IOException) {
    AppResult.Failure(ApiError.Network)
} catch (e: Exception) {
    AppResult.Failure(ApiError.Unknown(e))
}
