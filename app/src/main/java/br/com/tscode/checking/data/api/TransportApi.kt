package br.com.tscode.checking.data.api

import br.com.tscode.checking.data.dto.WebTransportActionResponse
import br.com.tscode.checking.data.dto.WebTransportAddressUpdateRequest
import br.com.tscode.checking.data.dto.WebTransportRequestAction
import br.com.tscode.checking.data.dto.WebTransportRequestCreate
import br.com.tscode.checking.data.dto.WebTransportStateResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface TransportApi {
    @GET("transport/state")
    suspend fun getState(@Query("chave") chave: String): WebTransportStateResponse

    // SSE endpoint — consumed via OkHttp EventSource in TransportSseDataSource, not Retrofit.
    // GET transport/stream?chave= → server-sent events

    @POST("transport/address")
    suspend fun updateAddress(@Body body: WebTransportAddressUpdateRequest): WebTransportActionResponse

    // Uses /transport/vehicle-request (the active path set in data-transport-request-endpoint;
    // /transport/request is an identical server alias — see §7.4).
    @POST("transport/vehicle-request")
    suspend fun createRequest(@Body body: WebTransportRequestCreate): WebTransportActionResponse

    @POST("transport/cancel")
    suspend fun cancelRequest(@Body body: WebTransportRequestAction): WebTransportActionResponse

    @POST("transport/acknowledge")
    suspend fun acknowledgeRequest(@Body body: WebTransportRequestAction): WebTransportActionResponse
}
