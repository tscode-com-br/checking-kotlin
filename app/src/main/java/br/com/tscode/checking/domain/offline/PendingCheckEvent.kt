package br.com.tscode.checking.domain.offline

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// One entry in the offline check queue (P8 — offline resilience). Persisted as a JSON list in
// DataStore and drained by SyncPendingChecksWorker when connectivity (or the server) returns.
//
// Two flavours, because the engine fails offline at two different points:
//  - Raw     : the AUTOMATIC engine captured a GPS fix but POST /check/location was unreachable,
//              so no decision could be made. Replayed by matching the STORED position against the
//              live server, deciding the situation, then submitting — exactly the online flow,
//              just with the original capture timestamp. No client-side geometry needed.
//  - Decided : a decision already exists (a MANUAL check where the user picked the local, or an
//              automatic decision whose submit failed mid-flight). Replayed by submitting verbatim.
//
// Idempotency: clientEventId is generated once (at capture) and reused on every replay, so the
// server (forms_submit dedups by client_event_id) records each real-world event exactly once even
// if a submit reached the server but its response was lost.
@Serializable
sealed class PendingCheckEvent {
    abstract val chave: String
    abstract val projeto: String
    abstract val capturedAtEpochMs: Long
    abstract val clientEventId: String

    @Serializable
    @SerialName("raw")
    data class Raw(
        override val chave: String,
        override val projeto: String,
        override val capturedAtEpochMs: Long,
        override val clientEventId: String,
        val latitude: Double,
        val longitude: Double,
        val accuracyMeters: Double?,
    ) : PendingCheckEvent()

    @Serializable
    @SerialName("decided")
    data class Decided(
        override val chave: String,
        override val projeto: String,
        override val capturedAtEpochMs: Long,
        override val clientEventId: String,
        val action: String, // "checkin" | "checkout"
        val local: String?,
        val informe: String, // "normal" | "retroativo"
    ) : PendingCheckEvent()
}
