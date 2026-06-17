package br.com.tscode.checking.presentation.check

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.content.ContextCompat
import br.com.tscode.checking.domain.model.CheckAction
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.tscode.checking.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import br.com.tscode.checking.i18n.rememberT
import br.com.tscode.checking.presentation.accident.AccidentAckDialog
import br.com.tscode.checking.presentation.accident.AccidentActionsDialog
import br.com.tscode.checking.presentation.accident.AccidentBanner
import br.com.tscode.checking.presentation.accident.AccidentInquiryCard
import br.com.tscode.checking.presentation.accident.AccidentReportButton
import br.com.tscode.checking.presentation.accident.AccidentViewModel
import br.com.tscode.checking.presentation.accident.AccidentWizard
import br.com.tscode.checking.presentation.accident.InquiryScenario
import br.com.tscode.checking.presentation.accident.VideoRecordScreen
import br.com.tscode.checking.presentation.theme.ProvideAccidentTheme
import br.com.tscode.checking.presentation.transport.TransportScreen
import br.com.tscode.checking.presentation.transport.TransportViewModel
import br.com.tscode.checking.presentation.components.AuthRow
import br.com.tscode.checking.presentation.components.CheckCard
import br.com.tscode.checking.presentation.components.HistoryCard
import br.com.tscode.checking.presentation.components.InformeFieldset
import br.com.tscode.checking.presentation.components.LocationCard
import br.com.tscode.checking.presentation.components.LocationSelectField
import br.com.tscode.checking.presentation.components.NotificationCard
import br.com.tscode.checking.presentation.components.PasswordChangeDialog
import br.com.tscode.checking.presentation.components.PrimaryButton
import br.com.tscode.checking.presentation.components.ProjectsFieldset
import br.com.tscode.checking.presentation.components.RegistrationFieldset
import br.com.tscode.checking.presentation.components.SelfRegistrationDialog
import br.com.tscode.checking.presentation.components.SettingsDialog
import br.com.tscode.checking.presentation.settings.autoactivities.AutoActivitiesDialog
import br.com.tscode.checking.platform.background.AccidentWatchWorker
import br.com.tscode.checking.platform.background.offline.SyncPendingChecksWorker
import br.com.tscode.checking.presentation.settings.permissions.PermissionsDialog
import br.com.tscode.checking.presentation.settings.notifications.NotificationsDialog
import br.com.tscode.checking.presentation.settings.diagnostics.EvaluationLogDialog
import br.com.tscode.checking.presentation.settings.scheduledpause.ScheduledPauseDialog
import br.com.tscode.checking.presentation.theme.CheckingHeaderBg
import br.com.tscode.checking.presentation.theme.CheckingOnPrimary
import br.com.tscode.checking.presentation.theme.CheckingSurfaceEnd
import br.com.tscode.checking.presentation.theme.CheckingSurfaceStart
import br.com.tscode.checking.presentation.theme.Tokens

