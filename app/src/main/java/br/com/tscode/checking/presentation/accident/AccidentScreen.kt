package br.com.tscode.checking.presentation.accident

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.tscode.checking.domain.model.AccidentActiveItem
import br.com.tscode.checking.domain.model.AccidentSafetyStatus
import br.com.tscode.checking.domain.model.AccidentZone
import br.com.tscode.checking.i18n.TranslateFunction
import br.com.tscode.checking.presentation.components.PrimaryButton
import br.com.tscode.checking.presentation.components.SecondaryButton
import br.com.tscode.checking.presentation.theme.CheckingAccident
import br.com.tscode.checking.presentation.theme.CheckingCardBg
import br.com.tscode.checking.presentation.theme.CheckingError
import br.com.tscode.checking.presentation.theme.CheckingOnPrimary
import br.com.tscode.checking.presentation.theme.CheckingPrimary
import br.com.tscode.checking.presentation.theme.CheckingTextMuted
import br.com.tscode.checking.presentation.theme.CheckingTextStrong
import br.com.tscode.checking.presentation.theme.Tokens

// ---- Accident Banner ----

@Composable
fun AccidentBanner(
    message: String,
    modifier: Modifier = Modifier,
) {
    if (message.isBlank()) return
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(CheckingError)
            .padding(horizontal = Tokens.cardPaddingSmall, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.labelMedium,
            color = CheckingOnPrimary,
            fontWeight = FontWeight.Bold,
        )
    }
}

// ---- Inquiry Card ----

@Composable
fun AccidentInquiryCard(
    accident: AccidentActiveItem,
    scenario: InquiryScenario,
    zoneConfirmStep: ZoneConfirmStep,
    reportSentForAccidentId: Int?,
    emergencyMessage: String,
    onZoneSafetyTap: () -> Unit,
    onZoneAccidentTap: () -> Unit,
    onZoneAccidentOkTap: () -> Unit,
    onZoneAccidentHelpTap: () -> Unit,
    onZoneConfirm: () -> Unit,
    onZoneConfirmDismiss: () -> Unit,
    onTriggerEmergencyCall: () -> Unit,
    onEmergencyMessageDismiss: () -> Unit,
    t: TranslateFunction,
    modifier: Modifier = Modifier,
) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Card(
            modifier = modifier
                .widthIn(max = Tokens.cardMaxWidth)
                .fillMaxWidth(),
            shape = RoundedCornerShape(Tokens.cardRadius),
            colors = CardDefaults.cardColors(containerColor = CheckingCardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = Tokens.cardElevation),
        ) {
            Column(
                modifier = Modifier.padding(Tokens.cardPadding),
                verticalArrangement = Arrangement.spacedBy(Tokens.sectionGap),
            ) {
                // Header
                Column {
                    Text(
                        text = accident.projectName,
                        style = MaterialTheme.typography.titleSmall,
                        color = CheckingError,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = accident.locationName,
                        style = MaterialTheme.typography.bodySmall,
                        color = CheckingTextMuted,
                    )
                    if (accident.description != null) {
                        Text(
                            text = accident.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = CheckingTextMuted,
                        )
                    }
                }

                when (scenario) {
                    InquiryScenario.ShowZoneButtons -> {
                        if (reportSentForAccidentId == accident.accidentId) {
                            // Post-report state
                            Text(
                                text = t("accident.situationSent", null),
                                style = MaterialTheme.typography.bodyMedium,
                                color = CheckingPrimary,
                            )
                            SecondaryButton(
                                text = t("accident.triggerEmergency", null),
                                onClick = onTriggerEmergencyCall,
                            )
                        } else {
                            ZoneButtons(
                                zoneConfirmStep = zoneConfirmStep,
                                accidentId = accident.accidentId,
                                onSafety = onZoneSafetyTap,
                                onAccident = onZoneAccidentTap,
                                onAccidentOk = onZoneAccidentOkTap,
                                onAccidentHelp = onZoneAccidentHelpTap,
                                t = t,
                            )
                        }
                        if (emergencyMessage.isNotBlank()) {
                            Text(
                                text = emergencyMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = CheckingError,
                                modifier = Modifier.clickable { onEmergencyMessageDismiss() },
                            )
                        }
                    }
                    InquiryScenario.AutoCheckinRunning -> {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = CheckingPrimary,
                        )
                        Text(
                            text = t("status.runningAutomaticActivitySequence", null),
                            style = MaterialTheme.typography.bodySmall,
                            color = CheckingTextMuted,
                        )
                    }
                    InquiryScenario.AutoCheckinFailed -> {
                        Text(
                            text = t("accident.fallback.manualCheckin", null),
                            style = MaterialTheme.typography.bodyMedium,
                            color = CheckingError,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    InquiryScenario.PostReport -> {
                        Text(
                            text = t("accident.situationSent", null),
                            style = MaterialTheme.typography.bodyMedium,
                            color = CheckingPrimary,
                        )
                        SecondaryButton(
                            text = t("accident.triggerEmergency", null),
                            onClick = onTriggerEmergencyCall,
                        )
                    }
                    InquiryScenario.HideCard,
                    InquiryScenario.CheckedOutAutoOff,
                    InquiryScenario.TriggerAutoCheckin -> { /* handled by caller */ }
                }
            }
        }
    }

    // Zone confirm dialogs
    when (zoneConfirmStep) {
        is ZoneConfirmStep.ConfirmSafety -> ZoneConfirmDialog(
            text = t("accident.confirm.safety", null),
            onConfirm = onZoneConfirm,
            onDismiss = onZoneConfirmDismiss,
            t = t,
        )
        is ZoneConfirmStep.ConfirmAccidentOk -> ZoneConfirmDialog(
            text = t("accident.confirm.accidentOk", null),
            onConfirm = onZoneConfirm,
            onDismiss = onZoneConfirmDismiss,
            t = t,
        )
        is ZoneConfirmStep.ConfirmAccidentHelp -> ZoneConfirmDialog(
            text = t("accident.confirm.help", null),
            onConfirm = onZoneConfirm,
            onDismiss = onZoneConfirmDismiss,
            t = t,
        )
        else -> {}
    }
}

