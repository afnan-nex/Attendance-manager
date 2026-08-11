package com.crattendance.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.crattendance.ui.components.AppTopBar
import com.crattendance.ui.components.CalendarDialog
import com.crattendance.ui.components.ClassCard
import com.crattendance.ui.components.DateSelector
import com.crattendance.ui.components.EmptyState
import com.crattendance.utils.DateUtils
import com.crattendance.viewmodel.HomeViewModel
import com.crattendance.viewmodel.activityViewModel

/** Screen 1 — today's classes for the selected day. */
@Composable
fun HomeScreen(
    onOpenSettings: () -> Unit
) {
    val viewModel: HomeViewModel = activityViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showCalendar by remember { mutableStateOf(false) }

    if (showCalendar) {
        CalendarDialog(
            initialDate = state.selectedDate,
            onSelect = viewModel::selectDate,
            onDismiss = { showCalendar = false }
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Attendance Manager",
                onCalendar = { showCalendar = true },
                onSettings = onOpenSettings
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            DateSelector(
                weekDays = state.weekDays,
                selectedDate = state.selectedDate,
                onSelect = viewModel::selectDate
            )
            Text(
                text = "Today's Classes",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
            )
            if (state.todayClasses.isEmpty()) {
                EmptyState("No classes scheduled for ${DateUtils.formatLong(state.selectedDate)}")
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.todayClasses, key = { it.id }) { cls ->
                        ClassCard(cls = cls)
                    }
                }
            }
        }
    }
}