@Composable
fun CheckScreen(
    onNavigateToManual: () -> Unit = {},
    vm: CheckViewModel = hiltViewModel(),
    transportVm: TransportViewModel = hiltViewModel(),
    accidentVm: AccidentViewModel = hiltViewModel(),
) {
    val state by vm.uiState.collectAsState()
    val langCode by vm.languageFlow.collectAsState()
    val t = rememberT(vm.languageFlow)
    val transportState by transportVm.uiState.collectAsState()
    val accidentState by accidentVm.uiState.collectAsState()
    var transportScreenOpen by remember { mutableStateOf(false) }

    // Wire accident lifecycle to check state
    LaunchedEffect(state.isAuthenticated, state.chave) {
        if (state.isAuthenticated && state.chave.length == 4) {
            accidentVm.onLogin(state.chave)
            accidentVm.onDisableAutoActivities = { /* TODO Phase 6: vm.setAutomaticActivitiesEnabled(false) */ }
        } else if (!state.isAuthenticated) {
            accidentVm.onLogout()
        }
    }
    LaunchedEffect(state.historyState) {
        state.historyState?.let { accidentVm.onCheckWebState(it, state.userProjects?.activeProject ?: "") }
    }

    LaunchedEffect(state.dialogOpen) {
        if (state.dialogOpen == CheckDialog.SelfRegistration) {
            vm.loadProjectCatalogForRegistration()
        }
    }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // Evaluate live location-permission sufficiency — precise (fine) + "Allow all the time"
    // (background) — and push it to the VM. This gates GPS capture, the "Local" card, and the
    // auto-activities toggle (§ items 2/6). No location is requested automatically here; the
    // user grants it from Ajustes › Permissões.
    val evaluateLocationPermission = {
        val fine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val background = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            fine
        }
        vm.onLocationPermissionStateChanged(fine, background, context)
    }

    // Re-sync check state + re-evaluate permissions whenever the app returns to the foreground
    // (also covers returning from the system permission / settings screens).
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        vm.onForegroundResume()
        evaluateLocationPermission()
    }

    LaunchedEffect(state.isAuthenticated) {
        if (state.isAuthenticated) {
            evaluateLocationPermission()
            // Accident watcher runs independently of automatic activities, so a logged-in
            // user is alerted to an accident in any project even with auto-activities OFF.
            AccidentWatchWorker.enqueue(context)
            // Drain any check events captured offline in a previous session that died before
            // they could sync (P8). No-op when the queue is empty.
            SyncPendingChecksWorker.enqueue(context)
        }
    }

    ProvideAccidentTheme(active = accidentState.isActive) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(CheckingSurfaceStart, CheckingSurfaceEnd)))
            .systemBarsPadding()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ) { focusManager.clearFocus() },
    ) {
        // Petrobras watermark (mirrors web body::before — centered, opacity 0.06)
        Image(
            painter = painterResource(R.drawable.petrobras_watermark),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            alpha = 0.06f,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.78f),
        )
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Tokens.headerHeight)
                    .background(CheckingHeaderBg),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_checking_logo),
                        contentDescription = null,
                        tint = CheckingOnPrimary,
                        modifier = Modifier.size(width = 36.dp, height = 28.dp),
                    )
                    Text(
                        text = t("auth.brand", null),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp,
                        ),
                        color = CheckingOnPrimary,
                    )
                }
            }

            // Accident banner (below header, above scroll)
            AccidentBanner(message = accidentState.bannerMessage)

            // Scrollable body — one big white card holds every section (web .check-card)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(Tokens.sectionGap),
            ) {
                CheckCard {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(Tokens.sectionGap),
                    ) {
                        // History (always visible — placeholders before login, like web)
                        HistoryCard(
                            historyState = state.historyState,
                            t = t,
                            langCode = langCode,
                        )

                        // Accident inquiry card (authenticated + active accident)
                        if (state.isAuthenticated) {
                            val primaryAccident = accidentState.primaryActiveAccident
                            if (primaryAccident != null) {
                                val scenario = accidentState.inquiryScenario(
                                    accident = primaryAccident,
                                    userActiveProject = state.userProjects?.activeProject ?: "",
                                    automticActivitiesEnabled = state.userProjects != null,
                                )
                                if (scenario != InquiryScenario.HideCard &&
                                    scenario != InquiryScenario.CheckedOutAutoOff
                                ) {
                                    AccidentInquiryCard(
                                        accident = primaryAccident,
                                        scenario = scenario,
                                        zoneConfirmStep = accidentState.zoneConfirmStep,
                                        reportSentForAccidentId = accidentState.reportSentForAccidentId,
                                        emergencyMessage = accidentState.emergencyMessage,
                                        onZoneSafetyTap = { accidentVm.onZoneSafetyTap(primaryAccident.accidentId) },
                                        onZoneAccidentTap = accidentVm::onZoneAccidentTap,
                                        onZoneAccidentOkTap = { accidentVm.onZoneAccidentOkTap(primaryAccident.accidentId) },
                                        onZoneAccidentHelpTap = { accidentVm.onZoneAccidentHelpTap(primaryAccident.accidentId) },
                                        onZoneConfirm = accidentVm::onZoneConfirm,
                                        onZoneConfirmDismiss = accidentVm::onZoneConfirmDismiss,
                                        onTriggerEmergencyCall = accidentVm::triggerEmergencyCall,
                                        onEmergencyMessageDismiss = accidentVm::onEmergencyMessageDismiss,
                                        t = t,
                                    )
                                }
                            }
                        }

                        // Notification strip
                        if (state.notificationTone != NotificationTone.None ||
                            state.notificationPrimary.isNotEmpty()
                        ) {
                            NotificationCard(
                                primary = state.notificationPrimary,
                                // "Atualizando a aplicação..." is only meaningful while the GPS
                                // fix is being obtained; otherwise show the regular secondary.
                                secondary = if (state.isLocationLoading) {
                                    t("status.updatingApp", null)
                                } else {
                                    state.notificationSecondary
                                },
                                tone = state.notificationTone,
                            )
                        }

                        // "Local" GPS card — shown only in GPS mode: automatic activities ON
                        // and location permission sufficient (precise + "Allow all the time").
                        // Otherwise it's hidden and no GPS is captured (§ items 2/6).
                        if (state.automaticActivitiesEnabled && state.locationPermissionSufficient) {
                            LocationCard(
                                locationMatch = state.locationMatch,
                                isLoading = state.isLocationLoading,
                                onRefresh = vm::onRefreshLocation,
                                t = t,
                            )
                        }

                        // Auth fields (chave | senha | gear) with colored glow
                        if (!state.isInitializing) {
                            AuthRow(
                                chave = state.chave,
                                onChaveChanged = vm::onChaveChanged,
                                password = state.password,
                                onPasswordChanged = vm::onPasswordChanged,
                                isFound = state.isFound,
                                isAuthenticated = state.isAuthenticated,
                                isStatusLoading = state.isStatusLoading,
                                isStatusAvailable = state.authStatus != null && !state.isStatusLoading,
                                prompt = state.prompt,
                                onSettingsClick = vm::openSettings,
                                onRequestRegistrationClick = vm::openSelfRegistrationDialog,
                                t = t,
                            )
                        }

                        // Authenticated-only sections
                        if (state.isAuthenticated) {
                            RegistrationFieldset(
                                selectedAction = state.selectedAction,
                                onActionSelected = vm::onActionSelected,
                                onTransportOpen = {
                                    transportVm.onOpen(state.chave)
                                    transportScreenOpen = true
                                },
                                transportEnabled = state.transportEnabled,
                                t = t,
                            )

                            InformeFieldset(
                                selected = state.selectedInforme,
                                onSelected = vm::onInformeSelected,
                                t = t,
                            )

                            ProjectsFieldset(
                                catalog = state.mainProjectCatalog,
                                memberships = state.userProjects?.projects ?: emptyList(),
                                isLoading = state.isProjectsLoading,
                                onMembershipToggled = vm::onProjectMembershipToggled,
                                t = t,
                            )

                            // "Local" dropdown — shown whenever manual location is required
                            // (automatic activities OFF, or Situation 9 low-accuracy fallback).
                            // Lists every registered location across the user's projects.
                            if (state.requiresManualLocation && state.availableLocations.isNotEmpty()) {
                                LocationSelectField(
                                    locations = state.availableLocations,
                                    selected = state.selectedManualLocation,
                                    onSelected = vm::onManualLocationSelected,
                                    t = t,
                                )
                            }

                            val submitActionLabel = if (state.selectedAction == CheckAction.CHECKIN) {
                                t("registration.checkinLabel", null)
                            } else {
                                t("registration.checkoutLabel", null)
                            }
                            PrimaryButton(
                                text = "${t("registration.submitButton", null)} $submitActionLabel",
                                onClick = vm::onSubmit,
                                enabled = state.canSubmit,
                            )

                            // Accident report button — always visible, below "Registrar"
                            AccidentReportButton(
                                isActive = accidentState.isActive,
                                canReport = accidentState.canReportAccident,
                                onTap = accidentVm::onReportButtonTap,
                                t = t,
                            )
                        }
                    }
                }
            }
        }

        // Dialog overlays — rendered above main content
        when (state.dialogOpen) {
            CheckDialog.Settings -> SettingsDialog(
                currentLanguage = langCode,
                onLanguageSelected = vm::onLanguageSelected,
                isAuthenticated = state.isAuthenticated,
                hasPassword = state.hasPassword,
                onResetPasswordClick = {
                    vm.dismissDialog()
                    vm.openPasswordChangeDialog()
                },
                onAutoActivitiesClick = {
                    vm.dismissDialog()
                    vm.openAutoActivitiesDialog()
                },
                onScheduledPauseClick = {
                    vm.dismissDialog()
                    vm.openScheduledPauseDialog()
                },
                onPermissionsClick = {
                    vm.dismissDialog()
                    vm.openPermissionsDialog()
                },
                onNotificationsClick = {
                    vm.dismissDialog()
                    vm.openNotificationsDialog()
                },
                onSupportClick = {
                    vm.dismissDialog()
                    val number = t("support.phoneNumber", null)
                    val message = t("support.messageTemplate", mapOf("chave" to state.chave))
                    val url = "https://wa.me/$number?text=" + Uri.encode(message)
                    runCatching {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                        )
                    }
                },
                onAboutClick = {
                    vm.dismissDialog()
                    onNavigateToManual()
                },
                onDismiss = vm::dismissDialog,
                t = t,
            )

            CheckDialog.PasswordChange -> PasswordChangeDialog(
                fields = state.passwordChangeFields,
                hasPassword = state.hasPassword,
                onOldPwChanged = vm::onPasswordChangeOldPwChanged,
                onNewPwChanged = vm::onPasswordChangeNewPwChanged,
                onConfirmPwChanged = vm::onPasswordChangeConfirmPwChanged,
                onSubmit = vm::submitPasswordChange,
                onDismiss = vm::dismissDialog,
                t = t,
            )

            CheckDialog.SelfRegistration -> SelfRegistrationDialog(
                fields = state.selfRegistrationFields,
                onNomeChanged = vm::onRegNomeChanged,
                onEmailChanged = vm::onRegEmailChanged,
                onPasswordChanged = vm::onRegPasswordChanged,
                onConfirmPwChanged = vm::onRegConfirmPwChanged,
                onProjectToggled = vm::onRegProjectToggled,
                onSubmit = vm::submitSelfRegistration,
                onDismiss = vm::dismissDialog,
                t = t,
            )

            CheckDialog.AutoActivities -> AutoActivitiesDialog(
                automaticActivitiesEnabled = state.automaticActivitiesEnabled,
                permissionsSufficient = state.locationPermissionSufficient,
                onToggleChanged = { enabled -> vm.onAutomaticActivitiesToggled(enabled, context) },
                onPermissionsGranted = { vm.onAutoActivitiesPermissionsGranted(context) },
                onPermissionsDenied = vm::onAutoActivitiesPermissionsDenied,
                onDismiss = vm::dismissDialog,
                t = t,
            )

            CheckDialog.ScheduledPause -> ScheduledPauseDialog(
                scheduledPauseEnabled = state.scheduledPauseEnabled,
                scheduledPauseFrom = state.scheduledPauseFrom,
                scheduledPauseTo = state.scheduledPauseTo,
                suspendSaturdays = state.suspendSaturdays,
                suspendSundays = state.suspendSundays,
                onSettingChanged = { enabled, from, to, sat, sun ->
                    vm.onScheduledPauseSettingChanged(enabled, from, to, sat, sun)
                },
                onDismiss = vm::dismissDialog,
                t = t,
            )

            CheckDialog.Permissions -> PermissionsDialog(
                onDismiss = vm::dismissDialog,
                t = t,
            )

            CheckDialog.Notifications -> NotificationsDialog(
                notifyActivities = state.notifyActivities,
                notifyScheduledPause = state.notifyScheduledPause,
                notifyAccident = state.notifyAccident,
                onChanged = { activities, pause, accident ->
                    vm.onNotificationSettingsChanged(activities, pause, accident)
                },
                onDismiss = vm::dismissDialog,
                t = t,
            )

            CheckDialog.EvaluationLog -> EvaluationLogDialog(onDismiss = vm::dismissDialog)

            null -> Unit
        }

        // Accident acknowledge dialog queue
        val ackShowing = accidentState.ackDialogShowing
        if (ackShowing != null) {
            AccidentAckDialog(
                accident = ackShowing,
                onConfirm = accidentVm::onAckConfirm,
                onDismiss = accidentVm::onAckDismiss,
                t = t,
            )
        }

        // Accident actions dialog
        if (accidentState.actionsDialogOpen) {
            AccidentActionsDialog(
                onOpenWizard = accidentVm::openWizard,
                onVideoRecord = accidentVm::onVideoRecordOpen,
                onDismiss = accidentVm::onActionsDialogDismiss,
                t = t,
            )
        }

        // Video record screen (full-screen overlay)
        if (accidentState.videoScreenOpen) {
            VideoRecordScreen(
                recorder = accidentVm.videoRecorder,
                onUpload = { file, contentType, onProgress ->
                    accidentVm.uploadVideo(file, contentType, onProgress)
                },
                onDone = accidentVm::onVideoRecordDone,
                t = t,
            )
        }

        // Accident report wizard
        val wizardState = accidentState.wizardState
        if (accidentState.wizardOpen && wizardState != null) {
            AccidentWizard(
                wizardState = wizardState,
                onProjectSelected = accidentVm::onWizardProjectSelected,
                onNextFromProject = accidentVm::onWizardNextFromProject,
                onLocationSelected = accidentVm::onWizardLocationSelected,
                onCustomLocationToggled = accidentVm::onWizardCustomLocationToggled,
                onCustomLocationChanged = accidentVm::onWizardCustomLocationChanged,
                onNextFromLocation = accidentVm::onWizardNextFromLocation,
                onDescriptionChanged = accidentVm::onWizardDescriptionChanged,
                onNextFromDescription = accidentVm::onWizardNextFromDescription,
                onSituationSelected = accidentVm::onWizardSituationSelected,
                onNextFromSituation = accidentVm::onWizardNextFromSituation,
                onConfirmSubmit = accidentVm::onWizardConfirmSubmit,
                onBack = accidentVm::onWizardBack,
                onDismiss = accidentVm::onWizardDismiss,
                t = t,
            )
        }

        // Transport screen full-screen modal
        if (transportScreenOpen) {
            TransportScreen(
                state = transportState,
                t = t,
                onClose = {
                    transportVm.onClose()
                    transportScreenOpen = false
                },
                onAddressEditorOpen = transportVm::onAddressEditorOpen,
                onAddressEditorClose = transportVm::onAddressEditorClose,
                onEndRuaChanged = transportVm::onEndRuaChanged,
                onZipChanged = transportVm::onZipChanged,
                onAddressSubmit = transportVm::onAddressSubmit,
                onBuilderOpen = transportVm::onBuilderOpen,
                onBuilderClose = transportVm::onBuilderClose,
                onBuilderWeekdayToggled = transportVm::onBuilderWeekdayToggled,
                onBuilderDateChanged = transportVm::onBuilderDateChanged,
                onBuilderTimeChanged = transportVm::onBuilderTimeChanged,
                onBuilderSubmit = transportVm::onBuilderSubmit,
                onRequestDismiss = transportVm::onRequestDismiss,
                onMarkRealized = transportVm::onMarkRealized,
                onCancelRequest = transportVm::onCancelRequest,
                onDetailOpen = transportVm::onDetailOpen,
                onDetailClose = transportVm::onDetailClose,
                onHistoryOpen = transportVm::onHistoryOpen,
                onHistoryClose = transportVm::onHistoryClose,
                onAcknowledgeOpen = transportVm::onAcknowledgeOpen,
                onAcknowledgeClose = transportVm::onAcknowledgeClose,
                onAcknowledgeConfirm = transportVm::onAcknowledgeConfirm,
                onClearInlineMessage = transportVm::clearInlineMessage,
            )
        }
    }
    } // ProvideAccidentTheme
}
