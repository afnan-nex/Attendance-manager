package com.crattendance.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.crattendance.data.model.StudentEntity
import com.crattendance.ui.components.AppBottomBar
import com.crattendance.ui.components.AppTopBar
import com.crattendance.ui.components.AttendanceTable
import com.crattendance.ui.components.CalendarDialog
import com.crattendance.ui.components.DateSelector
import com.crattendance.ui.components.EmptyState
import com.crattendance.ui.components.StudentPopup
import com.crattendance.ui.navigation.Routes
import com.crattendance.ui.theme.CrIcons
import com.crattendance.utils.DateUtils
import com.crattendance.utils.IntentHelper
import com.crattendance.viewmodel.AttendanceViewModel
import com.crattendance.viewmodel.activityViewModel
import java.time.LocalDate

/** Screen 2 — mark attendance for a subject on the selected day. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    onNavigateToTab: (String) -> Unit,
    onOpenSettings: () -> Unit
) {
    val viewModel: AttendanceViewModel = activityViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showCalendar by remember { mutableStateOf(false) }
    var showSubjectMenu by remember { mutableStateOf(false) }
    var selectedStudent by remember { mutableStateOf<StudentEntity?>(null) }

    // One-shot unlock / validation toasts.
    LaunchedEffect(Unit) {
        viewModel.toastEvents.collect { message ->
            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
        }
    }
    // Re-lock past-day editing when the user leaves this screen.
    DisposableEffect(Unit) {
        onDispose { viewModel.resetUnlock() }
    }

    if (showCalendar) {
        CalendarDialog(
            initialDate = state.selectedDate,
            onSelect = viewModel::selectDate,
            onDismiss = { showCalendar = false }
        )
    }

    selectedStudent?.let { student ->
        StudentPopup(
            student = student,
            onCopy = { IntentHelper.copyToClipboard(context, student.name, it) },
            onOpenWhatsApp = { IntentHelper.openWhatsApp(context, it) },
            onOpenPhone = { IntentHelper.openDialer(context, it) },
            onDismiss = { selectedStudent = null }
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Attendance",
                onCalendar = { showCalendar = true },
                onSettings = onOpenSettings
            )
        },
        bottomBar = { AppBottomBar(Routes.ATTENDANCE, onNavigateToTab) }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            DateSelector(
                weekDays = state.weekDays,
                selectedDate = state.selectedDate,
                onSelect = viewModel::selectDate
            )

            if (state.classesForDay.isEmpty()) {
                EmptyState("No classes scheduled on ${DateUtils.formatLong(state.selectedDate)}")
            } else {
                SubjectDropdown(
                    selectedLabel = state.selectedClass?.let { "${it.shortName} - ${it.fullNameWithCode}" }
                        ?: "Select subject",
                    classes = state.classesForDay,
                    expanded = showSubjectMenu,
                    onExpandedChange = { showSubjectMenu = it },
                    onSelect = viewModel::selectClass
                )

                LockStatusRow(
                    isFuture = state.isFuture,
                    isPast = state.selectedDate.isBefore(LocalDate.now()),
                    unlocked = state.unlocked,
                    unlockTaps = state.unlockTaps,
                    onUnlockToggle = viewModel::onUnlockToggle
                )

                if (state.students.isEmpty()) {
                    EmptyState(
                        if (state.section == "All") "No students added yet"
                        else "No students in section ${state.section}"
                    )
                } else {
                    AttendanceTable(
                        students = state.students,
                        attendance = state.attendance,
                        enabled = state.canEdit,
                        onToggle = viewModel::toggleAttendance,
                        onStudentClick = { selectedStudent = it }
                    )
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubjectDropdown(
    selectedLabel: String,
    classes: List<com.crattendance.data.model.ClassEntity>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelect: (String) -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text("Subject") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            classes.forEach { cls ->
                DropdownMenuItem(
                    text = { Text("${cls.shortName} - ${cls.fullNameWithCode}") },
                    onClick = {
                        onSelect(cls.id)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}

/** Lock state banner + the 7-tap unlock switch for past dates. */
@Composable
private fun LockStatusRow(
    isFuture: Boolean,
    isPast: Boolean,
    unlocked: Boolean,
    unlockTaps: Int,
    onUnlockToggle: () -> Unit
) {
    if (!isPast && !isFuture) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(CrIcons.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Today's attendance — free to edit",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        return
    }

    if (isFuture) {
        Text(
            text = "Attendance cannot be added for future dates",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        return
    }

    // Past date.
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = if (unlocked) CrIcons.LockOpen else CrIcons.Lock,
            contentDescription = null,
            tint = if (unlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = if (unlocked) "Past attendance unlocked"
                else "Past attendance is locked",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            if (!unlocked) {
                Text(
                    text = "Flip the switch to unlock (${unlockTaps}/7)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (!unlocked) {
            Switch(
                checked = false,
                onCheckedChange = { onUnlockToggle() }
            )
        } else {
            Text(
                text = "Unlocked",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
