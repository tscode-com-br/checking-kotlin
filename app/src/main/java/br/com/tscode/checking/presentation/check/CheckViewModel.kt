package br.com.tscode.checking.presentation.check

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tscode.checking.core.error.ApiError
import br.com.tscode.checking.core.result.AppResult
import br.com.tscode.checking.data.local.AppPreferencesDataSource
import br.com.tscode.checking.data.local.SecurePasswordStore
import br.com.tscode.checking.data.local.activitylog.ActivityLog
import br.com.tscode.checking.domain.clientstate.autofillPetrobrasEmailDomain
import br.com.tscode.checking.domain.clientstate.isPasswordLengthValid
import br.com.tscode.checking.domain.clientstate.isPasswordVerificationInputValid
import br.com.tscode.checking.domain.clientstate.sanitizeSettingsChave
import br.com.tscode.checking.domain.model.ActivityActor
import br.com.tscode.checking.domain.model.ActivityKind
import br.com.tscode.checking.domain.model.CheckAction
import br.com.tscode.checking.domain.model.InformeType
import br.com.tscode.checking.domain.model.MatchStatus
import br.com.tscode.checking.domain.model.UserProjects
import br.com.tscode.checking.domain.repository.AuthRepository
import br.com.tscode.checking.domain.repository.CheckRepository
import br.com.tscode.checking.domain.repository.ProjectRepository
import br.com.tscode.checking.domain.clientstate.UserSettings
import br.com.tscode.checking.domain.clientstate.resolvePersistedUserSettings
import br.com.tscode.checking.domain.clientstate.withPersistedUserSettings
import br.com.tscode.checking.domain.usecase.CaptureLocationUseCase
import br.com.tscode.checking.domain.usecase.LocationCaptureResult
import br.com.tscode.checking.platform.activitylog.ActivityLogger
import br.com.tscode.checking.platform.background.AutoActivityController
import br.com.tscode.checking.platform.background.AutoActivityServiceStartOrigin
import br.com.tscode.checking.platform.background.BackgroundCheckOrchestrator
import br.com.tscode.checking.platform.background.OrchestratorTrigger
import br.com.tscode.checking.platform.background.permissions.PermissionLadder
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import br.com.tscode.checking.i18n.DEFAULT_LANGUAGE
import br.com.tscode.checking.i18n.KnownApiMessages
import br.com.tscode.checking.i18n.SUPPORTED_LANGUAGES
import br.com.tscode.checking.i18n.resolveInitialLanguageCode
import br.com.tscode.checking.i18n.setActiveLanguageCode
import br.com.tscode.checking.i18n.t
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import br.com.tscode.checking.core.time.Clock
import br.com.tscode.checking.domain.offline.PendingCheckEvent
import br.com.tscode.checking.platform.background.offline.OfflineCheckQueue
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CheckViewModel @Inject constructor(
    private val appPreferences: AppPreferencesDataSource,
    private val securePasswordStore: SecurePasswordStore,
    private val authRepository: AuthRepository,
    private val projectRepository: ProjectRepository,
    private val checkRepository: CheckRepository,
    private val captureLocationUseCase: CaptureLocationUseCase,
    private val orchestrator: BackgroundCheckOrchestrator,
    // Offline check queue (P8): manual checks made while offline are queued here and synced on reconnect.
    private val offlineCheckQueue: OfflineCheckQueue,
    private val clock: Clock,
    // Application context (not an Activity — safe to hold in a ViewModel). Used to (re)start the
    // background engine from non-UI flows like auth/session restore (P4).
    @ApplicationContext private val appContext: Context,
    private val activityLogger: ActivityLogger,
    private val activityLog: ActivityLog,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckUiState())
    val uiState: StateFlow<CheckUiState> = _uiState.asStateFlow()

    private val settingsJson = Json { ignoreUnknownKeys = true; coerceInputValues = true }
    // Every settings mutation is a read-modify-write of the same JSON document. Serialize
    // them so a project update cannot overwrite a simultaneous toggle/notification change.
    private val settingsWriteMutex = Mutex()

    private val _languageFlow = MutableStateFlow(DEFAULT_LANGUAGE)
    val languageFlow: StateFlow<String> = _languageFlow.asStateFlow()

    private var passwordVerifyJob: Job? = null
    private var checkSseJob: Job? = null
    private var scheduledPauseSettingsJob: Job? = null
    // plan003 — polls /auth/status while a self-registration is awaiting admin approval.
    private var pendingApprovalPollJob: Job? = null
    private var projectMembershipLoadGeneration: Long = 0
    private var projectMembershipCoordinator: UserProjectMembershipCoordinator? = null
    private var projectMembershipCoordinatorChave: String = ""

    init {
        viewModelScope.launch {
            // Restore language
            val storedLang = appPreferences.language.first()
            val lang = resolveInitialLanguageCode(storedLang.ifEmpty { null })
            setActiveLanguageCode(lang)
            _languageFlow.value = lang

            // Restore chave and trigger status probe
            val storedChave = appPreferences.chave.first()
            if (storedChave.length == 4) {
                val storedPw = securePasswordStore.getPassword(storedChave)
                val settings = loadUserSettings(storedChave)
                _uiState.update {
                    it.copy(
                        chave = storedChave,
                        password = storedPw,
                        automaticActivitiesEnabled = settings.automaticActivitiesEnabled,
                        scheduledPauseEnabled = settings.scheduledPauseEnabled,
                        scheduledPauseFrom = settings.scheduledPauseFrom,
                        scheduledPauseTo = settings.scheduledPauseTo,
                        suspendSaturdays = settings.suspendSaturdays,
                        suspendSundays = settings.suspendSundays,
                        notifyActivities = settings.notifyActivities,
                        notifyScheduledPause = settings.notifyScheduledPause,
                        notifyAccident = settings.notifyAccident,
                        isInitializing = false,
                    )
                }
                probeStatus(storedChave)
            } else {
                _uiState.update { it.copy(isInitializing = false) }
            }
        }
    }

    // ─── Language ────────────────────────────────────────────────────────────

    fun onLanguageSelected(code: String) {
        setActiveLanguageCode(code)
        _languageFlow.value = code
        viewModelScope.launch { appPreferences.setLanguage(code) }
        // Re-evaluate prompt with new language
        _uiState.value.authStatus?.let { status ->
            _uiState.update { it.copy(prompt = resolvePrompt(status)) }
        }
    }

    // ─── Chave ───────────────────────────────────────────────────────────────

    fun onChaveChanged(rawValue: String) {
        val sanitized = sanitizeSettingsChave(rawValue)
        passwordVerifyJob?.cancel()
        stopPendingApprovalPolling()
        stopCheckStream()
        resetProjectMembershipCoordinator()
        stopAutomaticActivityEngine(appContext)

        _uiState.update {
            it.copy(
                chave = sanitized,
                password = "",
                authStatus = null,
                prompt = "",
                isStatusLoading = false,
                statusErrored = false,
                dialogOpen = null,
                dismissedAssistanceForChave = "",
                notificationPrimary = "",
                notificationSecondary = "",
                notificationTone = NotificationTone.None,
                historyState = null,
                transportEnabled = false,
                userProjects = null,
                mainProjectCatalog = emptyList(),
                availableLocations = emptyList(),
                selectedManualLocation = null,
                locationMatch = null,
                isProjectsLoading = false,
                isProjectMembershipSyncing = false,
                isLocationLoading = false,
                automaticActivitiesEnabled = false,
                autoActivitiesHealth = AutoActivitiesHealth.Off,
            )
        }

        viewModelScope.launch {
            appPreferences.setChave(sanitized)
            if (sanitized.length != 4) {
                authRepository.logout()
                return@launch
            }
            val storedPw = securePasswordStore.getPassword(sanitized)
            val settings = loadUserSettings(sanitized)
            if (_uiState.value.chave != sanitized) return@launch
            _uiState.update {
                it.copy(
                    password = storedPw,
                    automaticActivitiesEnabled = settings.automaticActivitiesEnabled,
                    scheduledPauseEnabled = settings.scheduledPauseEnabled,
                    scheduledPauseFrom = settings.scheduledPauseFrom,
                    scheduledPauseTo = settings.scheduledPauseTo,
                    suspendSaturdays = settings.suspendSaturdays,
                    suspendSundays = settings.suspendSundays,
                    notifyActivities = settings.notifyActivities,
                    notifyScheduledPause = settings.notifyScheduledPause,
                    notifyAccident = settings.notifyAccident,
                )
            }
            probeStatus(sanitized)
        }
    }

    // ─── Password ─────────────────────────────────────────────────────────────

    fun onPasswordChanged(rawValue: String) {
        _uiState.update { it.copy(password = rawValue) }
        passwordVerifyJob?.cancel()

        val status = _uiState.value.authStatus ?: return
        if (!status.hasPassword || !isPasswordVerificationInputValid(rawValue)) return

        passwordVerifyJob = viewModelScope.launch {
            delay(800)
            attemptLogin(_uiState.value.chave, rawValue)
        }
    }

    // ─── Status probe ─────────────────────────────────────────────────────────

    private suspend fun probeStatus(chave: String) {
        authRepository.logout() // clear stale session before re-probing
        if (_uiState.value.chave != chave) return
        _uiState.update { it.copy(isStatusLoading = true) }

        when (val result = authRepository.getStatus(chave)) {
            is AppResult.Success -> {
                if (_uiState.value.chave != chave) return
                val status = result.data
                _uiState.update {
                    it.copy(
                        authStatus = status,
                        isStatusLoading = false,
                        prompt = resolvePrompt(status),
                    )
                }
                if (status.pendingApproval) {
                    // plan003 — awaiting admin approval: red bar; do NOT auto-open the registration form.
                    _uiState.update {
                        it.copy(
                            notificationPrimary = t("auth.awaitingApproval", lang = _languageFlow.value),
                            notificationSecondary = "",
                            notificationTone = NotificationTone.Error,
                        )
                    }
                    startPendingApprovalPolling(chave)
                } else {
                    stopPendingApprovalPolling()
                    maybeAutoOpenAssistanceDialog(status)
                    // Auto-login if we have a stored password (also fires when approval flips found→true).
                    val storedPw = _uiState.value.password
                    if (status.hasPassword && storedPw.isNotEmpty()) {
                        attemptLogin(chave, storedPw)
                    }
                }
            }
            is AppResult.Failure -> {
                if (_uiState.value.chave != chave) return
                _uiState.update {
                    it.copy(isStatusLoading = false, statusErrored = true)
                }
            }
        }
    }

    // plan003 — light polling while awaiting approval. Each tick re-probes; probeStatus keeps this alive
    // (still pending) or lets it exit: approved (found→true) → auto-login → engine; rejected → silent
    // return to the unknown-key state.
    private fun startPendingApprovalPolling(chave: String) {
        if (pendingApprovalPollJob?.isActive == true) return
        pendingApprovalPollJob = viewModelScope.launch {
            while (_uiState.value.isAwaitingApproval && _uiState.value.chave == chave) {
                delay(10_000L) // ~10s between approval re-checks
                if (_uiState.value.chave != chave || !_uiState.value.isAwaitingApproval) break
                probeStatus(chave)
            }
        }
    }

    private fun stopPendingApprovalPolling() {
        pendingApprovalPollJob?.cancel()
        pendingApprovalPollJob = null
    }

    private fun resolvePrompt(status: br.com.tscode.checking.domain.model.AuthStatus): String {
        val lang = _languageFlow.value
        return when {
            status.authenticated -> ""
            status.hasPassword -> t("auth.enterPasswordPrompt", lang = lang)
            else -> t("auth.createPasswordPrompt", lang = lang)
        }
    }

    private fun maybeAutoOpenAssistanceDialog(status: br.com.tscode.checking.domain.model.AuthStatus) {
        val state = _uiState.value
        // plan003 — a pending registration is NOT an unknown user: never re-open the registration form.
        if (status.pendingApproval) return
        if (state.dismissedAssistanceForChave == status.chave) return
        when {
            status.found && !status.hasPassword && !status.authenticated ->
                _uiState.update { it.copy(dialogOpen = CheckDialog.PasswordChange) }
            !status.found ->
                _uiState.update {
                    it.copy(
                        dialogOpen = CheckDialog.SelfRegistration,
                        selfRegistrationFields = it.selfRegistrationFields.copy(chave = status.chave),
                    )
                }
        }
    }

    // ─── Login ────────────────────────────────────────────────────────────────

    private suspend fun attemptLogin(chave: String, password: String) {
        if (chave.length != 4 || !isPasswordVerificationInputValid(password)) return
        val lang = _languageFlow.value

        _uiState.update {
            it.copy(
                notificationPrimary = t("status.passwordVerifying", lang = lang),
                notificationTone = NotificationTone.Info,
            )
        }

        when (val result = authRepository.login(chave, password)) {
            is AppResult.Success -> {
                if (_uiState.value.chave != chave) return
                val status = result.data
                _uiState.update { it.copy(authStatus = status, prompt = resolvePrompt(status)) }
                if (status.authenticated) {
                    securePasswordStore.setPassword(chave, password)
                    onAuthenticationSucceeded(chave, status)
                    activityLogger.logAuth("Signed in.") // plan004
                } else {
                    _uiState.update {
                        it.copy(
                            notificationPrimary = KnownApiMessages.localizeApiMessage(status.message, lang),
                            notificationTone = NotificationTone.Error,
                        )
                    }
                    activityLogger.logError("Sign-in failed.") // plan004
                }
            }
            is AppResult.Failure -> {
                if (_uiState.value.chave != chave) return
                when (result.error) {
                    is ApiError.Unauthorized ->
                        _uiState.update {
                            it.copy(
                                notificationPrimary = "",
                                notificationSecondary = "",
                                notificationTone = NotificationTone.None,
                            )
                        }
                    else ->
                        _uiState.update {
                            it.copy(
                                notificationPrimary = t("status.apiCommunicationFailure", lang = lang),
                                notificationTone = NotificationTone.Error,
                            )
                        }
                }
                activityLogger.logError("Sign-in failed.") // plan004
            }
        }
    }

    private fun onAuthenticationSucceeded(
        chave: String,
        status: br.com.tscode.checking.domain.model.AuthStatus,
    ) {
        if (_uiState.value.chave != chave) return
        val lang = _languageFlow.value
        _uiState.update {
            it.copy(
                notificationPrimary = t("status.authenticationCompleted", lang = lang),
                notificationTone = NotificationTone.Teal,
            )
        }
        // Do not start the automatic engine from locally persisted membership data here.
        // loadUserProjects() first replaces it with the API's authoritative state and only
        // then reconciles the engine/geofences.
        // Load history in background
        viewModelScope.launch {
            _uiState.update { it.copy(isHistoryLoading = true) }
            when (val r = authRepository.getHistory(chave)) {
                is AppResult.Success -> {
                    if (!isCurrentAuthenticatedUser(chave)) return@launch
                    _uiState.update {
                        it.copy(
                            historyState = r.data,
                            transportEnabled = r.data.transportEnabled,
                            isHistoryLoading = false,
                            // Default the "Local" dropdown to the last activity's location
                            // (don't override an explicit user selection).
                            selectedManualLocation = it.selectedManualLocation ?: r.data.currentLocal,
                        )
                    }
                    // SSE/foreground GET is authoritative, but the orchestrator reconciles it only
                    // when a persisted deferral for this exact occurrence already exists.
                    _uiState.value.userProjects?.activeProject
                        ?.takeIf { it.isNotEmpty() }
                        ?.let { project ->
                            orchestrator.onServerConfirmedState(chave, project, r.data)
                        }
                }
                is AppResult.Failure -> {
                    if (_uiState.value.chave != chave) return@launch
                    _uiState.update { it.copy(isHistoryLoading = false) }
                }
            }
        }
        // Load user projects, the full project catalogue, and available locations
        viewModelScope.launch { loadUserProjects(chave) }
        viewModelScope.launch { loadMainProjectCatalog(chave) }
        // P5: compute the one-time first-login nudge (per-chave). Read the persisted dismissal flag,
        // then apply the pure predicate: show only if authenticated + auto OFF + not yet dismissed.
        viewModelScope.launch {
            val dismissed = appPreferences.getFlag(nudgeFlag(chave)).first()
            if (!isCurrentAuthenticatedUser(chave)) return@launch
            _uiState.update {
                it.copy(
                    showAutoActivitiesNudge = shouldShowAutoActivitiesNudge(
                        authenticated = status.authenticated,
                        autoEnabled = it.automaticActivitiesEnabled,
                        dismissed = dismissed,
                    ),
                )
            }
        }
        // GPS is NOT captured automatically here. captureLocation() runs only when automatic
        // activities are enabled AND location permission is sufficient — the screen drives it
        // once it has evaluated the live permission state (§ item 2).
        // Subscribe to check-state SSE so server-side changes (e.g. an admin toggling a
        // project's transport flag) reflect live without re-login.
        startCheckStream(chave)
    }

    // ─── Check-state realtime (SSE) + foreground refresh ──────────────────────

    private fun startCheckStream(chave: String) {
        checkSseJob?.cancel()
        checkSseJob = viewModelScope.launch {
            checkRepository.streamEvents(chave)
                .catch { /* reconnection handled by retryWhen in SseDataSource */ }
                .collect { refreshCheckState() }
        }
    }

    private fun stopCheckStream() {
        checkSseJob?.cancel()
        checkSseJob = null
    }

    // Re-fetch /check/state — authoritative source for history + the transport flag.
    private fun refreshCheckState() {
        val chave = _uiState.value.chave
        if (chave.length != 4 || !_uiState.value.isAuthenticated) return
        viewModelScope.launch {
            when (val r = checkRepository.getState(chave)) {
                is AppResult.Success -> {
                    if (!isCurrentAuthenticatedUser(chave)) return@launch
                    _uiState.update {
                        it.copy(
                            historyState = r.data,
                            transportEnabled = r.data.transportEnabled,
                            selectedManualLocation = it.selectedManualLocation ?: r.data.currentLocal,
                        )
                    }
                    _uiState.value.userProjects?.activeProject
                        ?.takeIf { it.isNotEmpty() }
                        ?.let { project ->
                            orchestrator.onServerConfirmedState(chave, project, r.data)
                        }
                }
                is AppResult.Failure -> {
                    if (_uiState.value.chave == chave && r.error is ApiError.Unauthorized) {
                        handleAuthExpiry()
                    }
                }
            }
        }
    }

    // Called from the screen on ON_RESUME — re-syncs state after the app returns to the
    // foreground (covers changes missed while backgrounded / SSE disconnected).
    fun onForegroundResume() {
        // plan003 — while awaiting approval, re-check status on foreground (catches an approval made
        // while backgrounded; approval → auto-login → engine via probeStatus).
        if (_uiState.value.isAwaitingApproval) {
            val chave = _uiState.value.chave
            if (chave.length == 4) viewModelScope.launch { probeStatus(chave) }
            return
        }
        if (_uiState.value.isAuthenticated) {
            refreshCheckState()
            val chave = _uiState.value.chave
            // Membership may have changed in the web/iOS app while Android was in the
            // background. Refresh it before any automatic evaluation.
            viewModelScope.launch { loadUserProjects(chave) }
        }
    }

    // ─── Auth expiry ──────────────────────────────────────────────────────────

    // 401/403 on any protected call → silently reset to auth prompt (§8.2).
    private fun handleAuthExpiry() {
        stopCheckStream()
        resetProjectMembershipCoordinator()
        stopAutomaticActivityEngine(appContext)
        checkRepository.invalidateGeofenceCache()
        _uiState.update {
            it.copy(
                authStatus = it.authStatus?.copy(authenticated = false),
                isSubmitting = false,
                isProjectsLoading = false,
                isProjectMembershipSyncing = false,
                isHistoryLoading = false,
                isLocationLoading = false,
                notificationPrimary = "",
                notificationSecondary = "",
                notificationTone = NotificationTone.None,
                userProjects = null,
                mainProjectCatalog = emptyList(),
                availableLocations = emptyList(),
                historyState = null,
                locationMatch = null,
                selectedManualLocation = null,
                // P5: no nudge once the session is gone.
                showAutoActivitiesNudge = false,
            )
        }
    }

    // ─── Location ─────────────────────────────────────────────────────────────

    fun captureLocation() {
        val s = _uiState.value
        // No GPS unless automatic activities are on AND location permission is sufficient
        // ("Allow all the time" + precise). Manual mode uses the "Local" dropdown instead.
        if (!s.automaticActivitiesEnabled || !s.locationPermissionSufficient) return
        if (s.isLocationLoading) return
        _uiState.update { it.copy(isLocationLoading = true) }
        viewModelScope.launch {
            val threshold = _uiState.value.locationMatch?.accuracyThresholdMeters ?: 30
            when (val result = captureLocationUseCase(threshold)) {
                is LocationCaptureResult.Matched -> {
                    _uiState.update {
                        it.copy(
                            locationMatch = result.match,
                            isLocationLoading = false,
                            locationPermissionGranted = true,
                            // Clear manual selection if location is no longer accuracy-too-low
                            selectedManualLocation = if (result.match.status == MatchStatus.ACCURACY_TOO_LOW) {
                                it.selectedManualLocation
                            } else null,
                        )
                    }
                }
                LocationCaptureResult.NoPermission ->
                    _uiState.update { it.copy(isLocationLoading = false, locationPermissionGranted = false) }
                LocationCaptureResult.Timeout ->
                    _uiState.update { it.copy(isLocationLoading = false) }
                is LocationCaptureResult.NetworkError ->
                    _uiState.update { it.copy(isLocationLoading = false) }
            }
        }
    }

    fun onLocationPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(locationPermissionGranted = granted) }
        if (granted && _uiState.value.isAuthenticated) {
            viewModelScope.launch { captureLocation() }
        }
    }

    // Called by the screen whenever the live location-permission state may have changed
    // (on ON_RESUME and after a permission request). "Sufficient" requires BOTH precise
    // (fine) and background ("Allow all the time"). When it drops, automatic activities are
    // forced off & persisted (§ item 6); when it holds and auto is on, GPS is (re)captured.
    fun onLocationPermissionStateChanged(
        fineGranted: Boolean,
        backgroundGranted: Boolean,
        context: Context,
    ) {
        // P2: FINE location is the HARD requirement — without it the engine can't capture location,
        // so turn it off. Background ("Allow all the time") is only RECOMMENDED: losing it must NOT
        // disable the engine (only reduces background reliability). "Sufficient" mirrors the start
        // minimum (notifications + fine), read live so the UI/dialog reflect the real state.
        if (backgroundGranted) {
            AutoActivityController.clearBackgroundLocationWarning(context)
        }
        val status = PermissionLadder.checkStatus(context)
        val minimumOk = status.minimumToStartGranted
        val wasEnabled = _uiState.value.automaticActivitiesEnabled
        // P2 (additive, cosmetic): derive the gear-glow health from the SAME status (no second
        // checkStatus call). Off when auto is disabled; else Healthy if all recommended perms are
        // granted, otherwise Degraded. Purely presentational — never gates behavior.
        _uiState.update {
            it.copy(
                locationPermissionSufficient = minimumOk,
                autoActivitiesHealth = if (!wasEnabled) AutoActivitiesHealth.Off
                    else if (status.allRecommendedGranted) AutoActivitiesHealth.Healthy
                    else AutoActivitiesHealth.Degraded,
            )
        }
        if (!fineGranted) {
            if (wasEnabled) {
                onAutomaticActivitiesToggled(false, context)
                activityLogger.logWarning("Location permission revoked — auto disabled.") // plan004
            }
            _uiState.update { it.copy(locationMatch = null, isLocationLoading = false) }
            return
        }
        // Fine present: keep/ensure the engine running (e.g. background just got granted) and refresh
        // the foreground location. Both calls are idempotent. (This also gives an ON_RESUME auto-start
        // safety net, complementing P4.)
        ensureEngineRunningIfEligible(context)
        if (_uiState.value.automaticActivitiesEnabled && _uiState.value.isAuthenticated) {
            captureLocation()
        }
    }

    // §13.3 / §17.4 — the manual Refresh button: re-capture the location for the UI AND
    // run the automatic-activities engine (Situations 6–7). The orchestrator no-ops when
    // automatic activities are disabled, so this is safe in manual mode too.
    fun onRefreshLocation() {
        captureLocation()
        viewModelScope.launch { orchestrator.runOnce(OrchestratorTrigger.FOREGROUND) }
    }

    fun onManualLocationSelected(location: String?) {
        _uiState.update { it.copy(selectedManualLocation = location) }
    }

    private fun isCurrentAuthenticatedUser(chave: String): Boolean =
        _uiState.value.chave == chave && _uiState.value.isAuthenticated

    private fun isProjectMembershipStateCurrent(chave: String, expected: UserProjects): Boolean {
        if (!isCurrentAuthenticatedUser(chave) || projectMembershipCoordinatorChave != chave) return false
        return projectMembershipCoordinator?.state?.displayed == expected
    }

    private suspend fun loadAvailableLocations(
        chave: String,
        expectedMembership: UserProjects? = null,
    ) {
        when (val r = checkRepository.getLocations()) {
            is AppResult.Success -> {
                if (!isCurrentAuthenticatedUser(chave)) return
                if (expectedMembership != null &&
                    !isProjectMembershipStateCurrent(chave, expectedMembership)
                ) {
                    return
                }
                _uiState.update { it.copy(availableLocations = r.data.items) }
            }
            is AppResult.Failure ->
                if (_uiState.value.chave == chave && r.error is ApiError.Unauthorized) handleAuthExpiry()
        }
    }

    // ─── User projects ────────────────────────────────────────────────────────

    private suspend fun loadUserProjects(chave: String) {
        // The PUT worker owns the optimistic state until it reaches a stable server
        // response. A parallel GET could briefly revert freshly tapped checkboxes.
        if (_uiState.value.isProjectMembershipSyncing) return
        val generation = ++projectMembershipLoadGeneration
        if (!isCurrentAuthenticatedUser(chave)) return
        _uiState.update { it.copy(isProjectsLoading = true) }
        when (val r = projectRepository.getUserProjects()) {
            is AppResult.Success -> {
                if (!isCurrentAuthenticatedUser(chave) || generation != projectMembershipLoadGeneration) return
                replaceProjectMembershipCoordinator(chave, r.data)
                updateNoProjectNotification(r.data)
                reconcileProjectMembership(
                    chave = chave,
                    projects = r.data,
                    clearManualSelection = false,
                )
                if (!isCurrentAuthenticatedUser(chave) || generation != projectMembershipLoadGeneration) return
                _uiState.update { it.copy(isProjectsLoading = false) }
            }
            is AppResult.Failure -> {
                if (_uiState.value.chave != chave || generation != projectMembershipLoadGeneration) return
                if (r.error is ApiError.Unauthorized) {
                    handleAuthExpiry()
                } else {
                    val lang = _languageFlow.value
                    _uiState.update {
                        it.copy(
                            isProjectsLoading = false,
                            notificationPrimary = t("projects.userProjectsLoadFailed", lang = lang),
                            notificationSecondary = "",
                            notificationTone = NotificationTone.Error,
                        )
                    }
                }
            }
        }
    }

    fun onActiveProjectSelected(projectName: String) {
        val chave = _uiState.value.chave
        viewModelScope.launch {
            when (val r = projectRepository.updateActiveProject(projectName)) {
                is AppResult.Success -> {
                    if (!isCurrentAuthenticatedUser(chave)) return@launch
                    replaceProjectMembershipCoordinator(chave, r.data)
                    _uiState.update { it.copy(userProjects = r.data) }
                    reconcileProjectMembership(
                        chave = chave,
                        projects = r.data,
                        clearManualSelection = true,
                    )
                }
                is AppResult.Failure ->
                    if (r.error is ApiError.Unauthorized) handleAuthExpiry()
            }
        }
    }

    // Persists the user's project membership + active project into userSettingsJson so the background
    // engine (which reads the PERSISTED settings, not uiState) has a project to submit under. Without
    // this, automatic activities are NotConfigured and never fire. Overrides projects/activeProject
    // directly (preserving the other persisted flags) to avoid any normalization wiping them.
    private suspend fun persistUserProjects(chave: String, projects: List<String>, activeProject: String) {
        updatePersistedUserSettings(chave) {
            it.copy(projects = projects, activeProject = activeProject)
        }
    }

    private suspend fun updatePersistedUserSettings(
        chave: String,
        transform: (UserSettings) -> UserSettings,
    ) {
        if (chave.length != 4) return
        settingsWriteMutex.withLock {
            val rawJson = appPreferences.userSettingsJson.first()
            val currentMap: Map<String, UserSettings?> = runCatching {
                settingsJson.decodeFromString<Map<String, UserSettings?>>(rawJson)
            }.getOrElse { emptyMap() }
            val current = resolvePersistedUserSettings(currentMap, chave)
            val nextMap = withPersistedUserSettings(currentMap, chave, transform(current))
            appPreferences.setUserSettingsJson(settingsJson.encodeToString(nextMap))
        }
    }

    private suspend fun loadMainProjectCatalog(chave: String) {
        when (val r = projectRepository.listProjects()) {
            is AppResult.Success -> {
                if (!isCurrentAuthenticatedUser(chave)) return
                _uiState.update { it.copy(mainProjectCatalog = r.data) }
            }
            is AppResult.Failure ->
                if (_uiState.value.chave == chave && r.error is ApiError.Unauthorized) handleAuthExpiry()
        }
    }

    private suspend fun reconcileProjectMembership(
        chave: String,
        projects: UserProjects,
        clearManualSelection: Boolean,
    ): Boolean {
        val previousActiveProject = loadUserSettings(chave).activeProject
        persistUserProjects(chave, projects.projects, projects.activeProject)
        if (!isCurrentAuthenticatedUser(chave)) return false
        if (!isProjectMembershipStateCurrent(chave, projects)) return false

        if (previousActiveProject != projects.activeProject) {
            // Invalidate an in-flight result only for a real active-project transition.
            // Idempotent foreground GET reconciliation must preserve the episode deadline.
            orchestrator.cancelLowAccuracyRetry()
            orchestrator.resetScheduledPauseContext()
        }
        checkRepository.invalidateGeofenceCache()
        if (projects.projects.isEmpty() || projects.activeProject.isEmpty()) {
            _uiState.update {
                it.copy(
                    availableLocations = emptyList(),
                    selectedManualLocation = null,
                    locationMatch = null,
                    isLocationLoading = false,
                )
            }
            stopAutomaticActivityEngine(appContext)
            return isProjectMembershipStateCurrent(chave, projects)
        }

        if (clearManualSelection) {
            _uiState.update { it.copy(selectedManualLocation = null) }
        }
        loadAvailableLocations(chave, expectedMembership = projects)
        if (!isProjectMembershipStateCurrent(chave, projects)) return false

        if (_uiState.value.automaticActivitiesEnabled) {
            ensureEngineRunningIfEligible(appContext, refreshGeofences = true)
            orchestrator.runOnce(OrchestratorTrigger.FOREGROUND)
        }
        return isProjectMembershipStateCurrent(chave, projects)
    }

    private fun showNoProjectNotification() {
        val lang = _languageFlow.value
        _uiState.update {
            it.copy(
                notificationPrimary = t("projects.noProjectMembership", lang = lang),
                notificationSecondary = "",
                notificationTone = NotificationTone.Error,
            )
        }
    }

    private fun updateNoProjectNotification(
        projects: UserProjects,
    ) {
        if (projects.projects.isEmpty() || projects.activeProject.isEmpty()) {
            showNoProjectNotification()
            return
        }
        val noProjectMessages = SUPPORTED_LANGUAGES
            .map { t("projects.noProjectMembership", lang = it.code) }
            .toSet()
        _uiState.update {
            if (it.notificationPrimary in noProjectMessages) {
                it.copy(
                    notificationPrimary = "",
                    notificationSecondary = "",
                    notificationTone = NotificationTone.None,
                )
            } else {
                it
            }
        }
    }

    private fun replaceProjectMembershipCoordinator(chave: String, confirmed: UserProjects) {
        val current = projectMembershipCoordinator
        if (current != null && projectMembershipCoordinatorChave == chave) {
            current.reset(confirmed)
            return
        }

        resetProjectMembershipCoordinator(invalidateLoads = false)
        projectMembershipCoordinatorChave = chave
        lateinit var coordinator: UserProjectMembershipCoordinator
        coordinator = UserProjectMembershipCoordinator(
            scope = viewModelScope,
            initialConfirmed = confirmed,
            update = { projectNames ->
                projectRepository.updateUserProjects(projectNames)
            },
            onStateChanged = { syncState ->
                if (isCurrentAuthenticatedUser(chave) && projectMembershipCoordinatorChave == chave) {
                    _uiState.update {
                        it.copy(
                            userProjects = syncState.displayed,
                            isProjectMembershipSyncing = syncState.syncing,
                        )
                    }
                }
            },
            onCommitted = commit@{ authoritative ->
                if (!isCurrentAuthenticatedUser(chave) ||
                    projectMembershipCoordinatorChave != chave ||
                    projectMembershipCoordinator !== coordinator
                ) {
                    return@commit
                }
                val committed = reconcileProjectMembership(
                    chave = chave,
                    projects = authoritative,
                    clearManualSelection = true,
                )
                if (!committed) return@commit
                if (authoritative.projects.isEmpty() || authoritative.activeProject.isEmpty()) {
                    showNoProjectNotification()
                } else {
                    val lang = _languageFlow.value
                    _uiState.update {
                        it.copy(
                            notificationPrimary = t("projects.updatedSuccess", lang = lang),
                            notificationSecondary = "",
                            notificationTone = NotificationTone.Success,
                        )
                    }
                }
            },
            onError = failure@{ error ->
                if (!isCurrentAuthenticatedUser(chave) ||
                    projectMembershipCoordinatorChave != chave ||
                    projectMembershipCoordinator !== coordinator
                ) {
                    return@failure
                }
                if (error is ApiError.Unauthorized) {
                    handleAuthExpiry()
                } else {
                    val lang = _languageFlow.value
                    if (coordinator.state.displayed.projects.isEmpty()) {
                        _uiState.update {
                            it.copy(
                                notificationPrimary = t("projects.noProjectMembership", lang = lang),
                                notificationSecondary = t("projects.updateFailed", lang = lang),
                                notificationTone = NotificationTone.Error,
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                notificationPrimary = t("projects.updateFailed", lang = lang),
                                notificationSecondary = "",
                                notificationTone = NotificationTone.Error,
                            )
                        }
                    }
                }
            },
        )
        projectMembershipCoordinator = coordinator
    }

    private fun resetProjectMembershipCoordinator(invalidateLoads: Boolean = true) {
        if (invalidateLoads) projectMembershipLoadGeneration += 1
        projectMembershipCoordinator?.reset(UserProjects(emptyList(), ""))
        projectMembershipCoordinator = null
        projectMembershipCoordinatorChave = ""
    }

    // Multi-select project membership (web: projectMembershipOptions checkboxes).
    // Toggles are optimistic and serialized; removing the final item is valid.
    fun onProjectMembershipToggled(projectName: String) {
        val state = _uiState.value
        if (!state.isAuthenticated) return
        val currentProjects = state.userProjects ?: return
        val chave = state.chave

        if (projectMembershipCoordinator == null || projectMembershipCoordinatorChave != chave) {
            replaceProjectMembershipCoordinator(chave, currentProjects)
        }
        projectMembershipLoadGeneration += 1
        _uiState.update { it.copy(isProjectsLoading = false) }
        projectMembershipCoordinator?.toggle(projectName)
    }

    // ─── Dialogs ──────────────────────────────────────────────────────────────

    fun openSettings() {
        _uiState.update { it.copy(dialogOpen = CheckDialog.Settings) }
    }

    // P2.2 (change D) — open the history dialog filtered to the tapped action. Read-only; requires a chave.
    fun openCheckinHistory() = openCheckHistoryDialog(CheckAction.CHECKIN)
    fun openCheckoutHistory() = openCheckHistoryDialog(CheckAction.CHECKOUT)

    private fun openCheckHistoryDialog(action: CheckAction) {
        _uiState.update {
            it.copy(
                dialogOpen = CheckDialog.History,
                historyDialogAction = action,
                historyDialogEntries = emptyList(),
                isHistoryDialogLoading = true,
                historyDialogError = false,
            )
        }
        loadHistoryDialog(action)
    }

    // plan004 EP2 — re-load the current action after a failure (the dialog's "retry"), without
    // re-opening it. Distinct error state means a load failure is never silently shown as "empty".
    fun retryHistoryDialog() {
        val action = _uiState.value.historyDialogAction ?: return
        _uiState.update {
            it.copy(
                historyDialogEntries = emptyList(),
                isHistoryDialogLoading = true,
                historyDialogError = false,
            )
        }
        loadHistoryDialog(action)
    }

    private fun loadHistoryDialog(action: CheckAction) {
        val chave = _uiState.value.chave
        viewModelScope.launch {
            when (val r = checkRepository.getHistory(chave)) {
                is AppResult.Success ->
                    _uiState.update {
                        it.copy(
                            historyDialogEntries = r.data.filter { entry -> entry.action == action },
                            isHistoryDialogLoading = false,
                        )
                    }
                is AppResult.Failure ->
                    _uiState.update { it.copy(isHistoryDialogLoading = false, historyDialogError = true) }
            }
        }
    }

    fun openPasswordChangeDialog() {
        _uiState.update { it.copy(dialogOpen = CheckDialog.PasswordChange) }
    }

    fun openSelfRegistrationDialog() {
        val chave = _uiState.value.chave
        _uiState.update {
            it.copy(
                dialogOpen = CheckDialog.SelfRegistration,
                dismissedAssistanceForChave = "",
                selfRegistrationFields = it.selfRegistrationFields.copy(chave = chave),
            )
        }
    }

    fun dismissDialog() {
        val state = _uiState.value
        val chave = state.chave
        // Track dismissal so the dialog doesn't auto-reopen for this chave
        if (state.dialogOpen == CheckDialog.PasswordChange || state.dialogOpen == CheckDialog.SelfRegistration) {
            _uiState.update { it.copy(dialogOpen = null, dismissedAssistanceForChave = chave) }
        } else {
            _uiState.update { it.copy(dialogOpen = null) }
        }
    }

    // ─── Password change dialog ───────────────────────────────────────────────

    fun onPasswordChangeOldPwChanged(v: String) =
        _uiState.update { it.copy(passwordChangeFields = it.passwordChangeFields.copy(oldPw = v)) }

    fun onPasswordChangeNewPwChanged(v: String) =
        _uiState.update { it.copy(passwordChangeFields = it.passwordChangeFields.copy(newPw = v)) }

    fun onPasswordChangeConfirmPwChanged(v: String) =
        _uiState.update { it.copy(passwordChangeFields = it.passwordChangeFields.copy(confirmPw = v)) }

    fun submitPasswordChange() {
        val state = _uiState.value
        val fields = state.passwordChangeFields
        val lang = _languageFlow.value
        val hasExistingPassword = state.hasPassword

        // Validate
        if (hasExistingPassword && !isPasswordLengthValid(fields.oldPw)) {
            _uiState.update {
                it.copy(passwordChangeFields = fields.copy(errorMessage = t("passwordDialog.oldPasswordInvalid", lang = lang)))
            }
            return
        }
        if (!isPasswordLengthValid(fields.newPw)) {
            _uiState.update {
                it.copy(passwordChangeFields = fields.copy(errorMessage = t("passwordDialog.newPasswordInvalid", lang = lang)))
            }
            return
        }
        if (fields.newPw != fields.confirmPw) {
            _uiState.update {
                it.copy(passwordChangeFields = fields.copy(errorMessage = t("passwordDialog.confirmMismatch", lang = lang)))
            }
            return
        }

        _uiState.update { it.copy(passwordChangeFields = fields.copy(isBusy = true, errorMessage = "")) }

        viewModelScope.launch {
            val result = if (hasExistingPassword) {
                authRepository.changePassword(state.chave, fields.oldPw, fields.newPw)
            } else {
                authRepository.registerPassword(state.chave, null, fields.newPw)
            }
            when (result) {
                is AppResult.Success -> {
                    val status = result.data
                    securePasswordStore.setPassword(state.chave, fields.newPw)
                    _uiState.update {
                        it.copy(
                            authStatus = status,
                            prompt = resolvePrompt(status),
                            dialogOpen = null,
                            dismissedAssistanceForChave = state.chave,
                            passwordChangeFields = PasswordChangeFields(),
                        )
                    }
                    if (status.authenticated) onAuthenticationSucceeded(state.chave, status)
                }
                is AppResult.Failure -> {
                    val msg = when (result.error) {
                        is ApiError.Unauthorized -> t("passwordDialog.changeFailed", lang = lang)
                        else -> t("passwordDialog.changeFailed", lang = lang)
                    }
                    _uiState.update {
                        it.copy(passwordChangeFields = fields.copy(isBusy = false, errorMessage = msg))
                    }
                }
            }
        }
    }

    // ─── Self-registration dialog ─────────────────────────────────────────────

    fun loadProjectCatalogForRegistration() {
        if (_uiState.value.selfRegistrationFields.projectCatalog.isNotEmpty()) return
        _uiState.update {
            it.copy(selfRegistrationFields = it.selfRegistrationFields.copy(isLoadingProjects = true))
        }
        viewModelScope.launch {
            when (val r = projectRepository.listProjects()) {
                is AppResult.Success ->
                    _uiState.update {
                        it.copy(
                            selfRegistrationFields = it.selfRegistrationFields.copy(
                                projectCatalog = r.data,
                                isLoadingProjects = false,
                            )
                        )
                    }
                is AppResult.Failure ->
                    _uiState.update {
                        it.copy(selfRegistrationFields = it.selfRegistrationFields.copy(isLoadingProjects = false))
                    }
            }
        }
    }

    fun onRegChaveChanged(v: String) {
        // plan003 (decision 3) — editable registration key (mirrors Check Web). Keep the main-screen chave
        // in sync so submit, pending-approval polling and auto-login all act on the corrected key. No
        // re-probe here — that path (onChaveChanged) would close this dialog; the server re-evaluates the
        // key on submit anyway.
        val sanitized = sanitizeSettingsChave(v)
        _uiState.update {
            it.copy(
                chave = sanitized,
                selfRegistrationFields = it.selfRegistrationFields.copy(chave = sanitized),
            )
        }
    }

    fun onRegNomeChanged(v: String) =
        _uiState.update { it.copy(selfRegistrationFields = it.selfRegistrationFields.copy(nome = v)) }

    fun onRegEmailChanged(v: String) =
        _uiState.update { it.copy(selfRegistrationFields = it.selfRegistrationFields.copy(email = autofillPetrobrasEmailDomain(v))) }

    fun onRegPasswordChanged(v: String) =
        _uiState.update { it.copy(selfRegistrationFields = it.selfRegistrationFields.copy(password = v)) }

    fun onRegConfirmPwChanged(v: String) =
        _uiState.update { it.copy(selfRegistrationFields = it.selfRegistrationFields.copy(confirmPw = v)) }

    fun onRegProjectToggled(projectId: Int) {
        val current = _uiState.value.selfRegistrationFields.selectedProjectIds.toMutableList()
        if (current.contains(projectId)) current.remove(projectId) else current.add(projectId)
        _uiState.update { it.copy(selfRegistrationFields = it.selfRegistrationFields.copy(selectedProjectIds = current)) }
    }

    fun submitSelfRegistration() {
        val state = _uiState.value
        val fields = state.selfRegistrationFields
        val lang = _languageFlow.value

        if (fields.nome.isBlank()) {
            _uiState.update {
                it.copy(selfRegistrationFields = fields.copy(errorMessage = t("registrationDialog.fullNameRequired", lang = lang)))
            }
            return
        }
        val email = fields.email.trim()
        if (email.isNotEmpty() && !email.contains('@')) {
            _uiState.update {
                it.copy(selfRegistrationFields = fields.copy(errorMessage = t("registrationDialog.emailInvalid", lang = lang)))
            }
            return
        }
        if (!isPasswordLengthValid(fields.password)) {
            _uiState.update {
                it.copy(selfRegistrationFields = fields.copy(errorMessage = t("registrationDialog.passwordInvalid", lang = lang)))
            }
            return
        }
        if (fields.password != fields.confirmPw) {
            _uiState.update {
                it.copy(selfRegistrationFields = fields.copy(errorMessage = t("registrationDialog.confirmMismatch", lang = lang)))
            }
            return
        }
        val selectedProjectNames = fields.projectCatalog
            .filter { fields.selectedProjectIds.contains(it.id) }
            .map { it.name }
        if (selectedProjectNames.isEmpty()) {
            _uiState.update {
                it.copy(selfRegistrationFields = fields.copy(errorMessage = t("projects.selectAtLeastOne", lang = lang)))
            }
            return
        }

        _uiState.update { it.copy(selfRegistrationFields = fields.copy(isBusy = true, errorMessage = "")) }

        viewModelScope.launch {
            when (val result = authRepository.selfRegister(
                chave = state.chave,
                nome = fields.nome.trim(),
                projetos = selectedProjectNames,
                email = email.ifEmpty { null },
                password = fields.password,
                confirmPassword = fields.confirmPw,
            )) {
                is AppResult.Success -> {
                    val status = result.data
                    // Always keep the typed password locally — needed for the auto-login once approved.
                    securePasswordStore.setPassword(state.chave, fields.password)
                    when {
                        status.queueFull -> {
                            // plan003 — pending queue is full: red bar, NOT authenticated, NOT awaiting.
                            _uiState.update {
                                it.copy(
                                    authStatus = status,
                                    prompt = "",
                                    dialogOpen = null,
                                    dismissedAssistanceForChave = state.chave,
                                    selfRegistrationFields = SelfRegistrationFields(chave = state.chave),
                                    notificationPrimary = t("auth.registrationQueueFull", lang = lang),
                                    notificationSecondary = "",
                                    notificationTone = NotificationTone.Error,
                                )
                            }
                        }
                        status.pendingApproval -> {
                            // plan003 — awaiting admin approval: orange fields + red bar; start polling.
                            _uiState.update {
                                it.copy(
                                    authStatus = status,
                                    prompt = "",
                                    dialogOpen = null,
                                    dismissedAssistanceForChave = state.chave,
                                    selfRegistrationFields = SelfRegistrationFields(chave = state.chave),
                                    notificationPrimary = t("auth.awaitingApproval", lang = lang),
                                    notificationSecondary = "",
                                    notificationTone = NotificationTone.Error,
                                )
                            }
                            startPendingApprovalPolling(state.chave)
                        }
                        else -> {
                            // "registered" (gate off): legacy authenticated path.
                            _uiState.update {
                                it.copy(
                                    authStatus = status,
                                    prompt = resolvePrompt(status),
                                    dialogOpen = null,
                                    dismissedAssistanceForChave = state.chave,
                                    selfRegistrationFields = SelfRegistrationFields(chave = state.chave),
                                )
                            }
                            if (status.authenticated) onAuthenticationSucceeded(state.chave, status)
                        }
                    }
                }
                is AppResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            selfRegistrationFields = fields.copy(
                                isBusy = false,
                                errorMessage = t("registrationDialog.submitFailed", lang = lang),
                            )
                        )
                    }
                }
            }
        }
    }

    // ─── Registration fieldset ────────────────────────────────────────────────

    fun onActionSelected(action: br.com.tscode.checking.domain.model.CheckAction) {
        _uiState.update { it.copy(selectedAction = action) }
    }

    fun onInformeSelected(informe: UiInformeType) {
        _uiState.update { it.copy(selectedInforme = informe) }
    }

    fun onSubmit() {
        val state = _uiState.value
        val lang = _languageFlow.value
        val userProjects = state.userProjects
        val projeto = userProjects?.activeProject
            ?.takeIf { it.isNotEmpty() && it in userProjects.projects }
        if (projeto == null) {
            showNoProjectNotification()
            return
        }
        if (state.isProjectsLoading || state.isProjectMembershipSyncing) {
            _uiState.update {
                it.copy(
                    notificationPrimary = t("projects.updatingProjects", lang = lang),
                    notificationSecondary = "",
                    notificationTone = NotificationTone.Info,
                )
            }
            return
        }
        if (!state.canSubmit) return

        // §13.6 step 2 — manual submission is only allowed when automatic activities are
        // off, or while the low-accuracy fallback is active (Situation 9). Otherwise block.
        if (state.automaticActivitiesEnabled && !state.isAccuracyTooLow) {
            _uiState.update {
                it.copy(
                    notificationPrimary = t("registration.disableAutomaticActivitiesForManualSubmit", lang = lang),
                    notificationTone = NotificationTone.Error,
                )
            }
            return
        }

        // Manual mode (automatic off) or Situation 9 (accuracy too low) → use the
        // location picked from the "Local" dropdown. Otherwise use the GPS-matched local.
        val local = if (state.requiresManualLocation) {
            state.selectedManualLocation
                ?: if (state.selectedAction == br.com.tscode.checking.domain.model.CheckAction.CHECKOUT) {
                    // Manual check-out without a selected location → record "Desconhecido"
                    // so the admin table still gets a value (the server accepts it).
                    "Desconhecido"
                } else {
                    // Manual check-in still requires a location.
                    _uiState.update {
                        it.copy(
                            notificationPrimary = t("location.selectManualLocation", lang = lang),
                            notificationTone = NotificationTone.Error,
                        )
                    }
                    return
                }
        } else {
            state.locationMatch?.resolvedLocal
        }

        val informe = when (state.selectedInforme) {
            UiInformeType.NORMAL -> InformeType.NORMAL
            UiInformeType.RETROATIVO -> InformeType.RETROATIVO
        }

        _uiState.update { it.copy(isSubmitting = true) }

        // Pre-generate id + timestamp so an offline submit can be queued with the SAME identity
        // (exactly-once: the replay dedups by client_event_id even if the submit reached the server).
        val clientEventId = UUID.randomUUID().toString()
        val eventTime = clock.now()
        val wasLowAccuracyFallback =
            state.automaticActivitiesEnabled && state.isAccuracyTooLow
        viewModelScope.launch {
            when (val r = checkRepository.submit(
                chave = state.chave,
                projeto = projeto,
                action = state.selectedAction,
                local = local,
                informe = informe,
                eventTime = eventTime,
                clientEventId = clientEventId,
            )) {
                is AppResult.Success -> {
                    if (!isCurrentAuthenticatedUser(state.chave)) return@launch
                    if (wasLowAccuracyFallback) {
                        orchestrator.cancelLowAccuracyRetry()
                    }
                    val newState = r.data
                    // Server-confirmed state (not the submitted payload) reconciles a deferred
                    // scheduled pause. Offline enqueue deliberately has no equivalent callback.
                    orchestrator.onServerConfirmedState(
                        chave = state.chave,
                        project = projeto,
                        newState = newState,
                    )
                    val actionKey = when (state.selectedAction) {
                        br.com.tscode.checking.domain.model.CheckAction.CHECKIN -> "status.checkinCompleted"
                        br.com.tscode.checking.domain.model.CheckAction.CHECKOUT -> "status.checkoutCompleted"
                    }
                    _uiState.update {
                        it.copy(
                            historyState = newState,
                            isSubmitting = false,
                            notificationPrimary = t(actionKey, lang = lang),
                            notificationTone = NotificationTone.Success,
                            // Keep the "Local" dropdown on the just-recorded activity's location.
                            selectedManualLocation = newState.currentLocal,
                        )
                    }
                    // plan004 — manual check-in/out succeeded (actor=USER). Side-effect-only log.
                    if (state.selectedAction == CheckAction.CHECKIN) {
                        activityLogger.logCheckIn(ActivityActor.USER, local, success = true)
                    } else {
                        activityLogger.logCheckOut(ActivityActor.USER, local, success = true)
                    }
                    // Refresh accurate history state in background (authoritative source
                    // for the transport-enabled flag, which the submit response omits).
                    viewModelScope.launch {
                        when (val refreshed = checkRepository.getState(state.chave)) {
                            is AppResult.Success -> {
                                if (!isCurrentAuthenticatedUser(state.chave)) return@launch
                                _uiState.update {
                                    it.copy(
                                        historyState = refreshed.data,
                                        transportEnabled = refreshed.data.transportEnabled,
                                        selectedManualLocation = it.selectedManualLocation ?: refreshed.data.currentLocal,
                                    )
                                }
                            }
                            is AppResult.Failure -> Unit
                        }
                    }
                }
                is AppResult.Failure -> {
                    val err = r.error
                    if (err is ApiError.Unauthorized) {
                        if (_uiState.value.chave == state.chave) handleAuthExpiry()
                        activityLogger.logError("Session expired — sign in again.")
                    } else if (err is ApiError.Network) {
                        // Offline / server unreachable → queue for sync on reconnect (P8) with the
                        // SAME id so it lands exactly once. It's saved, not failed — tell the user so.
                        offlineCheckQueue.enqueue(
                            PendingCheckEvent.Decided(
                                chave = state.chave,
                                projeto = projeto,
                                capturedAtEpochMs = eventTime.toEpochMilli(),
                                clientEventId = clientEventId,
                                action = if (state.selectedAction ==
                                    br.com.tscode.checking.domain.model.CheckAction.CHECKOUT
                                ) {
                                    "checkout"
                                } else {
                                    "checkin"
                                },
                                local = local,
                                informe = if (informe == InformeType.RETROATIVO) "retroativo" else "normal",
                            ),
                        )
                        if (_uiState.value.chave != state.chave) return@launch
                        if (wasLowAccuracyFallback) {
                            // Once queued, replay owns this exact manual event. Stop the automatic
                            // retry episode only after enqueue succeeds to avoid a distinct event
                            // being generated when connectivity returns.
                            orchestrator.cancelLowAccuracyRetry()
                        }
                        _uiState.update {
                            it.copy(
                                isSubmitting = false,
                                notificationPrimary = t("status.savedOffline", lang = lang),
                                notificationTone = NotificationTone.Success,
                            )
                        }
                        // plan004 — manual submit queued offline (actor=USER).
                        activityLogger.logQueuedOffline(
                            ActivityActor.USER,
                            if (state.selectedAction == CheckAction.CHECKIN) ActivityKind.CHECK_IN else ActivityKind.CHECK_OUT,
                            local,
                        )
                    } else if (err is ApiError.Conflict) {
                        loadUserProjects(state.chave)
                        if (!isCurrentAuthenticatedUser(state.chave)) return@launch
                        _uiState.update { it.copy(isSubmitting = false) }
                        if (_uiState.value.userProjects?.projects.isNullOrEmpty()) {
                            showNoProjectNotification()
                        } else {
                            _uiState.update {
                                it.copy(
                                    notificationPrimary = t("status.submitFailed", lang = lang),
                                    notificationSecondary = "",
                                    notificationTone = NotificationTone.Error,
                                )
                            }
                        }
                        if (state.selectedAction == CheckAction.CHECKIN) {
                            activityLogger.logCheckIn(ActivityActor.USER, local, success = false)
                        } else {
                            activityLogger.logCheckOut(ActivityActor.USER, local, success = false)
                        }
                    } else {
                        if (_uiState.value.chave != state.chave) return@launch
                        val msg = (err as? ApiError.Http)?.detail
                            ?.let { KnownApiMessages.localizeApiMessage(it, lang) }
                            ?: t("status.submitFailed", lang = lang)
                        _uiState.update {
                            it.copy(
                                isSubmitting = false,
                                notificationPrimary = msg,
                                notificationTone = NotificationTone.Error,
                            )
                        }
                        // plan004 — manual check-in/out failed (non-network; actor=USER).
                        if (state.selectedAction == CheckAction.CHECKIN) {
                            activityLogger.logCheckIn(ActivityActor.USER, local, success = false)
                        } else {
                            activityLogger.logCheckOut(ActivityActor.USER, local, success = false)
                        }
                    }
                }
            }
        }
    }

    // ─── Automatic Activities ─────────────────────────────────────────────────

    fun openAutoActivitiesDialog() {
        // P5: opening the dialog (incl. the nudge's "Ativar agora") hides the transient nudge card.
        _uiState.update { it.copy(dialogOpen = CheckDialog.AutoActivities, showAutoActivitiesNudge = false) }
    }

    // LGPD art. 8º §2 (ônus da prova) — persists WHEN the user affirmatively accepted the background-location
    // + international-transfer prominent disclosure, as evidence of specific, informed consent.
    fun recordBackgroundLocationConsent() {
        viewModelScope.launch {
            appPreferences.setBackgroundLocationConsentAt(clock.now().toString())
            activityLogger.logSystem("Background location consent granted by user.") // plan004
        }
    }

    // LGPD art. 18 — "Remover Cadastro": delete the account on the server, then (only if the server
    // actually removed it) wipe ALL local data and reset to the auth prompt. On a server refusal (409 —
    // admin / accident opener / user in an open accident) the account still exists, so we keep the local
    // session and surface a message pointing to the privacy channel instead of faking success.
    fun deleteAccount() {
        viewModelScope.launch {
            val lang = _languageFlow.value
            when (val result = authRepository.deleteAccount()) {
                is AppResult.Success -> {
                    orchestrator.cancelLowAccuracyRetry()
                    // clearAll removes DataStore state but cannot cancel in-process delay Jobs or
                    // AlarmManager PendingIntents. Tear those down first, under the orchestrator lock.
                    orchestrator.resetScheduledPauseContext()
                    runCatching { AutoActivityController.stop(appContext) }
                    runCatching { activityLog.clear() }
                    runCatching { securePasswordStore.clearAll() }
                    runCatching { appPreferences.clearAll() }
                    stopCheckStream()
                    stopPendingApprovalPolling()
                    _uiState.value = CheckUiState(isInitializing = false) // back to the chave-entry prompt
                }
                is AppResult.Failure -> {
                    // 409 (blocked) maps to ApiError.Conflict — direct the user to the privacy channel.
                    val message = if (result.error is ApiError.Conflict) {
                        t("settings.deleteAccountBlocked", lang = lang)
                    } else {
                        t("settings.deleteAccountFailed", lang = lang)
                    }
                    _uiState.update {
                        it.copy(notificationPrimary = message, notificationTone = NotificationTone.Error)
                    }
                }
            }
        }
    }

    // P5: per-chave persisted dismissal flag for the first-login nudge (generic flag API — no schema change).
    private fun nudgeFlag(chave: String) = "auto_activities_prompt_dismissed_$chave"

    // P5: "Agora não" — dismiss the nudge forever for this chave and hide it now.
    fun dismissAutoActivitiesNudge() {
        val chave = _uiState.value.chave
        viewModelScope.launch { appPreferences.setFlag(nudgeFlag(chave), true) }
        _uiState.update { it.copy(showAutoActivitiesNudge = false) }
    }

    fun openScheduledPauseDialog() {
        _uiState.update { it.copy(dialogOpen = CheckDialog.ScheduledPause) }
    }

    fun openNotificationsDialog() {
        _uiState.update { it.copy(dialogOpen = CheckDialog.Notifications) }
    }

    // Called from NotificationsDialog when the user changes a checkbox (saved on each change
    // so the "Voltar" button simply closes the dialog with everything already persisted).
    fun onNotificationSettingsChanged(
        notifyActivities: Boolean,
        notifyScheduledPause: Boolean,
        notifyAccident: Boolean,
    ) {
        _uiState.update {
            it.copy(
                notifyActivities = notifyActivities,
                notifyScheduledPause = notifyScheduledPause,
                notifyAccident = notifyAccident,
            )
        }
        viewModelScope.launch {
            persistNotificationSettings(
                chave = _uiState.value.chave,
                notifyActivities = notifyActivities,
                notifyScheduledPause = notifyScheduledPause,
                notifyAccident = notifyAccident,
            )
        }
    }

    private suspend fun persistNotificationSettings(
        chave: String,
        notifyActivities: Boolean,
        notifyScheduledPause: Boolean,
        notifyAccident: Boolean,
    ) {
        updatePersistedUserSettings(chave) {
            it.copy(
                notifyActivities = notifyActivities,
                notifyScheduledPause = notifyScheduledPause,
                notifyAccident = notifyAccident,
            )
        }
    }

    fun openEvaluationLogDialog() {
        _uiState.update { it.copy(dialogOpen = CheckDialog.EvaluationLog) }
    }

    // plan004 EP8 — Activities log (read-only, snapshot-at-open, paged in blocks of 30, newest-first).
    // Every store read is runCatching-wrapped so a DB hiccup can never break the dialog (golden rule 2).
    fun openActivitiesDialog() {
        _uiState.update {
            it.copy(
                dialogOpen = CheckDialog.Activities,
                activityEntries = emptyList(),
                activityNextOffset = 0,
                activityCanLoadMore = false,
                isActivitiesLoading = true,
            )
        }
        viewModelScope.launch {
            val page = runCatching { activityLog.page(offset = 0, limit = ActivityLog.PAGE_SIZE) }
                .getOrElse { emptyList() }
            _uiState.update {
                it.copy(
                    activityEntries = page,
                    activityNextOffset = page.size,
                    activityCanLoadMore = page.size == ActivityLog.PAGE_SIZE,
                    isActivitiesLoading = false,
                )
            }
        }
    }

    fun loadMoreActivities() {
        val s = _uiState.value
        if (s.isActivitiesLoading || !s.activityCanLoadMore) return // guard re-entrancy + end-of-list
        _uiState.update { it.copy(isActivitiesLoading = true) }
        viewModelScope.launch {
            val offset = _uiState.value.activityNextOffset
            val page = runCatching { activityLog.page(offset = offset, limit = ActivityLog.PAGE_SIZE) }
                .getOrElse { emptyList() }
            _uiState.update {
                it.copy(
                    activityEntries = it.activityEntries + page,
                    activityNextOffset = it.activityNextOffset + page.size,
                    activityCanLoadMore = page.size == ActivityLog.PAGE_SIZE,
                    isActivitiesLoading = false,
                )
            }
        }
    }

    fun clearActivities() {
        viewModelScope.launch {
            runCatching { activityLog.clear() }
            _uiState.update {
                it.copy(
                    activityEntries = emptyList(),
                    activityNextOffset = 0,
                    activityCanLoadMore = false,
                    isActivitiesLoading = false,
                )
            }
        }
    }

    fun onScheduledPauseSettingChanged(
        enabled: Boolean,
        from: String,
        to: String,
        suspendSat: Boolean,
        suspendSun: Boolean,
    ) {
        _uiState.update {
            it.copy(
                scheduledPauseEnabled = enabled,
                scheduledPauseFrom = from,
                scheduledPauseTo = to,
                suspendSaturdays = suspendSat,
                suspendSundays = suspendSun,
            )
        }
        val chave = _uiState.value.chave
        // Date/time pickers can emit several values quickly. The latest job always persists last
        // and is the only one allowed to reconcile the runtime pause occurrence.
        scheduledPauseSettingsJob?.cancel()
        scheduledPauseSettingsJob = viewModelScope.launch {
            persistScheduledPauseSettings(
                chave = chave,
                enabled = enabled,
                from = from,
                to = to,
                suspendSat = suspendSat,
                suspendSun = suspendSun,
            )
            val latest = _uiState.value
            if (latest.chave != chave ||
                latest.scheduledPauseEnabled != enabled ||
                latest.scheduledPauseFrom != from ||
                latest.scheduledPauseTo != to ||
                latest.suspendSaturdays != suspendSat ||
                latest.suspendSundays != suspendSun
            ) {
                return@launch
            }
            orchestrator.onScheduledPauseConfigurationChanged(chave)
        }
    }

    // Single, idempotent entry point that starts the background engine (FGS + watchdog) IFF it
    // should be running and isn't yet. "Eligible" = authenticated chave + automaticActivitiesEnabled
    // (the persisted intent, mirrored in uiState) + permissions sufficient. Permission sufficiency is
    // read LIVE (PermissionLadder.checkStatus) to avoid a race with the cached uiState flag right
    // after the permission ladder completes. Safe to call from the toggle, the ladder-granted
    // callback, and (P4) the launch/auth and ON_RESUME paths.
    // NOTE (P2): "sufficient" here still means fine + background; P2 relaxes the start threshold to the
    // minimum (fine only) and makes background/battery advisory.
    private fun ensureEngineRunningIfEligible(
        context: Context,
        refreshGeofences: Boolean = false,
    ) {
        val state = _uiState.value
        if (state.chave.length != 4) return
        if (!state.automaticActivitiesEnabled) return
        if (state.isProjectMembershipSyncing && !refreshGeofences) return
        if (state.userProjects?.activeProject?.isNotEmpty() != true) return
        val status = PermissionLadder.checkStatus(context)
        // P2: the MINIMUM to start is notifications (API 33+) + precise location. Background ("Allow
        // all the time") and battery exemption are RECOMMENDED, not required to start.
        val minimumOk = status.minimumToStartGranted
        _uiState.update { it.copy(locationPermissionSufficient = minimumOk) }
        if (!minimumOk) return
        if (!AutoActivityController.isRunning() || refreshGeofences) {
            AutoActivityController.start(
                context,
                AutoActivityServiceStartOrigin.USER_VISIBLE,
            )
        }
    }

    private fun stopAutomaticActivityEngine(context: Context) {
        // Also retire any in-process low-accuracy retry immediately. Stopping the Android service
        // alone does not cancel an application-scope coroutine.
        orchestrator.cancelLowAccuracyRetry()
        orchestrator.invalidateScheduledPauseContext()
        // WorkManager is not available in local JVM tests and Android may reject a
        // background service command during lifecycle transitions. Stopping is best effort;
        // the persisted membership/boot gates still prevent automatic submissions.
        runCatching { AutoActivityController.stop(context) }
    }

    // Runs ONE normal (situation-engine) evaluation the instant automatic activities are enabled, so the
    // routine acts NOW instead of waiting for the FGS's first 15-min timer tick or a geofence ENTER
    // transition — the latter never fires when the user is already standing inside a location, which is
    // exactly why a manual activity used to be needed first. Two reasons this is needed even though the
    // FGS runs an evaluation on (fresh) start:
    //   1) if the service was already running (re-enable / watchdog / boot) starting it is a no-op, so no
    //      immediate evaluation would otherwise happen until the next 15-min tick;
    //   2) the orchestrator writes only to the server + its own cache, never to uiState — so we refresh
    //      the history here so any recorded check-in/out shows on screen immediately.
    // FOREGROUND trigger bypasses the skip-if-unchanged gate; the situation engine still decides, so this
    // records an activity only when one is actually warranted (no forced/spurious event, no duplicate —
    // a same-location re-check-in is suppressed). Single-flight with the FGS via the orchestrator mutex.
    private fun runImmediateAutoEvaluation() {
        val state = _uiState.value
        val chave = state.chave
        if (chave.length != 4) return
        if (state.isProjectMembershipSyncing || state.userProjects?.activeProject?.isEmpty() != false) return
        viewModelScope.launch {
            orchestrator.runOnce(OrchestratorTrigger.FOREGROUND)
            when (val refreshed = checkRepository.getState(chave)) {
                is AppResult.Success -> {
                    if (!isCurrentAuthenticatedUser(chave)) return@launch
                    _uiState.update {
                        it.copy(
                            historyState = refreshed.data,
                            transportEnabled = refreshed.data.transportEnabled,
                            selectedManualLocation = it.selectedManualLocation ?: refreshed.data.currentLocal,
                        )
                    }
                }
                is AppResult.Failure -> Unit
            }
        }
    }

    // Called when the user toggles the "Habilitar Atividades Automáticas" checkbox.
    // Persists the intent. On disable → stop the FGS. On enable → start NOW if permissions already
    // suffice; otherwise the dialog launches the permission ladder and onAutoActivitiesPermissionsGranted()
    // starts the FGS once granted. (The old code never started the FGS on enable — that ordering bug
    // left the engine off in the field.)
    fun onAutomaticActivitiesToggled(enabled: Boolean, context: Context) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val chave = currentState.chave
            if (enabled && (currentState.isProjectsLoading || currentState.isProjectMembershipSyncing)) {
                val lang = _languageFlow.value
                _uiState.update {
                    it.copy(
                        automaticActivitiesEnabled = false,
                        autoActivitiesHealth = AutoActivitiesHealth.Off,
                        notificationPrimary = t("projects.updatingProjects", lang = lang),
                        notificationSecondary = "",
                        notificationTone = NotificationTone.Info,
                    )
                }
                persistAutoActivitiesEnabled(chave, false)
                return@launch
            }
            if (enabled &&
                (currentState.userProjects?.projects.isNullOrEmpty() ||
                    currentState.userProjects?.activeProject.isNullOrEmpty())
            ) {
                persistAutoActivitiesEnabled(chave, false)
                _uiState.update {
                    it.copy(
                        automaticActivitiesEnabled = false,
                        autoActivitiesHealth = AutoActivitiesHealth.Off,
                    )
                }
                stopAutomaticActivityEngine(context)
                showNoProjectNotification()
                return@launch
            }
            persistAutoActivitiesEnabled(chave, enabled)
            // P5: enabling auto-activities resolves the nudge (recompute → false).
            _uiState.update { it.copy(automaticActivitiesEnabled = enabled, autoActivitiesHealth = computeHealth(enabled, context), showAutoActivitiesNudge = if (enabled) false else it.showAutoActivitiesNudge) }
            if (enabled) {
                ensureEngineRunningIfEligible(context)
                // Evaluate immediately so the routine acts the moment it's enabled (only when permissions
                // already suffice; otherwise the ladder → onAutoActivitiesPermissionsGranted handles it).
                if (_uiState.value.locationPermissionSufficient) runImmediateAutoEvaluation()
                activityLogger.logSystem("Automatic activities enabled by user.") // plan004
            } else {
                stopAutomaticActivityEngine(context)
                activityLogger.logSystem("Automatic activities disabled by user.") // plan004
            }
        }
    }

    // Called by AutoActivitiesDialog when the permission ladder completes with all required granted.
    // The ladder is only ever launched to ENABLE automatic activities, so a successful completion
    // captures the user's intent: persist enabled=true and start the engine — WITHOUT requiring the
    // flag to already be true (that requirement was the deadlock that kept the FGS from ever starting
    // on a fresh install).
    fun onAutoActivitiesPermissionsGranted(context: Context) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val chave = currentState.chave
            if (currentState.isProjectsLoading || currentState.isProjectMembershipSyncing) {
                val lang = _languageFlow.value
                persistAutoActivitiesEnabled(chave, false)
                _uiState.update {
                    it.copy(
                        automaticActivitiesEnabled = false,
                        autoActivitiesHealth = AutoActivitiesHealth.Off,
                        notificationPrimary = t("projects.updatingProjects", lang = lang),
                        notificationSecondary = "",
                        notificationTone = NotificationTone.Info,
                    )
                }
                return@launch
            }
            if (currentState.userProjects?.projects.isNullOrEmpty() ||
                currentState.userProjects?.activeProject.isNullOrEmpty()
            ) {
                persistAutoActivitiesEnabled(chave, false)
                _uiState.update {
                    it.copy(
                        automaticActivitiesEnabled = false,
                        autoActivitiesHealth = AutoActivitiesHealth.Off,
                    )
                }
                stopAutomaticActivityEngine(context)
                showNoProjectNotification()
                return@launch
            }
            persistAutoActivitiesEnabled(chave, true)
            // P5: auto-activities now on → the nudge is resolved.
            _uiState.update { it.copy(automaticActivitiesEnabled = true, autoActivitiesHealth = computeHealth(true, context), showAutoActivitiesNudge = false) }
            ensureEngineRunningIfEligible(context)
            // Permissions just granted → evaluate immediately (same rationale as the toggle path).
            if (_uiState.value.locationPermissionSufficient) runImmediateAutoEvaluation()
        }
    }

    // Called by AutoActivitiesDialog when the ladder ends with at least one required permission
    // denied. Turns the toggle off and persists it.
    fun onAutoActivitiesPermissionsDenied() {
        viewModelScope.launch {
            val chave = _uiState.value.chave
            persistAutoActivitiesEnabled(chave, false)
            _uiState.update { it.copy(automaticActivitiesEnabled = false, autoActivitiesHealth = AutoActivitiesHealth.Off) }
        }
    }

    // P2 (additive, cosmetic): gear-glow health. Off when auto is disabled; else Healthy if all recommended
    // permissions are granted, otherwise Degraded. Reads live permission state; never gates any behavior.
    private fun computeHealth(enabled: Boolean, context: Context): AutoActivitiesHealth =
        if (!enabled) AutoActivitiesHealth.Off
        else if (PermissionLadder.checkStatus(context).allRecommendedGranted) AutoActivitiesHealth.Healthy
        else AutoActivitiesHealth.Degraded

    private suspend fun loadUserSettings(chave: String): UserSettings {
        val json = appPreferences.userSettingsJson.first()
        val map: Map<String, UserSettings?> = runCatching {
            settingsJson.decodeFromString<Map<String, UserSettings?>>(json)
        }.getOrElse { emptyMap() }
        return resolvePersistedUserSettings(map, chave)
    }

    private suspend fun persistAutoActivitiesEnabled(chave: String, enabled: Boolean) {
        updatePersistedUserSettings(chave) {
            it.copy(automaticActivitiesEnabled = enabled)
        }
    }

    private suspend fun persistScheduledPauseSettings(
        chave: String,
        enabled: Boolean,
        from: String,
        to: String,
        suspendSat: Boolean,
        suspendSun: Boolean,
    ) {
        updatePersistedUserSettings(chave) {
            it.copy(
                scheduledPauseEnabled = enabled,
                scheduledPauseFrom = from,
                scheduledPauseTo = to,
                suspendSaturdays = suspendSat,
                suspendSundays = suspendSun,
            )
        }
    }
}
