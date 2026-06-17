package br.com.tscode.checking.presentation.transport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tscode.checking.core.result.AppResult
import br.com.tscode.checking.data.local.AppPreferencesDataSource
import br.com.tscode.checking.domain.clientstate.TransportLocalState
import br.com.tscode.checking.domain.clientstate.loadTransportLocalState
import br.com.tscode.checking.domain.clientstate.saveTransportLocalState
import br.com.tscode.checking.domain.model.TransportRequestKind
import br.com.tscode.checking.domain.model.TransportRequestStatus
import br.com.tscode.checking.domain.repository.TransportRepository
import br.com.tscode.checking.i18n.t
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

private const val AUTO_REFRESH_INTERVAL_MS = 30_000L

@HiltViewModel
class TransportViewModel @Inject constructor(
    private val repository: TransportRepository,
    private val appPreferences: AppPreferencesDataSource,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransportUiState())
    val uiState: StateFlow<TransportUiState> = _uiState.asStateFlow()

    private var chave: String = ""
    private var sseJob: Job? = null
    private var autoRefreshJob: Job? = null

    // Called when CheckScreen opens the transport modal.
    fun onOpen(chaveValue: String) {
        chave = chaveValue
        loadState()
        startSseStream()
        scheduleAutoRefresh()
    }

    fun onClose() {
        sseJob?.cancel()
        sseJob = null
        autoRefreshJob?.cancel()
        autoRefreshJob = null
        _uiState.value = TransportUiState()
    }

    // ---- State load ----

    private fun loadState() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val localJson = appPreferences.transportLocalJson.first()
            val localState = loadTransportLocalState(localJson, chave)
            when (val result = repository.getState(chave)) {
                is AppResult.Success -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        transportState = result.data,
                        localState = localState,
                    )
                }
                is AppResult.Failure -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        localState = localState,
                        inlineMessage = t("transport.messages.loadFailed"),
                        inlineMessageTone = TransportNotificationTone.Error,
                    )
                }
            }
        }
    }

    private fun refreshState() {
        viewModelScope.launch {
            when (val result = repository.getState(chave)) {
                is AppResult.Success -> _uiState.update { state ->
                    state.copy(transportState = result.data)
                }
                is AppResult.Failure -> { /* silent on background refresh */ }
            }
        }
    }

    // ---- SSE stream ----

    private fun startSseStream() {
        sseJob?.cancel()
        sseJob = viewModelScope.launch {
            repository.streamEvents(chave)
                .catch { /* stream errors are handled by retryWhen in SseDataSource */ }
                .collect { refreshState() }
        }
    }

    // ---- Auto-refresh timer ----

    private fun scheduleAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = viewModelScope.launch {
            while (true) {
                delay(AUTO_REFRESH_INTERVAL_MS)
                val state = _uiState.value
                if (state.activeRequests.isNotEmpty()) {
                    refreshState()
                }
            }
        }
    }

    // ---- Address editor ----

    fun onAddressEditorOpen() {
        val s = _uiState.value
        _uiState.update {
            it.copy(
                addressEditorOpen = true,
                endRuaInput = s.endRua,
                zipInput = s.zip,
            )
        }
    }

    fun onAddressEditorClose() = _uiState.update { it.copy(addressEditorOpen = false) }

    fun onEndRuaChanged(value: String) = _uiState.update { it.copy(endRuaInput = value) }

    fun onZipChanged(value: String) {
        val digits = value.filter { it.isDigit() }.take(6)
        _uiState.update { it.copy(zipInput = digits) }
    }

    fun onAddressSubmit() {
        val state = _uiState.value
        if (!state.canAddressSubmit) return
        viewModelScope.launch {
            _uiState.update { it.copy(isAddressSaving = true) }
            when (val result = repository.updateAddress(chave, state.endRuaInput.trim(), state.zipInput.trim())) {
                is AppResult.Success -> _uiState.update {
                    it.copy(
                        isAddressSaving = false,
                        addressEditorOpen = false,
                        transportState = result.data,
                        inlineMessage = t("transport.messages.addressUpdated"),
                        inlineMessageTone = TransportNotificationTone.Success,
                    )
                }
                is AppResult.Failure -> _uiState.update {
                    it.copy(
                        isAddressSaving = false,
                        inlineMessage = t("transport.messages.addressUpdateFailed"),
                        inlineMessageTone = TransportNotificationTone.Error,
                    )
                }
            }
        }
    }

    // ---- Request builder ----

    fun onBuilderOpen(kind: TransportRequestKind) =
        _uiState.update {
            it.copy(
                builderState = TransportBuilderState(
                    kind = kind,
                    // Transporte Extra defaults to today's date (stored ISO yyyy-MM-dd).
                    requestedDate = if (kind == TransportRequestKind.EXTRA) LocalDate.now().toString() else "",
                ),
            )
        }

    fun onBuilderClose() = _uiState.update { it.copy(builderState = null) }

    fun onBuilderWeekdayToggled(day: Int) {
        val b = _uiState.value.builderState ?: return
        val days = if (b.selectedWeekdays.contains(day)) b.selectedWeekdays - day
        else b.selectedWeekdays + day
        _uiState.update { it.copy(builderState = b.copy(selectedWeekdays = days)) }
    }

    fun onBuilderDateChanged(value: String) {
        val b = _uiState.value.builderState ?: return
        _uiState.update { it.copy(builderState = b.copy(requestedDate = value)) }
    }

    fun onBuilderTimeChanged(value: String) {
        val b = _uiState.value.builderState ?: return
        _uiState.update { it.copy(builderState = b.copy(requestedTime = value)) }
    }

    fun onBuilderSubmit() {
        val state = _uiState.value
        val b = state.builderState ?: return
        if (!state.canSubmitBuilder) return
        viewModelScope.launch {
            _uiState.update { it.copy(isRequestSubmitting = true) }
            val result = repository.createRequest(
                chave = chave,
                kind = b.kind,
                requestedTime = b.requestedTime.ifBlank { null },
                requestedDate = if (b.kind == TransportRequestKind.EXTRA) b.requestedDate.ifBlank { null } else null,
                selectedWeekdays = if (b.kind != TransportRequestKind.EXTRA) b.selectedWeekdays else null,
            )
            when (result) {
                is AppResult.Success -> _uiState.update {
                    it.copy(
                        isRequestSubmitting = false,
                        builderState = null,
                        transportState = result.data,
                    )
                }
                is AppResult.Failure -> {
                    val kindLabel = t("transport.kinds.${b.kind.name.lowercase()}")
                    _uiState.update {
                        it.copy(
                            isRequestSubmitting = false,
                            inlineMessage = t("transport.messages.requestFailed", mapOf("requestLabel" to kindLabel)),
                            inlineMessageTone = TransportNotificationTone.Error,
                        )
                    }
                }
            }
        }
    }

    // ---- Request cards ----

    fun onRequestDismiss(requestId: Int) {
        val nextLocal = _uiState.value.localState.withDismissed(requestId)
        persistLocalState(nextLocal)
        _uiState.update { it.copy(localState = nextLocal) }
    }

    fun onMarkRealized(requestId: Int) {
        val nextLocal = _uiState.value.localState.withRealized(requestId)
        persistLocalState(nextLocal)
        _uiState.update { it.copy(localState = nextLocal) }
    }

    fun onCancelRequest(requestId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(cancellingIds = it.cancellingIds + requestId) }
            when (val result = repository.cancelRequest(chave, requestId)) {
                is AppResult.Success -> _uiState.update {
                    it.copy(
                        cancellingIds = it.cancellingIds - requestId,
                        transportState = result.data,
                        inlineMessage = t("transport.messages.cancelSuccess"),
                        inlineMessageTone = TransportNotificationTone.Success,
                    )
                }
                is AppResult.Failure -> _uiState.update {
                    it.copy(
                        cancellingIds = it.cancellingIds - requestId,
                        inlineMessage = t("transport.messages.cancelFailed"),
                        inlineMessageTone = TransportNotificationTone.Error,
                    )
                }
            }
        }
    }

    // ---- Detail overlay ----

    fun onDetailOpen(requestId: Int) = _uiState.update { it.copy(detailRequestId = requestId) }
    fun onDetailClose() = _uiState.update { it.copy(detailRequestId = null) }

    // ---- History panel ----

    fun onHistoryOpen() = _uiState.update { it.copy(historyOpen = true) }
    fun onHistoryClose() = _uiState.update { it.copy(historyOpen = false) }

    // ---- Acknowledge ----

    fun onAcknowledgeOpen(requestId: Int) = _uiState.update { it.copy(acknowledgeRequestId = requestId) }
    fun onAcknowledgeClose() = _uiState.update { it.copy(acknowledgeRequestId = null) }

    fun onAcknowledgeConfirm() {
        val requestId = _uiState.value.acknowledgeRequestId ?: return
        viewModelScope.launch {
            when (val result = repository.acknowledgeRequest(chave, requestId)) {
                is AppResult.Success -> _uiState.update {
                    it.copy(
                        acknowledgeRequestId = null,
                        transportState = result.data,
                    )
                }
                is AppResult.Failure -> _uiState.update { it.copy(acknowledgeRequestId = null) }
            }
        }
    }

    // ---- Local persistence ----

    private fun persistLocalState(state: TransportLocalState) {
        viewModelScope.launch {
            val currentJson = appPreferences.transportLocalJson.first()
            val nextJson = saveTransportLocalState(currentJson, chave, state)
            appPreferences.setTransportLocalJson(nextJson)
        }
    }

    fun clearInlineMessage() = _uiState.update { it.copy(inlineMessage = "") }
}
