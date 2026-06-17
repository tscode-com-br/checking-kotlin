package br.com.tscode.checking.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import br.com.tscode.checking.domain.model.LocationMatch
import br.com.tscode.checking.domain.model.MatchStatus
import br.com.tscode.checking.i18n.TranslateFunction
import br.com.tscode.checking.presentation.theme.CheckingLocationError
import br.com.tscode.checking.presentation.theme.CheckingLocationMuted
import br.com.tscode.checking.presentation.theme.CheckingLocationSuccess
import br.com.tscode.checking.presentation.theme.CheckingTeal
import br.com.tscode.checking.presentation.theme.CheckingTextMutedLight
import br.com.tscode.checking.presentation.theme.CheckingTextStrong
import br.com.tscode.checking.presentation.theme.Tokens
import java.util.Locale

@Composable
fun LocationCard(
    locationMatch: LocationMatch?,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    t: TranslateFunction,
    modifier: Modifier = Modifier,
) {
    val accuracyText = locationMatch?.accuracyMeters?.let {
        t("location.accuracyTemplate", mapOf("accuracy" to "±%.0f m".format(Locale.US, it)))
    }
    val valueColor = when {
        locationMatch == null -> CheckingLocationMuted
        locationMatch.matched -> CheckingLocationSuccess
        locationMatch.status == MatchStatus.ACCURACY_TOO_LOW -> CheckingLocationError
        else -> CheckingTextStrong
    }
    val valueText = locationMatch?.label ?: t("location.waitingLabel", null)

    TintedPanel(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header: "Local" label + accuracy
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = t("location.title", null).uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.labelSmall,
                    color = CheckingTextMutedLight,
                )
                Text(
                    text = accuracyText ?: "--",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (locationMatch?.status == MatchStatus.ACCURACY_TOO_LOW) {
                        CheckingLocationError
                    } else {
                        CheckingTextMutedLight
                    },
                )
            }

            // Main row: location value + refresh pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = valueColor,
                    modifier = Modifier.weight(1f),
                )
                RefreshPill(isLoading = isLoading, onRefresh = onRefresh, t = t)
            }
        }
    }
}

@Composable
private fun RefreshPill(
    isLoading: Boolean,
    onRefresh: () -> Unit,
    t: TranslateFunction,
) {
    val shape = RoundedCornerShape(Tokens.controlRadius)
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(shape)
            .background(CheckingTeal.copy(alpha = 0.08f))
            .border(BorderStroke(1.dp, CheckingTeal.copy(alpha = 0.24f)), shape)
            .clickable(enabled = !isLoading, onClick = onRefresh),
        contentAlignment = Alignment.Center,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = CheckingTeal,
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.Refresh,
                contentDescription = t("location.refreshLabel", null),
                tint = CheckingTeal,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
