package com.crattendance.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.crattendance.data.model.StudentEntity
import com.crattendance.ui.theme.CrIcons

/**
 * Student detail popup shown when a row is tapped on the Attendance or
 * Manage Students screens. Includes copy / WhatsApp-open / dialer-open
 * actions, plus an optional Edit button.
 */
@Composable
fun StudentPopup(
    student: StudentEntity,
    onCopy: (String) -> Unit,
    onOpenWhatsApp: (String) -> Unit,
    onOpenPhone: (String) -> Unit,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                Text(
                    text = student.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))

                InfoRow(
                    label = "Reg Number",
                    value = student.registrationNumber,
                    showCopy = true,
                    showOpen = false,
                    onCopy = { onCopy(student.registrationNumber) }
                )
                InfoRow(label = "Section", value = student.section.toString())

                student.cnic?.takeIf { it.isNotBlank() }?.let { cnic ->
                    InfoRow(
                        label = "CNIC",
                        value = cnic,
                        showCopy = true,
                        showOpen = false,
                        onCopy = { onCopy(cnic) }
                    )
                }

                InfoRow(
                    label = "WhatsApp Number",
                    value = student.whatsappNumber,
                    showCopy = true,
                    showOpen = true,
                    onCopy = { onCopy(student.whatsappNumber) },
                    onOpen = { onOpenWhatsApp(student.whatsappNumber) }
                )

                val effectivePhone = student.effectivePhone
                InfoRow(
                    label = if (student.sameAsWhatsapp) "Phone Number (same as WhatsApp)" else "Phone Number",
                    value = effectivePhone,
                    showCopy = effectivePhone.isNotBlank(),
                    showOpen = effectivePhone.isNotBlank(),
                    onCopy = { onCopy(effectivePhone) },
                    onOpen = { onOpenPhone(effectivePhone) }
                )

                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Delete icon — far left, destructive colour
                    if (onDelete != null) {
                        IconButton(onClick = onDelete) {
                            Icon(
                                imageVector = CrIcons.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    // Edit button
                    if (onEdit != null) {
                        OutlinedButton(
                            onClick = onEdit,
                            modifier = Modifier.padding(end = 8.dp)
                        ) { Text("Edit") }
                    }
                    // Close button
                    Button(onClick = onDismiss) { Text("Close") }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    showCopy: Boolean = false,
    showOpen: Boolean = false,
    onCopy: (() -> Unit)? = null,
    onOpen: (() -> Unit)? = null
) {
    Column(Modifier.padding(vertical = 6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value.ifBlank { "—" },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            if (showCopy && onCopy != null) {
                IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                    Icon(CrIcons.ContentCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.primary)
                }
            }
            if (showOpen && onOpen != null) {
                IconButton(onClick = onOpen, modifier = Modifier.size(32.dp)) {
                    Icon(CrIcons.OpenInNew, contentDescription = "Open", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
