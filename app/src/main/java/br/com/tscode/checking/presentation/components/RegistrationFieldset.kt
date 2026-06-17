package br.com.tscode.checking.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.tscode.checking.domain.model.CheckAction
import br.com.tscode.checking.presentation.theme.CheckingTextStrong

@Composable
fun RegistrationFieldset(
    selectedAction: CheckAction,
    onActionSelected: (CheckAction) -> Unit,
    onTransportOpen: () -> Unit,
    transportEnabled: Boolean,
    t: (String, Map<String, String>?) -> String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Section title ("Registro"). The automatic-activities toggle was moved to
        // Ajustes › Atividades Automáticas, so it no longer appears here.
        Text(
            text = t("registration.sectionTitle", null),
            style = MaterialTheme.typography.labelLarge,
            color = CheckingTextStrong,
        )
        Spacer(modifier = Modifier.height(8.dp))
        // When transport is enabled on at least one of the user's projects the row has
        // three columns (Check-In | Check-Out | Transporte); otherwise it collapses to
        // two equal columns (Check-In | Check-Out), matching the Informe row width.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ChoiceCard(
                label = t("registration.checkinLabel", null),
                selected = selectedAction == CheckAction.CHECKIN,
                onClick = { onActionSelected(CheckAction.CHECKIN) },
                modifier = Modifier.weight(1f),
            )
            ChoiceCard(
                label = t("registration.checkoutLabel", null),
                selected = selectedAction == CheckAction.CHECKOUT,
                onClick = { onActionSelected(CheckAction.CHECKOUT) },
                modifier = Modifier.weight(1f),
            )
            if (transportEnabled) {
                TransportChoiceCard(
                    label = t("registration.transportLabel", null),
                    onClick = onTransportOpen,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
