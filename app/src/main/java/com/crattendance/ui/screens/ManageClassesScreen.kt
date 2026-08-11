package com.crattendance.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.crattendance.data.model.ClassEntity
import com.crattendance.ui.components.AddClassDialog
import com.crattendance.ui.components.AppTopBar
import com.crattendance.ui.components.ClassCard
import com.crattendance.ui.components.EmptyState
import com.crattendance.ui.theme.CrIcons
import com.crattendance.viewmodel.ClassSaveResult
import com.crattendance.viewmodel.ManageClassesViewModel
import com.crattendance.viewmodel.activityViewModel

/** Screen 4 — add, edit, delete and hide classes. */
@Composable
fun ManageClassesScreen(
    onOpenSettings: () -> Unit
) {
    val viewModel: ManageClassesViewModel = activityViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showAddSheet by remember { mutableStateOf(false) }
    var editingClass by remember { mutableStateOf<ClassEntity?>(null) }
    var deleteTarget by remember { mutableStateOf<ClassEntity?>(null) }

    LaunchedEffect(Unit) {
        viewModel.saveResult.collect { result ->
            when (result) {
                is ClassSaveResult.Success -> {
                    showAddSheet = false
                    editingClass = null
                    android.widget.Toast.makeText(context, "Class saved", android.widget.Toast.LENGTH_SHORT).show()
                }
                is ClassSaveResult.Error -> {
                    android.widget.Toast.makeText(context, result.message, android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Manage Classes",
                onSettings = onOpenSettings
            )
        },
        bottomBar = {}
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            Button(
                onClick = { showAddSheet = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text("+ Add Class")
            }
            if (state.classes.isEmpty()) {
                EmptyState("No classes yet — tap \"+ Add Class\" to schedule a subject")
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.classes, key = { it.id }) { cls ->
                        ClassCard(cls = cls, actions = {
                            IconButton(onClick = { viewModel.toggleHidden(cls.id, !cls.isHidden) }) {
                                Icon(
                                    imageVector = if (cls.isHidden) CrIcons.Visibility else CrIcons.VisibilityOff,
                                    contentDescription = if (cls.isHidden) "Show class" else "Hide class",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(onClick = { deleteTarget = cls }) {
                                Icon(
                                    imageVector = CrIcons.Delete,
                                    contentDescription = "Delete class",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        })
                    }
                }
            }
        }
    }

    val sheetTarget = if (showAddSheet) null else editingClass
    if (sheetTarget != null || showAddSheet) {
        AddClassDialog(
            initial = sheetTarget,
            onSave = { cls -> viewModel.saveClass(cls) },
            onDismiss = {
                showAddSheet = false
                editingClass = null
            }
        )
    }

    deleteTarget?.let { cls ->
        DeleteClassDialog(
            cls = cls,
            onConfirm = {
                viewModel.deleteClass(cls)
                deleteTarget = null
            },
            onDismiss = { deleteTarget = null }
        )
    }
}

/** Confirmation dialog that requires typing `DELETE <full name>` exactly. */
@Composable
private fun DeleteClassDialog(
    cls: ClassEntity,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var input by remember { mutableStateOf("") }
    val requiredText = "DELETE ${cls.fullNameWithCode}"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Class?") },
        text = {
            Column {
                Text(
                    "This permanently deletes \"${cls.fullNameWithCode}\" and all its attendance records.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.size(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = CrIcons.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Advised to export CSV/Excel from Settings before deleting.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.size(12.dp))
                Text(
                    "Type '$requiredText' to confirm:",
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(Modifier.size(8.dp))
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = input == requiredText
            ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
