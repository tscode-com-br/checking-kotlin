package br.com.tscode.checking.core.error

// Maps HTTP status codes and network failures to domain errors (§8.2).
sealed interface ApiError {
    // 4xx/5xx with a FastAPI detail string.
    data class Http(val status: Int, val detail: String?) : ApiError

    // 401/403 — session expired; routes back to the password prompt silently (§8.2).
    data object Unauthorized : ApiError

    // 409 — resource conflict (accident already active; emergency call already placed).
    data object Conflict : ApiError

    // IOException — no network or timeout.
    data object Network : ApiError

    // Unexpected error not covered by the above.
    data class Unknown(val cause: Throwable) : ApiError
}
