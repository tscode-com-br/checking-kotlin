package br.com.tscode.checking.data.api

import br.com.tscode.checking.data.dto.MobileSubmitResponse
import br.com.tscode.checking.data.dto.WebCheckHistoryListResponseDto
import br.com.tscode.checking.data.dto.WebCheckHistoryResponse
import br.com.tscode.checking.data.dto.WebCheckSubmitRequest
import br.com.tscode.checking.data.dto.WebGeofencesResponse
import br.com.tscode.checking.data.dto.WebLocationMatchRequest
import br.com.tscode.checking.data.dto.WebLocationMatchResponse
import br.com.tscode.checking.data.dto.WebLocationOptionsResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface CheckApi {
    @GET("check/state")
    suspend fun getState(@Query("chave") chave: String): WebCheckHistoryResponse

    @GET("check/history")
    suspend fun getHistory(@Query("chave") chave: String): WebCheckHistoryListResponseDto

    @GET("check/locations")
    suspend fun getLocations(): WebLocationOptionsResponse

    @POST("check/location")
    suspend fun matchLocation(@Body body: WebLocationMatchRequest): WebLocationMatchResponse

    // Bounding circles for native geofence wake-up registration (Approach A hybrid, §23.2.2).
    // Not used for matching — POST /check/location remains the single matcher.
    @GET("check/geofences")
    suspend fun getGeofences(@Query("chave") chave: String): WebGeofencesResponse

    @POST("check")
    suspend fun submit(@Body body: WebCheckSubmitRequest): MobileSubmitResponse

    // SSE endpoint — consumed via OkHttp EventSource in CheckSseDataSource, not Retrofit.
    // GET check/stream?chave= → server-sent events
}
