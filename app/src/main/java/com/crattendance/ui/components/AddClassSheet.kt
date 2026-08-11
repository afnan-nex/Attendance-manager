package com.crattendance.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogWindowProvider
import android.view.WindowManager
import com.crattendance.data.model.ClassEntity
import com.crattendance.data.model.LectureType
import com.crattendance.utils.DateUtils
import java.time.LocalTime
import java.util.UUID

/**
 * Add / edit class form in a Dialog. [initial] pre-fills the form
 * when editing an existing class.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun AddClassDialog(
    initial: ClassEntity?,
    onSave: (ClassEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var shortName by remember { mutableStateOf(initial?.shortName.orEmpty()) }
    var fullName by remember { mutableStateOf(initial?.fullNameWithCode.orEmpty()) }
    var lectureType by remember { mutableStateOf<LectureType?>(initial?.lectureType) }
    var day by remember { mutableStateOf<Int?>(initial?.dayOfWeek) }
    var teacher     by remember { mutableStateOf(initial?.teacherName.orEmpty()) }
    var location     by remember { mutableStateOf(initial?.location.orEmpty()) }
    var creditHours  by remember { mutableStateOf<Int?>(initial?.creditHours) }
    var startTime    by remember { mutableStateOf(initial?.startTime ?: LocalTime.of(9, 0)) }
    var endTime      by remember { mutableStateOf(initial?.endTime ?: LocalTime.of(10, 0)) }

    // Focus chain for text fields: shortName → fullName → teacher → location
    val fullNameFocus = remember { FocusRequester() }
    val teacherFocus  = remember { FocusRequester() }
    val locationFocus = remember { FocusRequester() }
    val keyboard      = LocalSoftwareKeyboardController.current

    var typeExpanded by remember { mutableStateOf(false) }
    var dayExpanded by remember { mutableStateOf(false) }
    var creditsExpanded by remember { mutableStateOf(false) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    val startPickerState = rememberTimePickerState(
        initialHour = startTime.hour,
        initialMinute = startTime.minute,
        is24Hour = false
    )
    val endPickerState = rememberTimePickerState(
        initialHour = endTime.hour,
        initialMinute = endTime.minute,
        is24Hour = false
    )

    if (showStartPicker) {
        AlertDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startTime = LocalTime.of(startPickerState.hour, startPickerState.minute)
                    showStartPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartPicker = false }) { Text("Cancel") }
            },
            text = { TimePicker(state = startPickerState) }
        )
    }

    if (showEndPicker) {
        AlertDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endTime = LocalTime.of(endPickerState.hour, endPickerState.minute)
                    showEndPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndPicker = false }) { Text("Cancel") }
            },
            text = { TimePicker(state = endPickerState) }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        val view = LocalView.current
        DisposableEffect(view) {
            val window = (view.parent as? DialogWindowProvider)?.window
            window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            onDispose {}
        }

        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (initial == null) "Add Class" else "Edit Class",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = shortName,
                    onValueChange = { if (it.length <= 10) shortName = capitalizeFirst(it) },
                    label = { Text("Short Name (e.g. CP)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { fullNameFocus.requestFocus() }),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { if (it.length <= 100) fullName = capitalizeFirst(it) },
                    label = { Text("Full Name with Code (e.g. CS-101 Computer Programming)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { teacherFocus.requestFocus() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(fullNameFocus)
                )
                Spacer(Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = lectureType?.displayLabel().orEmpty(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Lecture Type") },
                        placeholder = { Text("Select type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        LectureType.entries.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t.displayLabel()) },
                                onClick = {
                                    lectureType = t
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = dayExpanded,
                    onExpandedChange = { dayExpanded = it }
                ) {
                    OutlinedTextField(
                        value = day?.let { dayOfWeekLabel(it) }.orEmpty(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Day") },
                        placeholder = { Text("Select day") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dayExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = dayExpanded,
                        onDismissRequest = { dayExpanded = false }
                    ) {
                        (1..7).forEach { d ->
                            DropdownMenuItem(
                                text = { Text(dayOfWeekLabel(d)) },
                                onClick = {
                                    day = d
                                    dayExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = DateUtils.formatTime(startTime),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Start Time") },
                        modifier = Modifier.weight(1f),
                        trailingIcon = {
                            TextButton(onClick = { showStartPicker = true }) { Text("Pick") }
                        }
                    )
                    Spacer(Modifier.width(12.dp))
                    OutlinedTextField(
                        value = DateUtils.formatTime(endTime),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("End Time") },
                        modifier = Modifier.weight(1f),
                        trailingIcon = {
                            TextButton(onClick = { showEndPicker = true }) { Text("Pick") }
                        }
                    )
                }
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = teacher,
                    onValueChange = { teacher = capitalizeWords(it) },
                    label = { Text("Teacher Name") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { locationFocus.requestFocus() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(teacherFocus)
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = capitalizeFirst(it) },
                    label = { Text("Location") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { keyboard?.hide() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(locationFocus)
                )
                Spacer(Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = creditsExpanded,
                    onExpandedChange = { creditsExpanded = it }
                ) {
                    OutlinedTextField(
                        value = creditHours?.toString().orEmpty(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Credit Hours") },
                        placeholder = { Text("Select credit hours") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = creditsExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = creditsExpanded,
                        onDismissRequest = { creditsExpanded = false }
                    ) {
                        (1..5).forEach { c ->
                            DropdownMenuItem(
                                text = { Text("$c") },
                                onClick = {
                                    creditHours = c
                                    creditsExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.width(12.dp))
                    Button(
                        onClick = {
                            val cls = ClassEntity(
                                id = initial?.id ?: UUID.randomUUID().toString(),
                                shortName = shortName.trim(),
                                fullNameWithCode = fullName.trim(),
                                lectureType = lectureType ?: return@Button,
                                dayOfWeek = day ?: return@Button,
                                startTime = startTime,
                                endTime = endTime,
                                teacherName = teacher.trim(),
                                location = location.trim(),
                                creditHours = creditHours ?: return@Button,
                                isHidden = initial?.isHidden ?: false
                            )
                            onSave(cls)
                        },
                        enabled = shortName.isNotBlank() && fullName.isNotBlank() && lectureType != null && day != null && creditHours != null,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

// ── Capitalization helpers ────────────────────────────────────────────────────

private fun capitalizeWords(input: String): String =
    input.split(" ").joinToString(" ") { word ->
        if (word.isEmpty()) word else word[0].uppercaseChar() + word.drop(1)
    }

private fun capitalizeFirst(input: String): String =
    if (input.isEmpty()) input else input[0].uppercaseChar() + input.drop(1)
