package com.crattendance.ui.theme

import androidx.compose.ui.graphics.Color

// ─── Primary — Professional Blue ────────────────────────────────────────────
// Unchanged from before; good contrast and brand recognition.
val BluePrimary          = Color(0xFF1B5E9E)   // light primary
val BluePrimaryDark      = Color(0xFF3E88C5)   // dark primary
val BlueContainer        = Color(0xFFD6E5F7)   // light primaryContainer
val BlueOnContainer      = Color(0xFF0B3B66)   // light onPrimaryContainer / dark primaryContainer
val BlueSecondary        = Color(0xFF395F83)   // light secondary (same hue, lighter)

// ─── Secondary — Teal (distinct hue, M3-compliant) ──────────────────────────
// A separate hue is required by the M3 tonal palette spec. Teal provides clear
// differentiation from the blue primary while staying cool and academic.
val TealSecondary        = Color(0xFF006874)   // light secondary
val TealSecondaryDark    = Color(0xFF4DD8E8)   // dark secondary
val TealContainer        = Color(0xFF97F0FF)   // light secondaryContainer
val TealOnContainer      = Color(0xFF001F24)   // light onSecondaryContainer
val TealContainerDark    = Color(0xFF004F58)   // dark secondaryContainer
val TealOnContainerDark  = Color(0xFF97F0FF)   // dark onSecondaryContainer

// ─── Tertiary — Golden Yellow (accent) ──────────────────────────────────────
val GoldenYellow         = Color(0xFFFFCA12)
val GoldenContainer      = Color(0xFFFFF3C4)
val GoldenOnContainer    = Color(0xFF5A4500)
val GoldenContainerDark  = Color(0xFF6B5400)

// ─── Neutral surfaces ────────────────────────────────────────────────────────
val Navy                 = Color(0xFF14324F)
val OffWhite             = Color(0xFFF8FAFC)
val SurfaceDark          = Color(0xFF111318)

// ─── Semantic ────────────────────────────────────────────────────────────────
val SuccessGreen         = Color(0xFF2E7D32)
val SuccessGreenDark     = Color(0xFF81C784)
val ErrorRed             = Color(0xFFBA1A1A)
val ErrorRedDark         = Color(0xFFFFB4AB)