@Composable
private fun ZoneButtons(
    zoneConfirmStep: ZoneConfirmStep,
    accidentId: Int,
    onSafety: () -> Unit,
    onAccident: () -> Unit,
    onAccidentOk: () -> Unit,
    onAccidentHelp: () -> Unit,
    t: TranslateFunction,
) {
    val accidentExpanded = zoneConfirmStep is ZoneConfirmStep.AccidentExpanded

    Column(verticalArrangement = Arrangement.spacedBy(Tokens.itemGap)) {
        Text(
            text = t("accident.inquiry.title", null),
            style = MaterialTheme.typography.labelMedium,
            color = CheckingTextMuted,
        )
        SecondaryButton(
            text = t("accident.inquiry.safetyZone", null),
            onClick = onSafety,
        )
        if (!accidentExpanded) {
            SecondaryButton(
                text = t("accident.inquiry.accidentZone", null),
                onClick = onAccident,
            )
        } else {
            SecondaryButton(
                text = t("accident.inquiry.imOk", null),
                onClick = onAccidentOk,
            )
            PrimaryButton(
                text = t("accident.inquiry.needHelp", null),
                onClick = onAccidentHelp,
            )
        }
    }
}

@Composable
private fun ZoneConfirmDialog(
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    t: TranslateFunction,
) {
    BackHandler { onDismiss() }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ) { onDismiss() },
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = Tokens.cardMaxWidth)
                .fillMaxWidth(0.85f)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) {},
            shape = RoundedCornerShape(Tokens.cardRadiusLarge),
            colors = CardDefaults.cardColors(containerColor = CheckingCardBg),
        ) {
            Column(
                modifier = Modifier.padding(Tokens.cardPadding),
                verticalArrangement = Arrangement.spacedBy(Tokens.sectionGap),
            ) {
                Text(text, style = MaterialTheme.typography.bodyMedium, color = CheckingTextStrong)
                Row(horizontalArrangement = Arrangement.spacedBy(Tokens.itemGap)) {
                    SecondaryButton(text = t("transport.historyCloseButton", null), onClick = onDismiss, modifier = Modifier.weight(1f))
                    PrimaryButton(text = t("accident.ack.button", null), onClick = onConfirm, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// ---- Acknowledgement Dialog ----

@Composable
fun AccidentAckDialog(
    accident: AccidentActiveItem,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    t: TranslateFunction,
) {
    BackHandler { onDismiss() }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ) {},
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = Tokens.cardMaxWidth)
                .fillMaxWidth(0.9f),
            shape = RoundedCornerShape(Tokens.cardRadiusLarge),
            colors = CardDefaults.cardColors(containerColor = CheckingCardBg),
        ) {
            Column(
                modifier = Modifier
                    .padding(Tokens.cardPadding)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Tokens.sectionGap),
            ) {
                Text(
                    text = t("accident.ack.title", null),
                    style = MaterialTheme.typography.titleMedium,
                    color = CheckingError,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = accident.projectName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CheckingTextStrong,
                )
                Text(
                    text = accident.locationName,
                    style = MaterialTheme.typography.bodySmall,
                    color = CheckingTextMuted,
                )
                if (accident.description != null) {
                    Text(
                        text = accident.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = CheckingTextMuted,
                    )
                }
                Text(
                    text = t("accident.ack.checkinReminder", null),
                    style = MaterialTheme.typography.bodyMedium,
                    color = CheckingError,
                    fontWeight = FontWeight.Bold,
                )
                PrimaryButton(text = t("accident.ack.button", null), onClick = onConfirm)
            }
        }
    }
}

