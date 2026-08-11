package com.crattendance.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Performance note ─────────────────────────────────────────────────────────
// Typography() allocates a full M3 type scale (18 TextStyle instances) on each
// call.  Create exactly ONE shared baseline and copy from it — eliminates the
// 5 redundant allocations that the previous version incurred at startup.
private val base = Typography()

val AppTypography = Typography(
    // Slightly heavier weight for prominence on the home and detail screens.
    headlineMedium = base.headlineMedium.copy(fontWeight = FontWeight.Bold),
    titleLarge     = base.titleLarge.copy(fontWeight = FontWeight.Bold),
    titleMedium    = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    // Slightly tighter body for dense attendance tables.
    bodyMedium     = base.bodyMedium.copy(fontSize = 14.sp),
    bodySmall      = base.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge     = base.labelLarge.copy(fontWeight = FontWeight.SemiBold)
)
