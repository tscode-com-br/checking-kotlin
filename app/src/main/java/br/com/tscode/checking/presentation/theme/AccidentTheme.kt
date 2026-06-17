package br.com.tscode.checking.presentation.theme

import androidx.compose.runtime.Composable

@Composable
fun ProvideAccidentTheme(active: Boolean, content: @Composable () -> Unit) {
    CheckingTheme(accidentModeActive = active, content = content)
}
