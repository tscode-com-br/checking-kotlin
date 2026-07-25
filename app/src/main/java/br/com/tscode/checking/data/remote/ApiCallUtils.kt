package br.com.tscode.checking.data.remote

import br.com.tscode.checking.core.error.ApiError
import br.com.tscode.checking.core.result.AppResult
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import retrofit2.HttpException
import java.io.IOException

suspend fun <T> safeApiCall(call: suspend () -> T): AppResult<T> = try {
    AppResult.Success(call())
} catch (e: HttpException) {
    val responseBody = try {
        e.response()?.errorBody()?.string()
    } catch (_: Exception) {
        null
    }
    val detail = extractApiErrorDetail(responseBody)
    when (e.code()) {
        401, 403 -> AppResult.Failure(ApiError.Unauthorized)
        409 -> AppResult.Failure(ApiError.Conflict(detail))
        else -> AppResult.Failure(ApiError.Http(e.code(), detail))
    }
} catch (e: IOException) {
    AppResult.Failure(ApiError.Network)
} catch (e: Exception) {
    AppResult.Failure(ApiError.Unknown(e))
}

/**
 * Extracts FastAPI's top-level `detail` value from an HTTP error body.
 *
 * String details are unescaped for direct display/localization. Structured
 * validation details remain JSON so no diagnostic information is discarded.
 * Non-FastAPI and malformed bodies fall back to their trimmed raw content.
 */
internal fun extractApiErrorDetail(responseBody: String?): String? {
    val raw = responseBody?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    return runCatching {
        val root = Json.parseToJsonElement(raw)
        val detail = (root as? JsonObject)?.get("detail") ?: return@runCatching raw
        when (detail) {
            is JsonPrimitive -> detail.content
            else -> detail.toString()
        }.takeIf { it.isNotEmpty() }
    }.getOrElse { raw }
}
