package br.com.tscode.checking.data.remote.sse

import br.com.tscode.checking.BuildConfig
import br.com.tscode.checking.di.SseClient
import br.com.tscode.checking.platform.connectivity.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Singleton

// Single shared SSE connection to /check/stream, fanned out to every collector.
//
// Both CheckViewModel (live check-state changes) and AccidentViewModel (accident events) consume
// the SAME server endpoint. Previously each repository opened its OWN cold EventSource, so a
// signed-in user with the accident screen active held TWO connections to /check/stream. Each
// long-lived SSE connection holds a server resource, so this doubled the per-user footprint.
//
// shareIn + WhileSubscribed keeps exactly one upstream connection alive while >=1 collector is
// active and tears it down a few seconds after the last collector leaves. The upstream is the
// reconnecting sseFlow, so its retry/backoff still applies transparently to all collectors.
@Singleton
class CheckEventStream @Inject constructor(
    @SseClient private val sseClient: OkHttpClient,
    private val networkMonitor: NetworkMonitor,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val lock = Any()

    private var sharedChave: String? = null
    private var shared: SharedFlow<String>? = null

    // Returns the shared event stream for [chave]. Callers with the same chave observe a single
    // upstream connection; a different chave (e.g. after re-login) starts a fresh one.
    fun events(chave: String): Flow<String> = synchronized(lock) {
        shared?.let { if (sharedChave == chave) return@synchronized it }

        val url = "${BuildConfig.BASE_URL}${BuildConfig.API_PREFIX}/check/stream?chave=$chave"
        sseFlow(sseClient, url, networkMonitor)
            .shareIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
                replay = 0,
            )
            .also {
                sharedChave = chave
                shared = it
            }
    }
}
