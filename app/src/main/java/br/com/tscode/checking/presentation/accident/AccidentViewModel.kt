package br.com.tscode.checking.presentation.accident

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tscode.checking.core.error.ApiError
import br.com.tscode.checking.core.result.AppResult
import br.com.tscode.checking.domain.model.AccidentActiveItem
import br.com.tscode.checking.domain.model.AccidentSafetyStatus
import br.com.tscode.checking.domain.model.AccidentZone
import br.com.tscode.checking.domain.model.HistoryState
import br.com.tscode.checking.domain.repository.AccidentRepository
import br.com.tscode.checking.i18n.t
import br.com.tscode.checking.platform.camera.VIDEO_CONTENT_TYPE
import br.com.tscode.checking.platform.camera.VideoRecorder
import java.io.File
import java.util.UUID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val POLL_INTERVAL_MS = 30_000L
private const val AUTO_CHECKIN_RETRIES = 3
private const val AUTO_CHECKIN_DELAY_MS = 3_000L

@HiltViewModel
class AccidentViewModel @Inject constructor(
    private val repository: AccidentRepository,
    val videoRecorder: VideoRecorder,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccidentUiState())
    val uiState: StateFlow<AccidentUiState> = _uiState.asStateFlow()

    private var chave: String = ""
    private var sseJob: Job? = null
    private var pollJob: Job? = null
    // Callback to notify CheckViewModel that automatic activities should be disabled
    var onDisableAutoActivities: (() -> Unit)? = null

    // ---- Lifecycle (called from CheckScreen) ----

    fun onLogin(chaveValue: String) {
        chave = chaveValue
        _uiState.update {
            AccidentUiState() // reset all session-scoped state
        }
        refreshState()
        startSseStream()
        startPolling()
    }

    fun onLogout() {
        sseJob?.cancel(); sseJob = null
        pollJob?.cancel(); pollJob = null
        chave = ""
        _uiState.value = AccidentUiState()
    }

    fun onCheckWebState(historyState: HistoryState, activeProject: String) {
        val wasCheckin = _uiState.value.currentActionIsCheckin
        _uiState.update {
            it.copy(
                hasCurrentDayCheckin = historyState.hasCurrentDayCheckin,
                currentActionIsCheckin = historyState.currentAction?.name == "CHECKIN",
            )
        }
        // Transition checkout→checkin: schedule a refresh
        val nowCheckin = _uiState.value.currentActionIsCheckin
        if (!wasCheckin && nowCheckin) {
            refreshState()
        }
        // Trigger auto-checkin for active accidents if needed
        _uiState.value.activeAccidents.forEach { accident ->
            val scenario = _uiState.value.inquiryScenario(accident, activeProject, true)
            if (scenario == InquiryScenario.TriggerAutoCheckin) {
                triggerAutoCheckin(accident.accidentId)
            }
        }
    }

    // ---- State load ----

    private fun refreshState() {
        viewModelScope.launch {
            when (val result = repository.getState(chave)) {
                is AppResult.Success -> {
                    val state = result.data
                    _uiState.update { current ->
                        val banner = if (state.isActive && state.projectName != null)
                            t("accident.notification.bannerTemplate", mapOf("project" to state.projectName))
                        else ""
                        val nextState = current.copy(
                            accidentState = state,
                            bannerMessage = banner,
                            isLoading = false,
                        )
                        reconcileAckQueue(nextState)
                    }
                }
                is AppResult.Failure -> _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun reconcileAckQueue(state: AccidentUiState): AccidentUiState {
        val activeIds = state.activeAccidents.map { it.accidentId }.toSet()
        // Remove queued accidents that are no longer active
        val prunedQueue = state.ackDialogQueue.filter { activeIds.contains(it.accidentId) }
        // Enqueue any new active accidents not yet shown this session
        val newToEnqueue = state.activeAccidents.filter { accident ->
            !state.ackShownForAccidentIds.contains(accident.accidentId) &&
                    prunedQueue.none { it.accidentId == accident.accidentId } &&
                    state.ackDialogShowing?.accidentId != accident.accidentId
        }
        val nextQueue = prunedQueue + newToEnqueue
        val nextShown = if (state.ackDialogShowing == null && nextQueue.isNotEmpty()) {
            nextQueue.first()
        } else state.ackDialogShowing
        val nextQueueAfterDequeue = if (state.ackDialogShowing == null && nextQueue.isNotEmpty()) {
            nextQueue.drop(1)
        } else nextQueue
        val nextShownIds = state.ackShownForAccidentIds +
                (nextShown?.accidentId?.let { setOf(it) } ?: emptySet())
        return state.copy(
            ackDialogQueue = nextQueueAfterDequeue,
            ackDialogShowing = nextShown,
            ackShownForAccidentIds = nextShownIds,
        )
    }

    // ---- SSE stream ----

    private fun startSseStream() {
        sseJob?.cancel()
        sseJob = viewModelScope.launch {
            repository.streamCheckEvents(chave)
                .catch {}
                .collect { data ->
                    if (data.startsWith("accident_") || data.contains("accident")) {
                        refreshState()
                    }
                }
        }
    }

    private fun startPolling() {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            while (true) {
                delay(POLL_INTERVAL_MS)
                if (_uiState.value.isActive) refreshState()
            }
        }
    }

    // ---- Zone reporting ----

    fun onZoneSafetyTap(accidentId: Int) {
        _uiState.update { it.copy(zoneConfirmStep = ZoneConfirmStep.ConfirmSafety(accidentId)) }
    }

    fun onZoneAccidentTap() {
        _uiState.update { it.copy(zoneConfirmStep = ZoneConfirmStep.AccidentExpanded) }
    }

    fun onZoneAccidentOkTap(accidentId: Int) {
        _uiState.update { it.copy(zoneConfirmStep = ZoneConfirmStep.ConfirmAccidentOk(accidentId)) }
    }

    fun onZoneAccidentHelpTap(accidentId: Int) {
        _uiState.update { it.copy(zoneConfirmStep = ZoneConfirmStep.ConfirmAccidentHelp(accidentId)) }
    }

    fun onZoneConfirmDismiss() {
        _uiState.update { it.copy(zoneConfirmStep = ZoneConfirmStep.None) }
    }

    fun onZoneConfirm() {
        val step = _uiState.value.zoneConfirmStep
        when (step) {
            is ZoneConfirmStep.ConfirmSafety -> submitReport(step.accidentId, AccidentZone.SAFETY, AccidentSafetyStatus.OK)
            is ZoneConfirmStep.ConfirmAccidentOk -> submitReport(step.accidentId, AccidentZone.ACCIDENT, AccidentSafetyStatus.OK)
            is ZoneConfirmStep.ConfirmAccidentHelp -> submitReport(step.accidentId, AccidentZone.ACCIDENT, AccidentSafetyStatus.HELP)
            else -> {}
        }
        _uiState.update { it.copy(zoneConfirmStep = ZoneConfirmStep.None) }
    }

    private fun submitReport(accidentId: Int, zone: AccidentZone, status: AccidentSafetyStatus) {
        viewModelScope.launch {
            when (val result = repository.report(chave, zone, status)) {
                is AppResult.Success -> {
                    _uiState.update { current ->
                        val banner = if (result.data.isActive && result.data.projectName != null)
                            t("accident.notification.bannerTemplate", mapOf("project" to result.data.projectName))
                        else ""
                        current.copy(
                            accidentState = result.data,
                            bannerMessage = banner,
                            reportSentForAccidentId = accidentId,
                        )
                    }
                    if (status == AccidentSafetyStatus.HELP) {
                        triggerEmergencyCall()
                    }
                }
                is AppResult.Failure -> { /* show inline error if needed */ }
            }
        }
    }

    // ---- Emergency call ----

    fun triggerEmergencyCall() {
        viewModelScope.launch {
            when (val result = repository.emergencyCall(chave)) {
                is AppResult.Success -> {
                    val msg = t(
                        "accident.emergency.callInitiated",
                        mapOf("label" to result.data.callNumberLabel),
                    )
                    _uiState.update { it.copy(emergencyMessage = msg) }
                }
                is AppResult.Failure -> {
                    val msg = if (result.error is ApiError.Conflict)
                        t("accident.emergency.alreadyCalled")
                    else t("accident.emergency.callFailed")
                    _uiState.update { it.copy(emergencyMessage = msg) }
                }
            }
        }
    }

    fun onEmergencyMessageDismiss() = _uiState.update { it.copy(emergencyMessage = "") }

    // ---- Auto-checkin retries (T5.5) ----

    private fun triggerAutoCheckin(accidentId: Int) {
        if (_uiState.value.autoCheckinStatus.containsKey(accidentId)) return
        _uiState.update {
            it.copy(autoCheckinStatus = it.autoCheckinStatus + (accidentId to AutoCheckinStatus.PENDING))
        }
        viewModelScope.launch {
            var success = false
            repeat(AUTO_CHECKIN_RETRIES) { attempt ->
                if (success) return@repeat
                delay(if (attempt == 0) 0L else AUTO_CHECKIN_DELAY_MS)
                when (repository.getState(chave)) {
                    is AppResult.Success -> {
                        // Check if state now shows checkin
                        refreshState()
                        if (_uiState.value.currentActionIsCheckin) {
                            success = true
                        }
                    }
                    is AppResult.Failure -> {}
                }
            }
            if (success) {
                _uiState.update {
                    it.copy(autoCheckinStatus = it.autoCheckinStatus + (accidentId to AutoCheckinStatus.SUCCESS))
                }
            } else {
                _uiState.update {
                    it.copy(autoCheckinStatus = it.autoCheckinStatus + (accidentId to AutoCheckinStatus.FAILED))
                }
                onDisableAutoActivities?.invoke()
            }
        }
    }

    // ---- Acknowledge dialog queue (T5.4) ----

    fun onAckConfirm() {
        val showing = _uiState.value.ackDialogShowing ?: return
        viewModelScope.launch {
            repository.acknowledge(chave, showing.accidentId)
            // Drain the queue
            _uiState.update { current ->
                val next = current.ackDialogQueue.firstOrNull()
                val remaining = current.ackDialogQueue.drop(1)
                val nextShownIds = current.ackShownForAccidentIds +
                        (next?.accidentId?.let { setOf(it) } ?: emptySet())
                current.copy(
                    ackDialogShowing = next,
                    ackDialogQueue = remaining,
                    ackShownForAccidentIds = nextShownIds,
                )
            }
            // Trigger auto-checkin for the acked accident
            triggerAutoCheckin(showing.accidentId)
            refreshState()
        }
    }

    fun onAckDismiss() {
        _uiState.update { current ->
            val next = current.ackDialogQueue.firstOrNull()
            val remaining = current.ackDialogQueue.drop(1)
            current.copy(ackDialogShowing = next, ackDialogQueue = remaining)
        }
    }

    // ---- Report wizard (T5.3) ----

    fun onReportButtonTap() {
        if (_uiState.value.isActive) {
            _uiState.update { it.copy(actionsDialogOpen = true) }
        } else {
            openWizard()
        }
    }

    fun onActionsDialogDismiss() = _uiState.update { it.copy(actionsDialogOpen = false) }

    fun openWizard() {
        _uiState.update {
            it.copy(
                actionsDialogOpen = false,
                wizardOpen = true,
                wizardState = WizardState(),
            )
        }
        loadWizardProjects()
    }

    fun onWizardDismiss() = _uiState.update { it.copy(wizardOpen = false, wizardState = null) }

    private fun loadWizardProjects() {
        viewModelScope.launch {
            _uiState.update { it.copy(wizardState = it.wizardState?.copy(isLoadingProjects = true)) }
            when (val result = repository.wizardProjects(chave)) {
                is AppResult.Success -> _uiState.update { state ->
                    state.copy(
                        wizardState = state.wizardState?.copy(
                            isLoadingProjects = false,
                            projects = result.data.map { WizardProject(it.first, it.second) },
                        )
                    )
                }
                is AppResult.Failure -> _uiState.update { state ->
                    state.copy(wizardState = state.wizardState?.copy(isLoadingProjects = false))
                }
            }
        }
    }

    fun onWizardProjectSelected(id: Int, name: String) {
        _uiState.update { state ->
            state.copy(
                wizardState = state.wizardState?.copy(
                    selectedProjectId = id,
                    selectedProjectName = name,
                )
            )
        }
    }

    fun onWizardNextFromProject() {
        val ws = _uiState.value.wizardState ?: return
        if (!ws.canProceedProject) return
        _uiState.update { state ->
            state.copy(
                wizardState = state.wizardState?.copy(
                    step = WizardStep.LOCATION,
                    isLoadingLocations = true,
                )
            )
        }
        loadWizardLocations(ws.selectedProjectId!!)
    }

    private fun loadWizardLocations(projectId: Int) {
        viewModelScope.launch {
            when (val result = repository.wizardLocations(chave, projectId)) {
                is AppResult.Success -> _uiState.update { state ->
                    state.copy(
                        wizardState = state.wizardState?.copy(
                            isLoadingLocations = false,
                            locations = result.data.map { WizardLocation(it.first, it.second, it.third) },
                        )
                    )
                }
                is AppResult.Failure -> _uiState.update { state ->
                    state.copy(wizardState = state.wizardState?.copy(isLoadingLocations = false))
                }
            }
        }
    }

    fun onWizardLocationSelected(id: Int, name: String) {
        _uiState.update { state ->
            state.copy(
                wizardState = state.wizardState?.copy(
                    selectedLocationId = id,
                    selectedLocationName = name,
                    useCustomLocation = false,
                )
            )
        }
    }

    fun onWizardCustomLocationToggled() {
        _uiState.update { state ->
            val ws = state.wizardState ?: return@update state
            state.copy(
                wizardState = ws.copy(
                    useCustomLocation = !ws.useCustomLocation,
                    selectedLocationId = if (!ws.useCustomLocation) null else ws.selectedLocationId,
                )
            )
        }
    }

    fun onWizardCustomLocationChanged(value: String) {
        _uiState.update { state ->
            state.copy(wizardState = state.wizardState?.copy(customLocationName = value))
        }
    }

    fun onWizardNextFromLocation() {
        val ws = _uiState.value.wizardState ?: return
        if (!ws.canProceedLocation) return
        _uiState.update { state ->
            state.copy(wizardState = state.wizardState?.copy(step = WizardStep.DESCRIPTION))
        }
    }

    fun onWizardDescriptionChanged(value: String) {
        if (value.length > 500) return
        _uiState.update { state ->
            state.copy(wizardState = state.wizardState?.copy(description = value))
        }
    }

    fun onWizardNextFromDescription() {
        _uiState.update { state ->
            state.copy(wizardState = state.wizardState?.copy(step = WizardStep.SITUATION))
        }
    }

    fun onWizardSituationSelected(zone: AccidentZone, status: AccidentSafetyStatus) {
        _uiState.update { state ->
            state.copy(
                wizardState = state.wizardState?.copy(
                    selectedZone = zone,
                    selectedStatus = status,
                )
            )
        }
    }

    fun onWizardNextFromSituation() {
        val ws = _uiState.value.wizardState ?: return
        if (!ws.canProceedSituation) return
        _uiState.update { state ->
            state.copy(wizardState = state.wizardState?.copy(step = WizardStep.CONFIRM))
        }
    }

    fun onWizardBack() {
        _uiState.update { state ->
            val ws = state.wizardState ?: return@update state
            val prevStep = when (ws.step) {
                WizardStep.PROJECT -> null // close wizard
                WizardStep.LOCATION -> WizardStep.PROJECT
                WizardStep.DESCRIPTION -> WizardStep.LOCATION
                WizardStep.SITUATION -> WizardStep.DESCRIPTION
                WizardStep.CONFIRM -> WizardStep.SITUATION
            }
            if (prevStep == null) state.copy(wizardOpen = false, wizardState = null)
            else state.copy(wizardState = ws.copy(step = prevStep, errorMessage = ""))
        }
    }

    fun onWizardConfirmSubmit() {
        val ws = _uiState.value.wizardState ?: return
        if (!ws.canSubmitConfirm) return
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(wizardState = state.wizardState?.copy(isSubmitting = true, errorMessage = ""))
            }
            val result = repository.open(
                chave = chave,
                projectId = ws.selectedProjectId!!,
                locationId = ws.effectiveLocationId,
                customLocationName = ws.effectiveCustomName,
                zone = ws.selectedZone!!,
                status = ws.selectedStatus!!,
                description = ws.description.ifBlank { null },
            )
            when (result) {
                is AppResult.Success -> {
                    val banner = if (result.data.isActive && result.data.projectName != null)
                        t("accident.notification.bannerTemplate", mapOf("project" to result.data.projectName))
                    else ""
                    _uiState.update { state ->
                        val nextState = state.copy(
                            accidentState = result.data,
                            bannerMessage = banner,
                            wizardOpen = false,
                            wizardState = null,
                        )
                        reconcileAckQueue(nextState)
                    }
                }
                is AppResult.Failure -> {
                    val errMsg = if (result.error is ApiError.Conflict)
                        t("accident.wizard.conflictAlreadyActive")
                    else t("status.apiCommunicationFailure")
                    _uiState.update { state ->
                        state.copy(wizardState = state.wizardState?.copy(isSubmitting = false, errorMessage = errMsg))
                    }
                }
            }
        }
    }

    // ---- Video recording (T5.7) ----

    fun onVideoRecordOpen() =
        _uiState.update { it.copy(actionsDialogOpen = false, videoScreenOpen = true) }

    fun onVideoRecordDone() =
        _uiState.update { it.copy(videoScreenOpen = false) }

    suspend fun uploadVideo(
        file: File,
        contentType: String,
        onProgress: (Float) -> Unit,
    ) {
        repository.uploadVideo(
            chave = chave,
            idempotencyKey = UUID.randomUUID().toString(),
            videoFile = file,
            contentType = contentType,
            onProgress = onProgress,
        )
    }

    // ---- Cleanup ----

    fun onNeedsDisableAutoActivitiesHandled() =
        _uiState.update { it.copy(needsDisableAutoActivities = false) }
}
