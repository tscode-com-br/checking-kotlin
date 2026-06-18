package br.com.tscode.checking.presentation.check

import br.com.tscode.checking.domain.model.AuthStatus
import br.com.tscode.checking.domain.model.CheckAction
import br.com.tscode.checking.domain.model.CheckHistoryEntry
import br.com.tscode.checking.domain.model.HistoryState
import br.com.tscode.checking.domain.model.LocationMatch
import br.com.tscode.checking.domain.model.Project
import br.com.tscode.checking.domain.model.UserProjects
import br.com.tscode.checking.presentation.components.FieldGlow

enum class CheckDialog { PasswordChange, SelfRegistration, Settings, AutoActivities, ScheduledPause, Notifications, EvaluationLog, History }

enum class NotificationTone { None, Info, Success, Error, Teal }

// Auto-activities health for the gear glow (P2). Off = disabled (no glow); Healthy = on + all recommended
// permissions granted (green); Degraded = on but a recommended permission is missing (orange).
enum class AutoActivitiesHealth { Off, Healthy, Degraded }

// Pure mapping from health to the reusable field-glow state (mirrors the chave/senha glow). Off → no glow;
// Healthy → green (same as authenticated field); Degraded → orange (same as pending field).
fun AutoActivitiesHealth.toGlow(): FieldGlow = when (this) {
    AutoActivitiesHealth.Off -> FieldGlow.None
    AutoActivitiesHealth.Healthy -> FieldGlow.Authenticated
    AutoActivitiesHealth.Degraded -> FieldGlow.Pending
}

// P5: pure predicate for the one-time first-login nudge. The card is shown only for an authenticated
// user who has NOT enabled automatic activities and has NOT dismissed it (per-chave persisted flag).
// Extracted as a top-level pure function so it is unit-testable without instantiating the ViewModel.
fun shouldShowAutoActivitiesNudge(authenticated: Boolean, autoEnabled: Boolean, dismissed: Boolean): Boolean =
    authenticated && !autoEnabled && !dismissed

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
    // P2.2 — full check-in/out history dialog (change D). `historyDialogAction` selects which action's
    // rows the dialog shows (driven by tapping the matching HistoryCard cell).
    val historyDialogAction: CheckAction? = null,
    val historyDialogEntries: List<CheckHistoryEntry> = emptyList(),
    val isHistoryDialogLoading: Boolean = false,
    val historyDialogError: Boolean = false,
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
    // Health of the auto-activities engine, for the gear glow (P2). Off when disabled; Healthy/Degraded
    // (derived in the ViewModel) when enabled. Purely presentational — never gates behavior.
    val autoActivitiesHealth: AutoActivitiesHealth = AutoActivitiesHealth.Off,
    // P5: one-time, dismissible first-login nudge to enable automatic activities (per-chave; persisted
    // via the generic flag API). Transient card in the Check screen body — see shouldShowAutoActivitiesNudge.
    val showAutoActivitiesNudge: Boolean = false,

    // Scheduled pause (§23.4.2) — install defaults: on 20:00–07:00 daily + full-day Sat/Sun suspension.
    val scheduledPauseEnabled: Boolean = true,
    val scheduledPauseFrom: String = "20:00",
    val scheduledPauseTo: String = "07:00",
    val suspendSaturdays: Boolean = true,
    val suspendSundays: Boolean = true,

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
