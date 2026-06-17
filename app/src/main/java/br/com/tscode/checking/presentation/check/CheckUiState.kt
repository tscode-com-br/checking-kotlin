package br.com.tscode.checking.presentation.check

import br.com.tscode.checking.domain.model.AuthStatus
import br.com.tscode.checking.domain.model.CheckAction
import br.com.tscode.checking.domain.model.HistoryState
import br.com.tscode.checking.domain.model.LocationMatch
import br.com.tscode.checking.domain.model.Project
import br.com.tscode.checking.domain.model.UserProjects

enum class CheckDialog { PasswordChange, SelfRegistration, Settings, AutoActivities, ScheduledPause, Permissions, Notifications, EvaluationLog }

enum class NotificationTone { None, Info, Success, Error, Teal }

enum class UiInformeType { NORMAL, RETROATIVO }

data class PasswordChangeFields(
    val oldPw: String = "",
    val newPw: String = "",
    val confirmPw: String = "",
    val errorMessage: String = "",
    val isBusy: Boolean = false,
)

data class SelfRegistrationFields(
    val chave: String = "",
    val nome: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPw: String = "",
    val selectedProjectIds: List<Int> = emptyList(),
    val errorMessage: String = "",
    val isBusy: Boolean = false,
    val projectCatalog: List<Project> = emptyList(),
    val isLoadingProjects: Boolean = false,
)

data class CheckUiState(
    // Startup
    val isInitializing: Boolean = true,

    // Auth
    val chave: String = "",
    val password: String = "",
    val authStatus: AuthStatus? = null,
    val isStatusLoading: Boolean = false,
    val prompt: String = "",
    val statusErrored: Boolean = false,
    val dismissedAssistanceForChave: String = "",

    // Notification
    val notificationPrimary: String = "",
    val notificationSecondary: String = "",
    val notificationTone: NotificationTone = NotificationTone.None,

    // History
    val historyState: HistoryState? = null,
    val isHistoryLoading: Boolean = false,
    // Transport service availability — server OR over all the user's project memberships
    // (is_transport_enabled_for_any_project). Drives the "Transporte" button visibility.
    // Tracked separately because the POST /check response doesn't carry this flag.
    val transportEnabled: Boolean = false,

    // Location
    val locationMatch: LocationMatch? = null,
    val isLocationLoading: Boolean = false,
    val locationPermissionGranted: Boolean = false,
    // True only when BOTH "Allow all the time" (background) and precise (fine) location are
    // granted. When false, automatic activities can't run: no GPS is captured, the "Local"
    // card is hidden, and the auto-activities toggle is forced off & disabled (§ items 2/6).
    val locationPermissionSufficient: Boolean = false,

    // User projects
    val userProjects: UserProjects? = null,
    val isProjectsLoading: Boolean = false,
    // Full catalogue of selectable projects (for the membership checkboxes on the main screen)
    val mainProjectCatalog: List<Project> = emptyList(),

    // Manual location (Situation 9 — accuracy too low)
    val availableLocations: List<String> = emptyList(),
    val selectedManualLocation: String? = null,

    // Registration
    val selectedAction: CheckAction = CheckAction.CHECKIN,
    val selectedInforme: UiInformeType = UiInformeType.NORMAL,
    val isSubmitting: Boolean = false,

    // Auto activities
    val automaticActivitiesEnabled: Boolean = false,

    // Scheduled pause (§23.4.2)
    val scheduledPauseEnabled: Boolean = false,
    val scheduledPauseFrom: String = "22:00",
    val scheduledPauseTo: String = "06:00",
    val suspendSaturdays: Boolean = false,
    val suspendSundays: Boolean = false,

    // Push-notification preferences
    val notifyActivities: Boolean = true,
    val notifyScheduledPause: Boolean = true,
    val notifyAccident: Boolean = true,

    // Dialogs
    val dialogOpen: CheckDialog? = null,
    val passwordChangeFields: PasswordChangeFields = PasswordChangeFields(),
    val selfRegistrationFields: SelfRegistrationFields = SelfRegistrationFields(),
) {
    val isAuthenticated: Boolean get() = authStatus?.authenticated == true
    val isFound: Boolean get() = authStatus?.found == true
    val hasPassword: Boolean get() = authStatus?.hasPassword == true

    // Situation 9 guard: manual location required when accuracy too low
    val isAccuracyTooLow: Boolean
        get() = locationMatch != null &&
            locationMatch.status == br.com.tscode.checking.domain.model.MatchStatus.ACCURACY_TOO_LOW

    // Manual location is required (and the "Local" dropdown is shown) whenever automatic
    // activities are OFF, or while the low-accuracy fallback is active (Situation 9).
    val requiresManualLocation: Boolean
        get() = !automaticActivitiesEnabled || isAccuracyTooLow

    val canSubmit: Boolean
        get() = isAuthenticated && !isSubmitting && when {
            !requiresManualLocation -> locationMatch != null
            // Manual check-out doesn't require a location (sent as "Desconhecido" if none);
            // manual check-in still requires one.
            selectedAction == CheckAction.CHECKOUT -> true
            else -> selectedManualLocation != null
        }
}
