package br.com.tscode.checking.presentation.theme

import androidx.compose.ui.graphics.Color

// ===== Brand palette (extracted from styles.css §14.1) =====

val CheckingPrimary = Color(0xFF0F766E)      // teal — normal primary
val CheckingPrimaryDark = Color(0xFF115E59)  // darker teal — gradient end / hover
val CheckingAccentBgSoft = Color(0xFFCCFBF1) // teal-light — primaryContainer

val CheckingTeal = Color(0xFF0F766E)
val CheckingTealLight = Color(0xFFCCFBF1)

val CheckingAccident = Color(0xFFC8222A)     // accident/emergency red

val CheckingTextStrong = Color(0xFF0F172A)
val CheckingTextStrongAlt = Color(0xFF1F2937)
val CheckingTextMuted = Color(0xFF475569)
val CheckingTextMutedLight = Color(0xFF64748B)
val CheckingTextMutedSoft = Color(0xFF94A3B8)

val CheckingSuccess = Color(0xFF166534)
val CheckingWarning = Color(0xFF92400E)
val CheckingError = Color(0xFFB42318)
val CheckingErrorVivid = Color(0xFFFF0000)

// ===== Activities log severity text colors (plan004 §3.1 — color only) =====
// SUCCESS=green / FAILURE=red reuse CheckingSuccess / CheckingErrorVivid; WARNING=orange + INFO=dark blue:
val CheckingActivityWarning = Color(0xFFEA580C)  // orange-600 — WARNING rows
val CheckingActivityInfo = Color(0xFF1E40AF)     // blue-800 (dark blue) — INFO rows

val CheckingSurfaceStart = Color(0xFFF7F8FA)
val CheckingSurfaceEnd = Color(0xFFEEF2F7)
val CheckingHeaderBg = Color(0xFF0F766E)
val CheckingOnPrimary = Color(0xFFFFFFFF)

val CheckingCardBg = Color(0xFFFFFFFF)
val CheckingCardTint = Color(0xFFF8FAFC)     // rgba(248,250,252,0.9) — inner card tint (history/location/notification)
val CheckingDivider = Color(0xFFE2E8F0)
val CheckingInputBg = Color(0xFFF8FAFC)
val CheckingInputBorder = Color(0xFFCBD5E1)

// ===== Auth field glow (styles.css §auth-field-pending / §auth-field-authenticated) =====
// The colored "brilho" around the chave/senha fields:
//   pending      → orange (key recognized, awaiting login)
//   authenticated → green (logged in)
val CheckingFieldPendingBorder = Color(0xFFF97316)  // orange-500
val CheckingFieldPendingGlow = Color(0xFFFB923C)    // orange-400 (glow tint)
val CheckingFieldAuthedBorder = Color(0xFF16A34A)   // green-600
val CheckingFieldAuthedGlow = Color(0xFF22C55E)     // green-500 (glow tint)

// ===== Choice / transport accents =====
val CheckingChoiceSelectedBg = Color(0xFFE6F2F0)    // rgba(15,118,110,0.08) over white
val TransportChoiceBgStart = Color(0xFF9ED8FF)
val TransportChoiceBgEnd = Color(0xFF6BBDFF)
val TransportChoiceBorder = Color(0xFF7DC8FF)

// ===== History latest-activity highlight (§history-item.is-latest-activity) =====
val CheckingLatestBorder = Color(0xFF16A34A)
val CheckingLatestBg = Color(0xFFDCFCE7)            // rgba(220,252,231,0.76)

// ===== Status text colors for location value (§location-value.is-*) =====
val CheckingLocationSuccess = Color(0xFF0F766E)
val CheckingLocationError = Color(0xFFB42318)
val CheckingLocationMuted = Color(0xFF94A3B8)

// ===== Accident mode (§14.1 — row accent colors, used if situation table is surfaced) =====

val AccidentRowRed = Color(0xFFFF0000)
val AccidentRowYellow = Color(0xFFFFFF00)
val AccidentRowTurquoise = Color(0xFF00CED1)
val AccidentRowLightGreen = Color(0xFF90EE90)
val AccidentRowLightGray = Color(0xFFD3D3D3)
val AccidentRowLightBlue = Color(0xFFADD8E6)
