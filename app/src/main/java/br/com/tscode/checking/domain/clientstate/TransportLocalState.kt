package br.com.tscode.checking.domain.clientstate

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class TransportLocalEntry(
    @SerialName("dismissed_request_ids") val dismissedIds: List<Int> = emptyList(),
    @SerialName("realized_request_ids") val realizedIds: List<Int> = emptyList(),
)

private val _json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

data class TransportLocalState(
    val dismissedIds: Set<Int> = emptySet(),
    val realizedIds: Set<Int> = emptySet(),
) {
    fun withDismissed(id: Int) = copy(dismissedIds = dismissedIds + id)
    fun withRealized(id: Int) = copy(realizedIds = realizedIds + id)
    fun isEmpty() = dismissedIds.isEmpty() && realizedIds.isEmpty()
}

fun loadTransportLocalState(json: String, chave: String): TransportLocalState {
    if (chave.length != 4 || json.isBlank()) return TransportLocalState()
    return try {
        val map = _json.decodeFromString<Map<String, TransportLocalEntry>>(json)
        val entry = map[chave] ?: return TransportLocalState()
        TransportLocalState(
            dismissedIds = entry.dismissedIds.toSet(),
            realizedIds = entry.realizedIds.toSet(),
        )
    } catch (_: Exception) {
        TransportLocalState()
    }
}

fun saveTransportLocalState(
    currentJson: String,
    chave: String,
    state: TransportLocalState,
): String {
    if (chave.length != 4) return currentJson
    val map: MutableMap<String, TransportLocalEntry> = try {
        if (currentJson.isBlank()) mutableMapOf()
        else _json.decodeFromString<Map<String, TransportLocalEntry>>(currentJson).toMutableMap()
    } catch (_: Exception) {
        mutableMapOf()
    }
    if (state.isEmpty()) {
        map.remove(chave)
    } else {
        map[chave] = TransportLocalEntry(
            dismissedIds = state.dismissedIds.toList(),
            realizedIds = state.realizedIds.toList(),
        )
    }
    return _json.encodeToString(map)
}
