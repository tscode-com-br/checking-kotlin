package br.com.tscode.checking.data.remote.sse

import br.com.tscode.checking.platform.connectivity.NetworkMonitor
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.retryWhen
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.io.IOException
import kotlin.math.min

// Reconnect strategy (§16.1, T7.2):
// - Non-IOException failures are not retried (e.g. auth errors caught at caller level).
// - When offline: suspend until NetworkMonitor reports connectivity restored.
// - When online: exponential backoff 1s → 2s → 4s … capped at 30s.
fun sseFlow(client: OkHttpClient, url: String, networkMonitor: NetworkMonitor): Flow<String> =
    callbackFlow<String> {
        val request = Request.Builder().url(url).header("Accept", "text/event-stream").build()
        val factory = EventSources.createFactory(client)

        val listener = object : EventSourceListener() {
            override fun onEvent(source: EventSource, id: String?, type: String?, data: String) {
                trySend(data)
            }

            override fun onFailure(source: EventSource, t: Throwable?, response: Response?) {
                close(t ?: IOException("SSE stream closed: ${response?.code}"))
            }

            override fun onClosed(source: EventSource) {
                close()
            }
        }

        val source = factory.newEventSource(request, listener)
        awaitClose { source.cancel() }
    }.retryWhen { cause, attempt ->
        if (cause !is IOException) return@retryWhen false
        // Wait until we're back online (no-op if already online — emits current state first)
        networkMonitor.isOnline.first { it }
        // Exponential backoff: 1s, 2s, 4s, 8s, 16s, 30s (cap at attempt 5)
        kotlinx.coroutines.delay(min(1_000L * (1L shl attempt.toInt().coerceAtMost(5)), 30_000L))
        true
    }
