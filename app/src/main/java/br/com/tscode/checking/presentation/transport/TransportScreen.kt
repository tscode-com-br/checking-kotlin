package br.com.tscode.checking.presentation.transport

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerSelectionMode
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.tscode.checking.domain.model.TransportRequest
import br.com.tscode.checking.domain.model.TransportRequestKind
import br.com.tscode.checking.domain.model.TransportRequestStatus
import br.com.tscode.checking.i18n.TranslateFunction
import br.com.tscode.checking.presentation.components.PrimaryButton
import br.com.tscode.checking.presentation.components.SecondaryButton
import br.com.tscode.checking.presentation.theme.CheckingAccident
import br.com.tscode.checking.presentation.theme.CheckingCardBg
import br.com.tscode.checking.presentation.theme.CheckingDivider
import br.com.tscode.checking.presentation.theme.CheckingError
import br.com.tscode.checking.presentation.theme.CheckingInputBorder
import br.com.tscode.checking.presentation.theme.CheckingOnPrimary
import br.com.tscode.checking.presentation.theme.CheckingPrimary
import br.com.tscode.checking.presentation.theme.CheckingSuccess
import br.com.tscode.checking.presentation.theme.CheckingTextMuted
import br.com.tscode.checking.presentation.theme.CheckingTextMutedSoft
import br.com.tscode.checking.presentation.theme.CheckingTextStrong
import br.com.tscode.checking.presentation.theme.CheckingSurfaceEnd
import br.com.tscode.checking.presentation.theme.CheckingSurfaceStart
import br.com.tscode.checking.presentation.theme.Tokens
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransportScreen(
    state: TransportUiState,
    t: TranslateFunction,
    onClose: () -> Unit,
    onAddressEditorOpen: () -> Unit,
    onAddressEditorClose: () -> Unit,
    onEndRuaChanged: (String) -> Unit,
    onZipChanged: (String) -> Unit,
    onAddressSubmit: () -> Unit,
    onBuilderOpen: (TransportRequestKind) -> Unit,
    onBuilderClose: () -> Unit,
    onBuilderWeekdayToggled: (Int) -> Unit,
    onBuilderDateChanged: (String) -> Unit,
    onBuilderTimeChanged: (String) -> Unit,
    onBuilderSubmit: () -> Unit,
    onRequestDismiss: (Int) -> Unit,
    onMarkRealized: (Int) -> Unit,
    onCancelRequest: (Int) -> Unit,
    onDetailOpen: (Int) -> Unit,
    onDetailClose: () -> Unit,
    onHistoryOpen: () -> Unit,
    onHistoryClose: () -> Unit,
    onAcknowledgeOpen: (Int) -> Unit,
    onAcknowledgeClose: () -> Unit,
    onAcknowledgeConfirm: () -> Unit,
    onClearInlineMessage: () -> Unit,
) {
    // BackHandler: detail > history > builder > screen
    BackHandler {
        when {
            state.detailRequestId != null -> onDetailClose()
            state.historyOpen -> onHistoryClose()
            state.builderState != null -> onBuilderClose()
            state.addressEditorOpen -> onAddressEditorClose()
            else -> onClose()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(CheckingSurfaceStart, CheckingSurfaceEnd))
            ),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            TransportHeader(
                title = t("transport.title", null),
                onBack = onClose,
                t = t,
            )

            // Scrollable content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Tokens.cardPaddingSmall, vertical = Tokens.sectionGap),
                verticalArrangement = Arrangement.spacedBy(Tokens.sectionGap),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = CheckingPrimary)
                }

                // Inline status message
                if (state.inlineMessage.isNotBlank()) {
                    LaunchedEffect(state.inlineMessage) {
                        kotlinx.coroutines.delay(4_000)
                        onClearInlineMessage()
                    }
                    InlineStatusRow(
                        message = state.inlineMessage,
                        tone = state.inlineMessageTone,
                    )
                }

                // Address section
                AddressSummaryRow(
                    endRua = state.endRua,
                    zip = state.zip,
                    onEdit = onAddressEditorOpen,
                    t = t,
                )

                if (state.addressEditorOpen) {
                    AddressEditorSection(
                        endRuaInput = state.endRuaInput,
                        zipInput = state.zipInput,
                        isSaving = state.isAddressSaving,
                        canSubmit = state.canAddressSubmit,
                        onEndRuaChanged = onEndRuaChanged,
                        onZipChanged = onZipChanged,
                        onSubmit = onAddressSubmit,
                        onCancel = onAddressEditorClose,
                        t = t,
                    )
                }

                HorizontalDivider(color = CheckingDivider)

                // Kind selector
                if (state.builderState == null) {
                    KindSelectorRow(
                        hasAddress = state.hasAddress,
                        onKindSelected = onBuilderOpen,
                        t = t,
                    )
                }

                // Builder form
                if (state.builderState != null) {
                    RequestBuilderForm(
                        builderState = state.builderState,
                        canSubmit = state.canSubmitBuilder,
                        isSubmitting = state.isRequestSubmitting,
                        onWeekdayToggled = onBuilderWeekdayToggled,
                        onDateChanged = onBuilderDateChanged,
                        onTimeChanged = onBuilderTimeChanged,
                        onSubmit = onBuilderSubmit,
                        onCancel = onBuilderClose,
                        t = t,
                    )
                }

                // Active requests list — only the 3 most recent (newest first)
                val latestRequests = state.visibleRequests
                    .sortedByDescending { it.requestId }
                    .take(3)
                if (latestRequests.isNotEmpty()) {
                    SectionLabel(t("transport.historyTitle", null))
                    latestRequests.forEach { req ->
                        TransportRequestCard(
                            request = req,
                            isCancelling = state.cancellingIds.contains(req.requestId),
                            onTap = { onDetailOpen(req.requestId) },
                            onDismiss = { onRequestDismiss(req.requestId) },
                            onMarkRealized = { onMarkRealized(req.requestId) },
                            onCancel = { onCancelRequest(req.requestId) },
                            onAcknowledge = { onAcknowledgeOpen(req.requestId) },
                            t = t,
                        )
                    }
                }

                // "Histórico" button — moved here from the header; opens the full list.
                if (state.allRequests.isNotEmpty()) {
                    SecondaryButton(
                        text = t("transport.historyButtonLabel", null),
                        onClick = onHistoryOpen,
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // Detail overlay
        val detailRequest = state.detailRequest
        if (state.detailRequestId != null && detailRequest != null) {
            RequestDetailOverlay(
                request = detailRequest,
                onDismiss = onDetailClose,
                t = t,
            )
        }

        // History panel
        if (state.historyOpen) {
            HistoryPanel(
                requests = state.allRequests,
                onDismiss = onHistoryClose,
                onRequestTap = { onDetailOpen(it) },
                t = t,
            )
        }

        // Acknowledge dialog
        if (state.acknowledgeRequestId != null) {
            AcknowledgeDialog(
                onDismiss = onAcknowledgeClose,
                onConfirm = onAcknowledgeConfirm,
                t = t,
            )
        }
    }
}

