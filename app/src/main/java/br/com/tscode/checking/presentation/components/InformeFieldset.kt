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
import br.com.tscode.checking.presentation.check.UiInformeType
import br.com.tscode.checking.presentation.theme.CheckingTextStrong

@Composable
fun InformeFieldset(
    selected: UiInformeType,
    onSelected: (UiInformeType) -> Unit,
    t: (String, Map<String, String>?) -> String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = t("registration.informeTitle", null),
            style = MaterialTheme.typography.labelLarge,
            color = CheckingTextStrong,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ChoiceCard(
                label = t("registration.informeNormalLabel", null),
                selected = selected == UiInformeType.NORMAL,
                onClick = { onSelected(UiInformeType.NORMAL) },
                modifier = Modifier.weight(1f),
            )
            ChoiceCard(
                label = t("registration.informeRetroativoLabel", null),
                selected = selected == UiInformeType.RETROATIVO,
                onClick = { onSelected(UiInformeType.RETROATIVO) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}