// ---- Actions Dialog ----

@Composable
fun AccidentActionsDialog(
    onOpenWizard: () -> Unit,
    onVideoRecord: () -> Unit,
    onDismiss: () -> Unit,
    t: TranslateFunction,
) {
    BackHandler { onDismiss() }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ) { onDismiss() },
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = Tokens.cardMaxWidth)
                .fillMaxWidth(0.85f)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) {},
            shape = RoundedCornerShape(Tokens.cardRadiusLarge),
            colors = CardDefaults.cardColors(containerColor = CheckingCardBg),
        ) {
            Column(
                modifier = Modifier.padding(Tokens.cardPadding),
                verticalArrangement = Arrangement.spacedBy(Tokens.sectionGap),
            ) {
                Text(
                    text = t("accident.actions.title", null),
                    style = MaterialTheme.typography.titleMedium,
                    color = CheckingTextStrong,
                )
                SecondaryButton(text = t("accident.actions.audioVideo", null), onClick = onVideoRecord)
                SecondaryButton(text = t("accident.actions.reportNew", null), onClick = onOpenWizard)
                SecondaryButton(text = t("accident.actions.back", null), onClick = onDismiss)
            }
        }
    }
}

// ---- Report Wizard ----

@Composable
fun AccidentWizard(
    wizardState: WizardState,
    onProjectSelected: (Int, String) -> Unit,
    onNextFromProject: () -> Unit,
    onLocationSelected: (Int, String) -> Unit,
    onCustomLocationToggled: () -> Unit,
    onCustomLocationChanged: (String) -> Unit,
    onNextFromLocation: () -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onNextFromDescription: () -> Unit,
    onSituationSelected: (AccidentZone, AccidentSafetyStatus) -> Unit,
    onNextFromSituation: () -> Unit,
    onConfirmSubmit: () -> Unit,
    onBack: () -> Unit,
    onDismiss: () -> Unit,
    t: TranslateFunction,
) {
    BackHandler { onBack() }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ) { onDismiss() },
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = Tokens.cardMaxWidth)
                .fillMaxWidth(0.92f)
                .padding(vertical = 32.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) {},
            shape = RoundedCornerShape(Tokens.cardRadiusLarge),
            colors = CardDefaults.cardColors(containerColor = CheckingCardBg),
        ) {
            Column(
                modifier = Modifier
                    .padding(Tokens.cardPadding)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Tokens.sectionGap),
            ) {
                if (wizardState.errorMessage.isNotBlank()) {
                    Text(wizardState.errorMessage, color = CheckingError, style = MaterialTheme.typography.bodySmall)
                }

                when (wizardState.step) {
                    WizardStep.PROJECT -> WizardProjectStep(
                        wizardState = wizardState,
                        onProjectSelected = onProjectSelected,
                        onNext = onNextFromProject,
                        onBack = onBack,
                        t = t,
                    )
                    WizardStep.LOCATION -> WizardLocationStep(
                        wizardState = wizardState,
                        onLocationSelected = onLocationSelected,
                        onCustomToggled = onCustomLocationToggled,
                        onCustomChanged = onCustomLocationChanged,
                        onNext = onNextFromLocation,
                        onBack = onBack,
                        t = t,
                    )
                    WizardStep.DESCRIPTION -> WizardDescriptionStep(
                        wizardState = wizardState,
                        onDescriptionChanged = onDescriptionChanged,
                        onNext = onNextFromDescription,
                        onBack = onBack,
                        t = t,
                    )
                    WizardStep.SITUATION -> WizardSituationStep(
                        wizardState = wizardState,
                        onSituationSelected = onSituationSelected,
                        onNext = onNextFromSituation,
                        onBack = onBack,
                        t = t,
                    )
                    WizardStep.CONFIRM -> WizardConfirmStep(
                        wizardState = wizardState,
                        onConfirm = onConfirmSubmit,
                        onBack = onBack,
                        t = t,
                    )
                }
            }
        }
    }
}