// ---- Sub-composables ----

@Composable
private fun TransportHeader(
    title: String,
    onBack: () -> Unit,
    t: TranslateFunction,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CheckingPrimary)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = t("transport.backToMainAria", null),
                tint = CheckingOnPrimary,
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = CheckingOnPrimary,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun AddressSummaryRow(
    endRua: String,
    zip: String,
    onEdit: () -> Unit,
    t: TranslateFunction,
) {
    TransportCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = t("transport.addressLabel", null),
                    style = MaterialTheme.typography.labelSmall,
                    color = CheckingTextMuted,
                )
                val summary = if (endRua.isNotBlank() && zip.isNotBlank()) "$endRua • $zip"
                else t("transport.addressPlaceholder", null)
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CheckingTextStrong,
                )
            }
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = t("transport.editAddressAria", null),
                    tint = CheckingPrimary,
                )
            }
        }
    }
}

@Composable
private fun AddressEditorSection(
    endRuaInput: String,
    zipInput: String,
    isSaving: Boolean,
    canSubmit: Boolean,
    onEndRuaChanged: (String) -> Unit,
    onZipChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
    t: TranslateFunction,
) {
    TransportCard {
        Column(verticalArrangement = Arrangement.spacedBy(Tokens.itemGap)) {
            OutlinedTextField(
                value = endRuaInput,
                onValueChange = onEndRuaChanged,
                label = { Text(t("transport.addressToggleLabel", null)) },
                placeholder = { Text(t("transport.addressPlaceholder", null)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = zipInput,
                onValueChange = onZipChanged,
                label = { Text(t("transport.zipLabel", null)) },
                placeholder = { Text(t("transport.zipPlaceholder", null)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Tokens.itemGap),
            ) {
                SecondaryButton(
                    text = t("transport.addressBackButton", null),
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                )
                PrimaryButton(
                    text = if (isSaving) "..." else t("transport.addressSubmitButton", null),
                    onClick = onSubmit,
                    enabled = canSubmit && !isSaving,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun KindSelectorRow(
    hasAddress: Boolean,
    onKindSelected: (TransportRequestKind) -> Unit,
    t: TranslateFunction,
) {
    if (!hasAddress) {
        Text(
            text = t("transport.requestBuilder.addressRequired", null),
            style = MaterialTheme.typography.bodySmall,
            color = CheckingTextMuted,
        )
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(Tokens.itemGap)) {
        Text(
            text = t("transport.optionInstruction", null),
            style = MaterialTheme.typography.bodySmall,
            color = CheckingTextMuted,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Tokens.itemGap),
        ) {
            TransportRequestKind.entries.forEach { kind ->
                val containerColor = when (kind) {
                    TransportRequestKind.REGULAR -> CheckingPrimary        // green (kept)
                    TransportRequestKind.WEEKEND -> Color(0xFFEA580C)      // orange
                    TransportRequestKind.EXTRA -> CheckingAccident         // red
                }
                Button(
                    onClick = { onKindSelected(kind) },
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = containerColor,
                        contentColor = CheckingOnPrimary,
                    ),
                ) {
                    Text(
                        text = t("transport.kinds.${kind.name.lowercase()}", null),
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RequestBuilderForm(
    builderState: TransportBuilderState,
    canSubmit: Boolean,
    isSubmitting: Boolean,
    onWeekdayToggled: (Int) -> Unit,
    onDateChanged: (String) -> Unit,
    onTimeChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
    t: TranslateFunction,
) {
    TransportCard {
        Column(verticalArrangement = Arrangement.spacedBy(Tokens.sectionGap)) {
            Text(
                text = when (builderState.kind) {
                    TransportRequestKind.REGULAR -> t("transport.requestBuilder.regularSubtitle", null)
                    TransportRequestKind.WEEKEND -> t("transport.requestBuilder.weekendSubtitle", null)
                    TransportRequestKind.EXTRA -> t("transport.requestBuilder.extraSubtitle", null)
                },
                style = MaterialTheme.typography.bodySmall,
                color = CheckingTextMuted,
            )

            // Weekday chips (REGULAR or WEEKEND)
            if (builderState.kind != TransportRequestKind.EXTRA) {
                val availableDays = if (builderState.kind == TransportRequestKind.REGULAR)
                    (0..4).toList() else listOf(5, 6)
                Text(
                    text = t("transport.requestBuilder.selectDaysLabel", null),
                    style = MaterialTheme.typography.labelSmall,
                    color = CheckingTextMuted,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    availableDays.forEach { day ->
                        val selected = builderState.selectedWeekdays.contains(day)
                        FilterChip(
                            selected = selected,
                            onClick = { onWeekdayToggled(day) },
                            label = { Text(t("transport.weekdays.short.$day", null)) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CheckingPrimary,
                                selectedLabelColor = CheckingOnPrimary,
                            ),
                        )
                    }
                }
            }

            // Date + time pickers — Transporte Extra only. Dias/Fins de Semana follow the
            // regular work schedule, so no time (or date) is requested there.
            if (builderState.kind == TransportRequestKind.EXTRA) {
                var showDatePicker by remember { mutableStateOf(false) }
                var showTimePicker by remember { mutableStateOf(false) }

                val displayDate = runCatching {
                    LocalDate.parse(builderState.requestedDate)
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                }.getOrDefault(builderState.requestedDate)

                PickerField(
                    label = t("transport.requestBuilder.dateLabel", null),
                    value = displayDate,
                    placeholder = "",
                    leadingIcon = Icons.Outlined.DateRange,
                    onClick = { showDatePicker = true },
                )

                PickerField(
                    label = t("transport.requestBuilder.timeLabel", null),
                    value = builderState.requestedTime,
                    placeholder = t("transport.requestBuilder.timePlaceholder", null),
                    leadingIcon = Icons.Outlined.Schedule,
                    onClick = { showTimePicker = true },
                )

                if (showDatePicker) {
                    val initialMillis = runCatching {
                        LocalDate.parse(builderState.requestedDate)
                            .atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
                    }.getOrNull()
                    val dateState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                dateState.selectedDateMillis?.let { millis ->
                                    val picked = Instant.ofEpochMilli(millis)
                                        .atZone(ZoneId.of("UTC")).toLocalDate()
                                    onDateChanged(picked.toString())
                                }
                                showDatePicker = false
                            }) { Text(t("transport.requestBuilder.pickerConfirm", null)) }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) {
                                Text(t("transport.requestBuilder.pickerCancel", null))
                            }
                        },
                    ) {
                        DatePicker(state = dateState)
                    }
                }

                if (showTimePicker) {
                    val initialParts = builderState.requestedTime.split(":")
                    val initialHour = initialParts.getOrNull(0)?.toIntOrNull() ?: 8
                    val initialMinute = initialParts.getOrNull(1)?.toIntOrNull() ?: 0
                    val timeState = rememberTimePickerState(
                        initialHour = initialHour,
                        initialMinute = initialMinute,
                        is24Hour = true,
                    )
                    AlertDialog(
                        onDismissRequest = { showTimePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                onTimeChanged("%02d:%02d".format(timeState.hour, timeState.minute))
                                showTimePicker = false
                            }) { Text(t("transport.requestBuilder.pickerConfirm", null)) }
                        },
                        dismissButton = {
                            TextButton(onClick = { showTimePicker = false }) {
                                Text(t("transport.requestBuilder.pickerCancel", null))
                            }
                        },
                        text = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(Tokens.itemGap),
                            ) {
                                TimePicker(state = timeState)
                                // "Voltar para a hora" — returns the dial to hour selection
                                // so the user can fix a wrong hour without cancelling.
                                SecondaryButton(
                                    text = t("transport.requestBuilder.backToHour", null),
                                    onClick = { timeState.selection = TimePickerSelectionMode.Hour },
                                )
                            }
                        },
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Tokens.itemGap),
            ) {
                SecondaryButton(
                    text = t("transport.requestBuilder.backButton", null),
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                )
                PrimaryButton(
                    text = if (isSubmitting) "..." else t("transport.requestBuilder.submitButton", null),
                    onClick = onSubmit,
                    enabled = canSubmit && !isSubmitting,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

// Read-only field that opens a picker dialog on tap (date / time). Styled like the other
// form fields: a bold label above a bordered, clickable box.
@Composable
private fun PickerField(
    label: String,
    value: String,
    placeholder: String,
    leadingIcon: ImageVector,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(Tokens.controlRadius)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = CheckingTextMuted,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = Tokens.controlHeight)
                .clip(shape)
                .background(CheckingCardBg)
                .border(BorderStroke(1.dp, CheckingInputBorder), shape)
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = CheckingTextMuted,
            )
            Text(
                text = value.ifBlank { placeholder },
                style = MaterialTheme.typography.bodyLarge,
                color = if (value.isBlank()) CheckingTextMutedSoft else CheckingTextStrong,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransportRequestCard(
    request: TransportRequest,
    isCancelling: Boolean,
    onTap: () -> Unit,
    onDismiss: () -> Unit,
    onMarkRealized: () -> Unit,
    onCancel: () -> Unit,
    onAcknowledge: () -> Unit,
    t: TranslateFunction,
) {
    val canDismiss = request.status == TransportRequestStatus.REALIZED ||
            request.status == TransportRequestStatus.CANCELLED ||
            request.status == TransportRequestStatus.REJECTED

    if (canDismiss) {
        val dismissState = rememberSwipeToDismissBoxState()
        LaunchedEffect(dismissState.currentValue) {
            if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                onDismiss()
            }
        }
        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CheckingError.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    Text(
                        text = "✕",
                        color = CheckingError,
                        modifier = Modifier.padding(end = 16.dp),
                    )
                }
            },
        ) {
            RequestCardContent(
                request = request,
                isCancelling = isCancelling,
                onTap = onTap,
                onMarkRealized = onMarkRealized,
                onCancel = onCancel,
                onAcknowledge = onAcknowledge,
                t = t,
            )
        }
    } else {
        RequestCardContent(
            request = request,
            isCancelling = isCancelling,
            onTap = onTap,
            onMarkRealized = onMarkRealized,
            onCancel = onCancel,
            onAcknowledge = onAcknowledge,
            t = t,
        )
    }
}

@Composable
private fun RequestCardContent(
    request: TransportRequest,
    isCancelling: Boolean,
    onTap: () -> Unit,
    onMarkRealized: () -> Unit,
    onCancel: () -> Unit,
    onAcknowledge: () -> Unit,
    t: TranslateFunction,
) {
    TransportCard(
        modifier = Modifier.clickable(onClick = onTap),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Tokens.itemGap)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = t("transport.kinds.${request.requestKind.name.lowercase()}", null),
                    style = MaterialTheme.typography.titleSmall,
                    color = CheckingTextStrong,
                    fontWeight = FontWeight.Bold,
                )
                StatusBadge(status = request.status, t = t)
            }

            // Date/time info
            val dateStr = request.serviceDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            val weekdays = request.selectedWeekdays.joinToString(", ") {
                t("transport.weekdays.short.$it", null)
            }
            val dateDisplay = when {
                dateStr != null -> dateStr
                weekdays.isNotBlank() -> weekdays
                else -> null
            }
            if (dateDisplay != null) {
                Text(
                    text = dateDisplay,
                    style = MaterialTheme.typography.bodySmall,
                    color = CheckingTextMuted,
                )
            }

            // Boarding time
            if (request.boardingTime != null) {
                Text(
                    text = request.boardingTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = CheckingTextMuted,
                )
            }

            // Acknowledge button
            if (request.awarenessRequired && !request.awarenessConfirmed) {
                SecondaryButton(
                    text = "Ciente",
                    onClick = onAcknowledge,
                )
            }

            // Action buttons
            val canRealize = request.status == TransportRequestStatus.CONFIRMED && request.isActive
            val canCancel = (request.status == TransportRequestStatus.PENDING ||
                    request.status == TransportRequestStatus.CONFIRMED) && request.isActive

            if (canRealize || canCancel) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Tokens.itemGap),
                ) {
                    if (canRealize) {
                        SecondaryButton(
                            text = t("transport.actions.markRealized", null),
                            onClick = onMarkRealized,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (canCancel) {
                        SecondaryButton(
                            text = if (isCancelling) t("transport.actions.cancelling", null)
                            else t("transport.actions.cancel", null),
                            onClick = onCancel,
                            enabled = !isCancelling,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: TransportRequestStatus, t: TranslateFunction) {
    val (bgColor, textColor) = when (status) {
        TransportRequestStatus.PENDING -> Color(0xFFFEF3C7) to Color(0xFF92400E)
        TransportRequestStatus.CONFIRMED -> Color(0xFFD1FAE5) to Color(0xFF065F46)
        TransportRequestStatus.REALIZED -> Color(0xFFDBEAFE) to Color(0xFF1E40AF)
        TransportRequestStatus.REJECTED -> Color(0xFFFEE2E2) to CheckingError
        TransportRequestStatus.CANCELLED -> Color(0xFFF1F5F9) to CheckingTextMuted
    }
    Text(
        text = t("transport.statusLabels.${status.name.lowercase()}", null),
        style = MaterialTheme.typography.labelSmall,
        color = textColor,
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

@Composable
private fun RequestDetailOverlay(
    request: TransportRequest,
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
                .fillMaxWidth(0.92f)
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
                Text(
                    text = t("transport.detail.title", null),
                    style = MaterialTheme.typography.titleMedium,
                    color = CheckingTextStrong,
                )
                DetailRow(t("transport.detail.vehicleTypeLabel", null), request.vehicleType?.name ?: t("transport.detail.unavailableValue", null))
                if (request.vehiclePlate != null) DetailRow(t("transport.detail.vehiclePlateLabel", null), request.vehiclePlate)
                if (request.vehicleColor != null) DetailRow(t("transport.detail.vehicleColorLabel", null), request.vehicleColor)
                if (request.boardingTime != null) DetailRow(t("transport.detail.departureTimeLabel", null), request.boardingTime)
                if (request.responseMessage != null) {
                    Text(
                        text = request.responseMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = CheckingTextMuted,
                    )
                }
                SecondaryButton(text = t("transport.historyCloseButton", null), onClick = onDismiss)
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = CheckingTextMuted)
        Text(value, style = MaterialTheme.typography.bodySmall, color = CheckingTextStrong)
    }
}

@Composable
private fun HistoryPanel(
    requests: List<TransportRequest>,
    onDismiss: () -> Unit,
    onRequestTap: (Int) -> Unit,
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
        contentAlignment = Alignment.CenterEnd,
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize(0.85f)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) {},
            shape = RoundedCornerShape(topStart = Tokens.cardRadius, bottomStart = Tokens.cardRadius),
            colors = CardDefaults.cardColors(containerColor = CheckingCardBg),
        ) {
            Column(
                modifier = Modifier.padding(Tokens.cardPadding),
                verticalArrangement = Arrangement.spacedBy(Tokens.sectionGap),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = t("transport.historyPanelTitle", null),
                        style = MaterialTheme.typography.titleMedium,
                        color = CheckingTextStrong,
                    )
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = CheckingPrimary),
                    ) {
                        Text(t("transport.historyCloseButton", null), color = CheckingOnPrimary)
                    }
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(Tokens.itemGap),
                ) {
                    if (requests.isEmpty()) {
                        Text(
                            text = t("transport.summary.noRequestRecorded", null),
                            style = MaterialTheme.typography.bodyMedium,
                            color = CheckingTextMuted,
                        )
                    } else {
                        requests.forEach { req ->
                            HistoryRequestRow(req, onTap = { onRequestTap(req.requestId) }, t = t)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryRequestRow(
    request: TransportRequest,
    onTap: () -> Unit,
    t: TranslateFunction,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTap)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = t("transport.kinds.${request.requestKind.name.lowercase()}", null),
                style = MaterialTheme.typography.bodyMedium,
                color = CheckingTextStrong,
            )
            val dateStr = request.serviceDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            if (dateStr != null) {
                Text(dateStr, style = MaterialTheme.typography.bodySmall, color = CheckingTextMuted)
            }
        }
        StatusBadge(status = request.status, t = t)
    }
}

@Composable
private fun AcknowledgeDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
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
                    text = t("transport.detail.confirmed", null),
                    style = MaterialTheme.typography.titleMedium,
                    color = CheckingTextStrong,
                )
                PrimaryButton(text = "Ciente", onClick = onConfirm)
                SecondaryButton(text = t("transport.historyCloseButton", null), onClick = onDismiss)
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = CheckingTextMuted,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun InlineStatusRow(message: String, tone: TransportNotificationTone) {
    val color = when (tone) {
        TransportNotificationTone.Success -> CheckingSuccess
        TransportNotificationTone.Error -> CheckingError
        TransportNotificationTone.Neutral -> CheckingTextMuted
    }
    Text(
        text = message,
        style = MaterialTheme.typography.bodySmall,
        color = color,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
fun TransportCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
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
            Box(modifier = Modifier.padding(Tokens.cardPaddingSmall)) {
                content()
            }
        }
    }
}
