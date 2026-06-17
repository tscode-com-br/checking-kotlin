package br.com.tscode.checking.presentation.accident

import br.com.tscode.checking.domain.model.AccidentActiveItem
import br.com.tscode.checking.domain.model.AccidentSafetyStatus
import br.com.tscode.checking.domain.model.AccidentState
import br.com.tscode.checking.domain.model.AccidentZone

// Wizard step sequence: project → location → description → situation → confirm
enum class WizardStep { PROJECT, LOCATION, DESCRIPTION, SITUATION, CONFIRM }

data class WizardProject(val id: Int, val name: String)
data class WizardLocation(val id: Int, val name: String, val registered: Boolean)

data class WizardState(
    val step: WizardStep = WizardStep.PROJECT,
    val projects: List<WizardProject> = emptyList(),
    val selectedProjectId: Int? = null,
    val selectedProjectName: String = "",
    val isLoadingProjects: Boolean = false,
    val locations: List<WizardLocation> = emptyList(),
    val selectedLocationId: Int? = null,
    val selectedLocationName: String = "",
    val customLocationName: String = "",
    val useCustomLocation: Boolean = false,
    val isLoadingLocations: Boolean = false,
    val description: String = "",
    val selectedZone: AccidentZone? = null,
    val selectedStatus: AccidentSafetyStatus? = null,
    val isSubmitting: Boolean = false,
    val errorMessage: String = "",
) {
    val effectiveLocationId: Int? get() = if (useCustomLocation) null else selectedLocationId
    val effectiveCustomName: String? get() = if (useCustomLocation) customLocationName.ifBlank { null } else null
    val effectiveLocationLabel: String get() = when {
        useCustomLocation -> customLocationName.ifBlank { "?" }
        selectedLocationName.isNotBlank() -> selectedLocationName
        else -> "?"
    }
    val canProceedProject: Boolean get() = selectedProjectId != null
    val canProceedLocation: Boolean get() = selectedLocationId != null || (useCustomLocation && customLocationName.isNotBlank())
    val canProceedSituation: Boolean get() = selectedZone != null && selectedStatus != null
    val canSubmitConfirm: Boolean get() = canProceedProject && canProceedLocation && canProceedSituation && !isSubmitting
}

enum class AutoCheckinStatus { PENDING, SUCCESS, FAILED }

// Zone confirm sub-state (for the two-step zone report confirmation)
sealed class ZoneConfirmStep {
    object None : ZoneConfirmStep()
    // First tap on accidentZone expands to ok/help buttons (no confirm dialog needed for expand)
    object AccidentExpanded : ZoneConfirmStep()
    // Confirm dialogs before POSTing
    data class ConfirmSafety(val accidentId: Int) : ZoneConfirmStep()
    data class ConfirmAccidentOk(val accidentId: Int) : ZoneConfirmStep()
    data class ConfirmAccidentHelp(val accidentId: Int) : ZoneConfirmStep()
}

data class AccidentUiState(
    val accidentState: AccidentState? = null,
    // Session-scoped ack trackers (reset on login/logout)
    val ackShownForAccidentIds: Set<Int> = emptySet(),
    val ackDialogQueue: List<AccidentActiveItem> = emptyList(),
    val ackDialogShowing: AccidentActiveItem? = null,
    // Check web state inputs (drives canReportAccident)
    val hasCurrentDayCheckin: Boolean = false,
    val currentActionIsCheckin: Boolean = false,
    // Auto-checkin retry status per accident id
    val autoCheckinStatus: Map<Int, AutoCheckinStatus> = emptyMap(),
    // Zone confirm sub-state
    val zoneConfirmStep: ZoneConfirmStep = ZoneConfirmStep.None,
    // Wizard
    val wizardOpen: Boolean = false,
    val wizardState: WizardState? = null,
    // Post-report state
    val reportSentForAccidentId: Int? = null,
    // Actions dialog (when accident already active, show options)
    val actionsDialogOpen: Boolean = false,
    // Video record screen overlay
    val videoScreenOpen: Boolean = false,
    // Emergency call result message
    val emergencyMessage: String = "",
    // Loading + error
    val isLoading: Boolean = false,
    val bannerMessage: String = "",
    val needsDisableAutoActivities: Boolean = false,
) {
    val isActive: Boolean get() = accidentState?.isActive == true
    val activeAccidents: List<AccidentActiveItem> get() = accidentState?.activeAccidents ?: emptyList()

    // The first active accident to display inquiry for (single-accident common case)
    val primaryActiveAccident: AccidentActiveItem? get() = activeAccidents.firstOrNull()

    val canReportAccident: Boolean get() = hasCurrentDayCheckin && currentActionIsCheckin

    // Inquiry card scenario for a given accident
    fun inquiryScenario(
        accident: AccidentActiveItem,
        userActiveProject: String,
        automticActivitiesEnabled: Boolean,
    ): InquiryScenario {
        val report = accident.currentUserReport
        return when {
            // Already reported → post-report state
            report?.reportedAt != null -> InquiryScenario.PostReport
            // Checked in at accident's project → show zone buttons
            currentActionIsCheckin && userActiveProject == accident.projectName -> InquiryScenario.ShowZoneButtons
            // Checked in at different project → hide card
            currentActionIsCheckin -> InquiryScenario.HideCard
            // Checked out
            !automticActivitiesEnabled -> InquiryScenario.CheckedOutAutoOff
            else -> when (autoCheckinStatus[accident.accidentId]) {
                AutoCheckinStatus.PENDING -> InquiryScenario.AutoCheckinRunning
                AutoCheckinStatus.SUCCESS -> InquiryScenario.ShowZoneButtons
                AutoCheckinStatus.FAILED -> InquiryScenario.AutoCheckinFailed
                null -> InquiryScenario.TriggerAutoCheckin // will trigger retries
            }
        }
    }
}

enum class InquiryScenario {
    ShowZoneButtons,
    PostReport,
    HideCard,
    CheckedOutAutoOff,
    AutoCheckinRunning,
    AutoCheckinFailed,
    TriggerAutoCheckin,
}
