package com.crattendance.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Material 3 type scale with a slightly tighter body for dense tables.
val AppTypography = Typography(
    headlineMedium = Typography().headlineMedium.copy(fontWeight = FontWeight.Bold),
    titleLarge = Typography().titleLarge.copy(fontWeight = FontWeight.Bold),
    titleMedium = Typography().titleMedium.copy(fontWeight = FontWeight.SemiBold),
    bodyMedium = Typography().bodyMedium.copy(fontSize = 14.sp),
    bodySmall = Typography().bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = Typography().labelLarge.copy(fontWeight = FontWeight.SemiBold)
)
