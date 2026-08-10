package com.crattendance.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crattendance.utils.DateUtils
import java.time.LocalDate
import java.util.Locale

/**
 * Horizontally scrolling date strip. Each day is a pill/rounded-rectangle
 * with the 3-letter abbreviation on top and the day number below.
 * The selected day gets the primary colour background (light blue tint like the
 * reference image). Auto-scrolls the selected item into view.
 */
@Composable
fun DateSelector(
    weekDays: List<LocalDate>,
    selectedDate: LocalDate,
    onSelect: (LocalDate) -> Unit
) {
    val listState = rememberLazyListState()
    LaunchedEffect(selectedDate) {
        val index = weekDays.indexOf(selectedDate)
        if (index >= 0) listState.animateScrollToItem(
            index = (index - 2).coerceAtLeast(0)
        )
    }

    LazyRow(
        state = listState,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(weekDays.size) { i ->
            DayCell(
                date = weekDays[i],
                isSelected = weekDays[i] == selectedDate,
                isToday = weekDays[i] == LocalDate.now(),
                onClick = { onSelect(weekDays[i]) }
            )
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val dayAbbr = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.ENGLISH)
        .take(3) // "Mon", "Tue", etc.

    // Selected: light periwinkle/blue similar to the screenshot
    val selectedBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
    val normalBg   = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    val bg         = if (isSelected) selectedBg else normalBg

    val selectedTextColor = MaterialTheme.colorScheme.primary
    val normalTextColor   = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .width(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Day abbreviation (Mon, Tue …)
            Text(
                text = dayAbbr,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) selectedTextColor else normalTextColor
            )
            // Day number
            Text(
                text = date.dayOfMonth.toString(),
                fontSize = 18.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) selectedTextColor else MaterialTheme.colorScheme.onSurface
            )
            // Today indicator dot
            Box(Modifier.height(6.dp), contentAlignment = Alignment.Center) {
                if (isToday) {
                    Box(
                        Modifier
                            .size(5.dp)
                            .clip(RoundedCornerShape(50))
                            .background(if (isSelected) selectedTextColor else MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }
}

/** Shown above the calendar picker so the user knows the current selection. */
@Composable
fun SelectedDateLabel(date: LocalDate) {
    Text(
        DateUtils.formatLong(date),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