@Composable
private fun WizardProjectStep(
    wizardState: WizardState,
    onProjectSelected: (Int, String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    t: TranslateFunction,
) {
    Text(t("accident.wizard.selectProject", null), style = MaterialTheme.typography.titleSmall, color = CheckingTextStrong)
    if (wizardState.isLoadingProjects) {
        CircularProgressIndicator(color = CheckingPrimary)
    } else {
        Column {
            wizardState.projects.forEach { project ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { onProjectSelected(project.id, project.name) },
                ) {
                    RadioButton(
                        selected = project.id == wizardState.selectedProjectId,
                        onClick = { onProjectSelected(project.id, project.name) },
                        colors = RadioButtonDefaults.colors(selectedColor = CheckingPrimary),
                    )
                    Text(project.name, style = MaterialTheme.typography.bodyMedium, color = CheckingTextStrong)
                }
            }
        }
    }
    WizardNavRow(canNext = wizardState.canProceedProject, onNext = onNext, onBack = onBack, t = t)
}

@Composable
private fun WizardLocationStep(
    wizardState: WizardState,
    onLocationSelected: (Int, String) -> Unit,
    onCustomToggled: () -> Unit,
    onCustomChanged: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    t: TranslateFunction,
) {
    Text(t("accident.wizard.selectLocation", null), style = MaterialTheme.typography.titleSmall, color = CheckingTextStrong)
    if (wizardState.isLoadingLocations) {
        CircularProgressIndicator(color = CheckingPrimary)
    } else {
        Column {
            wizardState.locations.forEach { location ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { onLocationSelected(location.id, location.name) },
                ) {
                    RadioButton(
                        selected = !wizardState.useCustomLocation && location.id == wizardState.selectedLocationId,
                        onClick = { onLocationSelected(location.id, location.name) },
                        colors = RadioButtonDefaults.colors(selectedColor = CheckingPrimary),
                    )
                    Text(location.name, style = MaterialTheme.typography.bodyMedium, color = CheckingTextStrong)
                }
            }
            // Custom location option
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable { onCustomToggled() },
            ) {
                RadioButton(
                    selected = wizardState.useCustomLocation,
                    onClick = onCustomToggled,
                    colors = RadioButtonDefaults.colors(selectedColor = CheckingPrimary),
                )
                Text("Outro local", style = MaterialTheme.typography.bodyMedium, color = CheckingTextStrong)
            }
            if (wizardState.useCustomLocation) {
                OutlinedTextField(
                    value = wizardState.customLocationName,
                    onValueChange = onCustomChanged,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        }
    }
    WizardNavRow(canNext = wizardState.canProceedLocation, onNext = onNext, onBack = onBack, t = t)
}

@Composable
private fun WizardDescriptionStep(
    wizardState: WizardState,
    onDescriptionChanged: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    t: TranslateFunction,
) {
    Text(t("accident.description.title", null), style = MaterialTheme.typography.titleSmall, color = CheckingTextStrong)
    OutlinedTextField(
        value = wizardState.description,
        onValueChange = onDescriptionChanged,
        placeholder = { Text(t("accident.description.placeholder", null), style = MaterialTheme.typography.bodySmall) },
        modifier = Modifier.fillMaxWidth().height(120.dp),
        maxLines = 6,
    )
    Text(
        text = "${wizardState.description.length}/500",
        style = MaterialTheme.typography.labelSmall,
        color = CheckingTextMuted,
    )
    WizardNavRow(canNext = true, onNext = onNext, onBack = onBack, t = t)
}

