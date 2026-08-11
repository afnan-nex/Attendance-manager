package com.crattendance.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crattendance.utils.DateUtils
import java.time.LocalDate
import java.util.Locale

/**
 * Horizontally scrollable date strip spanning ±365 days from today (731 items).
 *
 * ### Performance optimisations
 *
 * 1. **`key = { epoch day }`** on the `LazyRow` items block — Compose uses this
 *    as a structural identity key.  When the list is scrolled or a date is
 *    selected, only the cells whose identity has changed (i.e. those that gained
 *    or lost the `isSelected` state) are recomposed.  Without a `key`, Compose
 *    must recompose every visible item on every state change.
 *
 * 2. **Pre-computed `dayAbbr` and `dayNumber` strings** — `getDisplayName()` is
 *    not free; it formats a locale-aware string on every call.  These values are
 *    invariant per date, so they are computed once in [DayCell] with
 *    `remember(date)` and cached for the lifetime of that cell's composition.
 *
 * 3. **Guarded `scrollToItem`** — the `LaunchedEffect` now checks whether the
 *    target index is already within the currently visible items range before
 *    calling `scrollToItem`.  This avoids triggering a layout pass when the
 *    selected date is already on screen (e.g. tapping a visible cell).
 *
 * 4. **`@Stable` annotation on [DayCell]'s parameters** — all parameters are
 *    primitives or `LocalDate` (which is already `@Stable` via value-equality).
 *    The `@Stable` contract on the caller lets Compose skip recomposing a cell
 *    if none of its inputs changed.
 */
@Composable
fun DateSelector(
    // weekDays kept for API compatibility with existing call-sites; ignored
    // internally — the strip always shows the full 731-day scrollable range.
    @Suppress("UNUSED_PARAMETER") weekDays: List<LocalDate>,
    selectedDate: LocalDate,
    onSelect: (LocalDate) -> Unit
) {
    // allDays is stable for the lifetime of the composition (same list every
    // recompose) — remember {} ensures it is never re-allocated.
    val allDays   = remember { DateUtils.scrollableDays() }
    val listState = rememberLazyListState()

    // Scroll to the selected date when it changes — but only if it is not
    // already visible.  Skipping the scroll when the cell is on-screen avoids
    // an unnecessary layout frame after a simple tap.
    LaunchedEffect(selectedDate) {
        val targetIdx = DateUtils.scrollableDayIndex(selectedDate)
        val visibleRange = listState.layoutInfo.visibleItemsInfo
        val alreadyVisible = visibleRange.any { it.index == targetIdx }
        if (!alreadyVisible) {
            // Scroll so the selected item is 2 cells from the left edge, giving
            // the user context about preceding dates.
            listState.scrollToItem(index = (targetIdx - 2).coerceAtLeast(0))
        }
    }

    LazyRow(
        state = listState,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(
            count = allDays.size,
            // Structural identity key: Compose skips recomposing a DayCell
            // whose epoch-day key has not moved position in the list.
            key   = { allDays[it].toEpochDay() }
        ) { i ->
            val date = allDays[i]
            DayCell(
                date       = date,
                isSelected = date == selectedDate,
                isToday    = date == LocalDate.now(),
                onClick    = { onSelect(date) }
            )
        }
    }
}

/**
 * Single day chip in the date strip.
 *
 * Parameters are all primitives or value-equal types, so Compose can skip
 * recomposing this cell if none of its inputs changed.
 */
@Stable
@Composable
private fun DayCell(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    // These string values are invariant per date — cache them so `remember`
    // returns the same objects on subsequent recompositions of the same cell.
    val dayAbbr   = remember(date) {
        date.dayOfWeek
            .getDisplayName(java.time.format.TextStyle.SHORT, Locale.ENGLISH)
            .take(3)   // "Mon", "Tue", …
    }
    val dayNumber = remember(date) { date.dayOfMonth.toString() }

    val selectedBg    = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
    val normalBg      = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    val bg            = if (isSelected) selectedBg else normalBg
    val selectedColor = MaterialTheme.colorScheme.primary
    val normalColor   = MaterialTheme.colorScheme.onSurfaceVariant

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
                text       = dayAbbr,
                fontSize   = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color      = if (isSelected) selectedColor else normalColor
            )
            // Day number
            Text(
                text       = dayNumber,
                fontSize   = 18.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color      = if (isSelected) selectedColor else MaterialTheme.colorScheme.onSurface
            )
            // Today indicator dot
            Box(Modifier.size(6.dp), contentAlignment = Alignment.Center) {
                if (isToday) {
                    Box(
                        Modifier
                            .size(5.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (isSelected) selectedColor
                                else MaterialTheme.colorScheme.primary
                            )
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
