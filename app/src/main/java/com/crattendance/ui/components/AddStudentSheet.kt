package com.crattendance.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.crattendance.data.model.StudentEntity
import java.util.UUID

/**
 * Add / edit student form in a modal bottom sheet. When [initial] is provided
 * the sheet edits that student; otherwise it creates a new one.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStudentSheet(
    initial: StudentEntity?,
    onSave: (StudentEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initial?.name.orEmpty()) }
    var regNo by remember { mutableStateOf(initial?.registrationNumber.orEmpty()) }
    var section by remember { mutableStateOf(initial?.section?.toString() ?: "A") }
    var cnic by remember { mutableStateOf(initial?.cnic.orEmpty()) }
    var whatsapp by remember { mutableStateOf(initial?.whatsappNumber.orEmpty()) }
    var sameAsWhatsapp by remember { mutableStateOf(initial?.sameAsWhatsapp ?: true) }
    var phone by remember { mutableStateOf(initial?.phoneNumber.orEmpty()) }
    var sectionExpanded by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = if (initial == null) "Add Student" else "Edit Student",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = cnic,
                onValueChange = { cnic = it },
                label = { Text("CNIC (optional)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = regNo,
                onValueChange = { regNo = it },
                label = { Text("Registration Number") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = sectionExpanded,
                onExpandedChange = { sectionExpanded = it }
            ) {
                OutlinedTextField(
                    value = section,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Section") },
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

            OutlinedTextField(
                value = whatsapp,
                onValueChange = { whatsapp = it },
                label = { Text("WhatsApp Number (+92…)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = sameAsWhatsapp,
                    onCheckedChange = { sameAsWhatsapp = it }
                )
                Text("Same as WhatsApp")
            }

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                enabled = !sameAsWhatsapp,
                label = { Text("Phone Number") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    val student = StudentEntity(
                        id = initial?.id ?: UUID.randomUUID().toString(),
                        name = name.trim(),
                        registrationNumber = regNo.trim(),
                        section = section.first(),
                        cnic = cnic.trim().ifBlank { null },
                        whatsappNumber = whatsapp.trim(),
                        phoneNumber = if (sameAsWhatsapp) null else phone.trim().ifBlank { null },
                        sameAsWhatsapp = sameAsWhatsapp,
                        orderIndex = initial?.orderIndex ?: -1
                    )
                    onSave(student)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}
