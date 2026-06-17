package br.com.tscode.checking.presentation.transport

import br.com.tscode.checking.domain.clientstate.TransportLocalState
import br.com.tscode.checking.domain.model.TransportOverallStatus
import br.com.tscode.checking.domain.model.TransportRequest
import br.com.tscode.checking.domain.model.TransportRequestKind
import br.com.tscode.checking.domain.model.TransportRequestStatus
import br.com.tscode.checking.domain.model.TransportState

// Builder state for the request creation form.
data class TransportBuilderState(
    val kind: TransportRequestKind,
    val selectedWeekdays: List<Int> = when (kind) {
        TransportRequestKind.REGULAR -> listOf(0, 1, 2, 3, 4)
        TransportRequestKind.WEEKEND -> listOf(5, 6)
        TransportRequestKind.EXTRA -> emptyList()
    },
    val requestedDate: String = "",
    val requestedTime: String = "",
)

enum class TransportNotificationTone { Neutral, Success, Error }

data class TransportUiState(
    val isLoading: Boolean = false,
    val transportState: TransportState? = null,
    val localState: TransportLocalState = TransportLocalState(),

    // Address editor
    val addressEditorOpen: Boolean = false,
    val endRuaInput: String = "",
    val zipInput: String = "",
    val isAddressSaving: Boolean = false,

    // Request builder
    val builderState: TransportBuilderState? = null,
    val isRequestSubmitting: Boolean = false,

    // Detail overlay
    val detailRequestId: Int? = null,

    // History panel
    val historyOpen: Boolean = false,

    // Inline status
    val inlineMessage: String = "",
    val inlineMessageTone: TransportNotificationTone = TransportNotificationTone.Neutral,

    // Cancel in-progress set
    val cancellingIds: Set<Int> = emptySet(),

    // Acknowledge overlay
    val acknowledgeRequestId: Int? = null,
) {
    val endRua: String get() = transportState?.endRua ?: ""
    val zip: String get() = transportState?.zip ?: ""
    val hasAddress: Boolean get() = endRua.isNotBlank() && zip.isNotBlank()

    val allRequests: List<TransportRequest>
        get() = (transportState?.requests ?: emptyList()).map { req ->
            when {
                localState.realizedIds.contains(req.requestId) && req.status == TransportRequestStatus.CONFIRMED ->
                    req.copy(status = TransportRequestStatus.REALIZED)
                else -> req
            }
        }

    val visibleRequests: List<TransportRequest>
        get() = allRequests.filter { !localState.dismissedIds.contains(it.requestId) }

    val activeRequests: List<TransportRequest>
        get() = visibleRequests.filter {
            it.isActive && (it.status == TransportRequestStatus.PENDING || it.status == TransportRequestStatus.CONFIRMED)
        }

    val detailRequest: TransportRequest?
        get() = detailRequestId?.let { id -> visibleRequests.find { it.requestId == id } }

    val canAddressSubmit: Boolean
        get() = !isAddressSaving && endRuaInput.trim().length >= 3 && zipInput.trim().length == 6

    val canCreateRequest: Boolean
        get() = hasAddress && builderState != null

    val canSubmitBuilder: Boolean
        get() {
            val b = builderState ?: return false
            if (isRequestSubmitting) return false
            return when (b.kind) {
                TransportRequestKind.REGULAR -> b.selectedWeekdays.isNotEmpty() && b.selectedWeekdays.size <= 5
                TransportRequestKind.WEEKEND -> b.selectedWeekdays.isNotEmpty()
                TransportRequestKind.EXTRA -> b.requestedDate.isNotBlank()
            }
        }
}
