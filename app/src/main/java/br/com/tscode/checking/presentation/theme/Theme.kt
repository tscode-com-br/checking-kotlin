package br.com.tscode.checking.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ===== Normal color scheme (§14.1) =====

private val CheckingColorScheme = lightColorScheme(
    primary = CheckingPrimary,
    onPrimary = CheckingOnPrimary,
    primaryContainer = CheckingAccentBgSoft,
    onPrimaryContainer = CheckingPrimaryDark,
    secondary = CheckingTeal,
    onSecondary = CheckingOnPrimary,
    secondaryContainer = CheckingTealLight,
    onSecondaryContainer = CheckingTeal,
    surface = CheckingSurfaceStart,
    onSurface = CheckingTextStrong,
    surfaceVariant = CheckingInputBg,
    onSurfaceVariant = CheckingTextMuted,
    background = CheckingSurfaceStart,
    onBackground = CheckingTextStrong,
    error = CheckingError,
    onError = CheckingOnPrimary,
    outline = CheckingInputBorder,
    outlineVariant = CheckingDivider,
)

// ===== Accident mode overlay (§13.8 / §14.3) =====
// When an accident is active, the UI switches to a red-accented emergency theme.

private val AccidentColorScheme = lightColorScheme(
    primary = CheckingAccident,
    onPrimary = CheckingOnPrimary,
    primaryContainer = Color(0xFFFDE7E9),
    onPrimaryContainer = Color(0xFF8C1A20),
    secondary = CheckingAccident,
    onSecondary = CheckingOnPrimary,
    surface = CheckingSurfaceStart,
    onSurface = CheckingTextStrong,
    background = CheckingSurfaceStart,
    onBackground = CheckingTextStrong,
    error = CheckingError,
    onError = CheckingOnPrimary,
)

// CompositionLocal for accident-mode state, consumed by components that need to adapt visuals.
val LocalAccidentModeActive = staticCompositionLocalOf { false }

@Composable
fun CheckingTheme(
    accidentModeActive: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (accidentModeActive) AccidentColorScheme else CheckingColorScheme

    CompositionLocalProvider(LocalAccidentModeActive provides accidentModeActive) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = CheckingTypography,
            content = content,
        )
    }
}
