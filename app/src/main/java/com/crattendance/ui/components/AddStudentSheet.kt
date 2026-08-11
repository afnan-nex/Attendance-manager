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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.crattendance.data.model.StudentEntity
import java.util.UUID

/**
 * Add / edit student form in a Dialog. When [initial] is provided
 * the dialog edits that student; otherwise it creates a new one.
 *
 * Keyboard behaviour:
 *  • Enter / Next on every field moves focus to the next editable field.
 *  • Enter on the last field (Phone Number) dismisses the keyboard.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun AddStudentDialog(
    initial: StudentEntity?,
    onSave: (StudentEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var name          by remember { mutableStateOf(initial?.name.orEmpty()) }
    var regNo         by remember { mutableStateOf(initial?.registrationNumber.orEmpty()) }
    var section       by remember { mutableStateOf(initial?.section?.toString() ?: "") }
    var cnic          by remember { mutableStateOf(initial?.cnic.orEmpty()) }
    var whatsapp      by remember { mutableStateOf(initial?.whatsappNumber.orEmpty()) }
    var sameAsWhatsapp by remember { mutableStateOf(initial?.sameAsWhatsapp ?: true) }
    var phone         by remember { mutableStateOf(initial?.phoneNumber.orEmpty()) }
    var sectionExpanded by remember { mutableStateOf(false) }

    // Focus requesters to move the cursor forward on Next
    val cnicFocus     = remember { FocusRequester() }
    val regNoFocus    = remember { FocusRequester() }
    val whatsappFocus = remember { FocusRequester() }
    val phoneFocus    = remember { FocusRequester() }
    val keyboard      = LocalSoftwareKeyboardController.current

    Dialog(onDismissRequest = onDismiss) {
        val view = androidx.compose.ui.platform.LocalView.current
        androidx.compose.runtime.DisposableEffect(view) {
            val window = (view.parent as? androidx.compose.ui.window.DialogWindowProvider)?.window
            window?.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
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
                    text = if (initial == null) "Add Student" else "Edit Student",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))

                // ── Name (first letter of each word auto-capitalised)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = capitalizeWords(it) },
                    label = { Text("Name") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { cnicFocus.requestFocus() }),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                // ── CNIC
                OutlinedTextField(
                    value = cnic,
                    onValueChange = { input ->
                        if (input.length <= 13 && input.all { it.isDigit() }) cnic = input
                    },
                    label = { Text("CNIC – 13 digits (optional)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = { regNoFocus.requestFocus() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(cnicFocus)
                )
                Spacer(Modifier.height(8.dp))

                // ── Registration Number (first letter auto-capitalised)
                OutlinedTextField(
                    value = regNo,
                    onValueChange = { regNo = capitalizeFirst(it) },
                    label = { Text("Registration Number") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { whatsappFocus.requestFocus() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(regNoFocus)
                )
                Spacer(Modifier.height(8.dp))

                // ── Section (read-only dropdown — must be selected manually, no default)
                ExposedDropdownMenuBox(
                    expanded = sectionExpanded,
                    onExpandedChange = { sectionExpanded = it }
                ) {
                    OutlinedTextField(
                        value = section,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Section") },
                        placeholder = { Text("Select section") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sectionExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = sectionExpanded,
                        onDismissRequest = { sectionExpanded = false }
                    ) {
                        ('A'..'F').forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s.toString()) },
                                onClick = {
                                    section = s.toString()
                                    sectionExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))

                // ── WhatsApp
                OutlinedTextField(
                    value = whatsapp,
                    onValueChange = { if (it.length <= 13) whatsapp = it },
                    label = { Text("WhatsApp Number (+92…)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = if (sameAsWhatsapp) ImeAction.Done else ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { phoneFocus.requestFocus() },
                        onDone = { keyboard?.hide() }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(whatsappFocus)
                )
                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = sameAsWhatsapp,
                        onCheckedChange = { sameAsWhatsapp = it }
                    )
                    Text("Same as WhatsApp")
                }

                // ── Phone Number (last editable field → Done dismisses keyboard)
                OutlinedTextField(
                    value = phone,
                    onValueChange = { if (it.length <= 13) phone = it },
                    enabled = !sameAsWhatsapp,
                    label = { Text("Phone Number") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { keyboard?.hide() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(phoneFocus)
                )
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
                            val student = StudentEntity(
                                id = initial?.id ?: UUID.randomUUID().toString(),
                                name = name.trim(),
                                registrationNumber = regNo.trim(),
                                section = section.firstOrNull() ?: return@Button,
                                cnic = cnic.trim().ifBlank { null },
                                whatsappNumber = whatsapp.trim(),
                                phoneNumber = if (sameAsWhatsapp) null else phone.trim().ifBlank { null },
                                sameAsWhatsapp = sameAsWhatsapp,
                                orderIndex = initial?.orderIndex ?: -1
                            )
                            onSave(student)
                        },
                        enabled = name.isNotBlank() && section.isNotBlank(),
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

/** Capitalises the first character of each whitespace-separated word. */
private fun capitalizeWords(input: String): String =
    input.split(" ").joinToString(" ") { word ->
        if (word.isEmpty()) word
        else word[0].uppercaseChar() + word.drop(1)
    }

/** Capitalises only the very first character of the string. */
private fun capitalizeFirst(input: String): String =
    if (input.isEmpty()) input
    else input[0].uppercaseChar() + input.drop(1)
