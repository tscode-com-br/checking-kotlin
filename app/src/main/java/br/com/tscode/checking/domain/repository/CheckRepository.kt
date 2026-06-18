package br.com.tscode.checking.domain.repository

import br.com.tscode.checking.core.result.AppResult
import br.com.tscode.checking.domain.model.CheckAction
import br.com.tscode.checking.domain.model.CheckHistoryEntry
import br.com.tscode.checking.domain.model.GeofenceCircle
import br.com.tscode.checking.domain.model.HistoryState
import br.com.tscode.checking.domain.model.InformeType
import br.com.tscode.checking.domain.model.LocationMatch
import br.com.tscode.checking.domain.model.LocationOptions
import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface CheckRepository {
    suspend fun getState(chave: String): AppResult<HistoryState>
    // Full check-in/out history (newest-first), including location (change D). Read-only.
    suspend fun getHistory(chave: String): AppResult<List<CheckHistoryEntry>>
    // SSE stream of check-state change events (/check/stream). Emits on every server-side
    // change (e.g. an admin toggling a project's transport flag) so the UI can re-sync.
    fun streamEvents(chave: String): Flow<String>
    suspend fun getLocations(): AppResult<LocationOptions>
    suspend fun matchLocation(lat: Double, lon: Double, accuracy: Double?): AppResult<LocationMatch>
    // eventTime/clientEventId default to "now"/a fresh UUID for live submits; the offline replay
    // (SyncPendingChecksWorker) passes the ORIGINAL capture time + id so the server records the
    // event at the real-world time and dedups by client_event_id (P8).
    suspend fun submit(
        chave: String,
        projeto: String,
        action: CheckAction,
        local: String?,
        informe: InformeType,
        eventTime: Instant? = null,
        clientEventId: String? = null,
    ): AppResult<HistoryState>
    // Returns bounding circles for the user's project locations (Approach A, §23.2.2).
    // Result is cached for 1 h; callers that need fresh data after login/project change
    // should call this again — the repository re-fetches when the cache is stale or the
    // chave changes.
    suspend fun getGeofences(chave: String): AppResult<List<GeofenceCircle>>
}