@Composable
private fun WizardSituationStep(
    wizardState: WizardState,
    onSituationSelected: (AccidentZone, AccidentSafetyStatus) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    t: TranslateFunction,
) {
    Text(t("accident.wizard.yourSituation", null), style = MaterialTheme.typography.titleSmall, color = CheckingTextStrong)
    val situations = listOf(
        Triple(AccidentZone.SAFETY, AccidentSafetyStatus.OK, t("accident.inquiry.safetyZone", null)),
        Triple(AccidentZone.ACCIDENT, AccidentSafetyStatus.OK, t("accident.inquiry.imOk", null)),
        Triple(AccidentZone.ACCIDENT, AccidentSafetyStatus.HELP, t("accident.inquiry.needHelp", null)),
    )
    Column {
        situations.forEach { (zone, status, label) ->
            val selected = zone == wizardState.selectedZone && status == wizardState.selectedStatus
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable { onSituationSelected(zone, status) },
            ) {
                RadioButton(
                    selected = selected,
                    onClick = { onSituationSelected(zone, status) },
                    colors = RadioButtonDefaults.colors(selectedColor = CheckingPrimary),
                )
                Text(label, style = MaterialTheme.typography.bodyMedium, color = CheckingTextStrong)
            }
        }
    }
    WizardNavRow(canNext = wizardState.canProceedSituation, onNext = onNext, onBack = onBack, t = t)
}

@Composable
private fun WizardConfirmStep(
    wizardState: WizardState,
    onConfirm: () -> Unit,
    onBack: () -> Unit,
    t: TranslateFunction,
) {
    Text(t("accident.wizard.confirmTitle", null), style = MaterialTheme.typography.titleSmall, color = CheckingTextStrong)
    Text(
        text = t(
            "accident.wizard.confirmTextTemplate",
            mapOf(
                "location" to wizardState.effectiveLocationLabel,
                "project" to wizardState.selectedProjectName,
            )
        ),
        style = MaterialTheme.typography.bodyMedium,
        color = CheckingTextStrong,
    )
    Row(horizontalArrangement = Arrangement.spacedBy(Tokens.itemGap)) {
        SecondaryButton(
            text = t("transport.requestBuilder.backButton", null),
            onClick = onBack,
            modifier = Modifier.weight(1f),
        )
        PrimaryButton(
            text = if (wizardState.isSubmitting) "..." else t("accident.ack.button", null),
            onClick = onConfirm,
            enabled = wizardState.canSubmitConfirm,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun WizardNavRow(
    canNext: Boolean,
    onNext: () -> Unit,
    onBack: () -> Unit,
    t: TranslateFunction,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Tokens.itemGap),
    ) {
        SecondaryButton(
            text = t("transport.requestBuilder.backButton", null),
            onClick = onBack,
            modifier = Modifier.weight(1f),
        )
        PrimaryButton(
            text = "Próximo",
            onClick = onNext,
            enabled = canNext,
            modifier = Modifier.weight(1f),
        )
    }
}

// ---- Report Button ----

@Composable
fun AccidentReportButton(
    isActive: Boolean,
    canReport: Boolean,
    onTap: () -> Unit,
    t: TranslateFunction,
) {
    // Always visible (below the "Registrar" button) so the user can open the accident
    // report wizard at any time. Label flips to "Acidente Reportado" while one is active.
    val label = if (isActive) t("accident.button.reported", null)
    else t("accident.button.report", null)

    val shape = RoundedCornerShape(Tokens.controlRadius)
    val pulse = Color(0xFFFF4D57)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(Tokens.controlHeight)
            .then(
                // Pulsing glow when an accident is active (CSS aria-pressed glow).
                if (isActive) {
                    Modifier.shadow(18.dp, shape, clip = false, ambientColor = pulse, spotColor = pulse)
                } else {
                    Modifier
                },
            )
            .clip(shape)
            .background(CheckingAccident)
            .clickable(onClick = onTap),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = CheckingOnPrimary,
        )
    }
}
