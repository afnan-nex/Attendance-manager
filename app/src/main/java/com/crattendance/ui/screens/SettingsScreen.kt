package com.crattendance.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.crattendance.data.model.AppSettingsEntity
import com.crattendance.data.model.ClassEntity
import com.crattendance.ui.components.AppTopBar
import com.crattendance.ui.components.SectionHeader
import com.crattendance.ui.theme.CrIcons
import com.crattendance.utils.BiometricHelper
import com.crattendance.utils.CsvExporter
import com.crattendance.utils.IntentHelper
import com.crattendance.viewmodel.SettingsViewModel
import com.crattendance.viewmodel.activityViewModel
import kotlinx.coroutines.launch

/** Settings — section filter, CSV export/share and biometric lock. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    val viewModel: SettingsViewModel = activityViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showSectionModal by remember { mutableStateOf(false) }
    var exportMenuExpanded by remember { mutableStateOf(false) }
    var pendingExport by remember { mutableStateOf<CsvExporter.CsvResult?>(null) }
    // Guards against launching the file picker twice (a second launch while one
    // is active throws IllegalStateException) and turns any export failure into
    // a toast instead of an uncaught crash.
    var exportInProgress by remember { mutableStateOf(false) }

    val toast = { msg: String ->
        android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        val result = pendingExport
        pendingExport = null
        if (uri != null && result != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(result.content.toByteArray(Charsets.UTF_8))
                } ?: toast("Could not open the file for writing")
                toast("Exported ${result.fileName}")
            } catch (e: Exception) {
                toast("Export failed: ${e.message ?: "unknown error"}")
            }
        }
        exportInProgress = false
    }

    val selectedClass = state.classes.firstOrNull { it.id == state.selectedExportClassId }
    val canExport = state.selectedExportClassId != null

    if (showSectionModal) {
        SectionSelectorModal(
            current = state.selectedSection,
            onSave = { section ->
                viewModel.setSection(section)
                showSectionModal = false
            },
            onDismiss = { showSectionModal = false }
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Settings",
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ---- Section 1: Your Section ----
            SectionHeader("Your Section")
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Your Section", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "Attendance and CSV export show only this section",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = state.selectedSection,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    TextButton(onClick = { showSectionModal = true }) { Text("Change") }
                }
            }

            // ---- Section 2: Export & Share ----
            SectionHeader("Export & Share")
            Column(Modifier.padding(horizontal = 16.dp)) {
                ExposedDropdownMenuBox(
                    expanded = exportMenuExpanded,
                    onExpandedChange = { exportMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedClass?.let { "${it.shortName} - ${it.fullNameWithCode}" } ?: "Select Class…",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Class") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = exportMenuExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = exportMenuExpanded,
                        onDismissRequest = { exportMenuExpanded = false }
                    ) {
                        state.classes.forEach { cls ->
                            DropdownMenuItem(
                                text = { Text("${cls.shortName} - ${cls.fullNameWithCode}") },
                                onClick = {
                                    viewModel.selectExportClass(cls.id)
                                    exportMenuExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row {
                    Button(
                        onClick = {
                            if (exportInProgress) return@Button
                            val id = state.selectedExportClassId ?: return@Button
                            exportInProgress = true
                            scope.launch {
                                try {
                                    val result = viewModel.buildExportCsv(id)
                                    if (result == null) {
                                        toast("Selected class no longer exists")
                                    } else if (IntentHelper.exportCsvIntent(result.fileName)
                                            .resolveActivity(context.packageManager) == null
                                    ) {
                                        toast("No file picker available on this device")
                                    } else {
                                        pendingExport = result
                                        exportLauncher.launch(result.fileName)
                                    }
                                } catch (e: Exception) {
                                    toast("Export failed: ${e.message ?: "unknown error"}")
                                } finally {
                                    exportInProgress = false
                                }
                            }
                        },
                        enabled = canExport,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Export")
                    }
                    Spacer(Modifier.width(12.dp))
                    OutlinedButton(
                        onClick = {
                            if (exportInProgress) return@OutlinedButton
                            val id = state.selectedExportClassId ?: return@OutlinedButton
                            exportInProgress = true
                            scope.launch {
                                try {
                                    val result = viewModel.buildExportCsv(id)
                                    if (result == null) {
                                        toast("Selected class no longer exists")
                                    } else {
                                        IntentHelper.shareCsv(context, result.fileName, result.content)
                                    }
                                } catch (e: Exception) {
                                    toast("Share failed: ${e.message ?: "unknown error"}")
                                } finally {
                                    exportInProgress = false
                                }
                            }
                        },
                        enabled = canExport,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = CrIcons.Share,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Share")
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Filename example: CS-101_Computer-Programming_0930_15-01-2025.csv\nOnly the selected section is exported.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ---- Section 3: Biometric Security ----
            SectionHeader("Biometric Security")
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Unlock with Biometric", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "Locks the app when resumed; unlock with fingerprint or PIN",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = state.biometricEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                val activity = context as? FragmentActivity
                                val status = BiometricHelper.status(context)
                                when (status) {
                                    is BiometricHelper.Status.Success -> {
                                        if (activity != null) {
                                            BiometricHelper.showPrompt(
                                                activity = activity,
                                                title = "Enable biometric lock",
                                                subtitle = "Confirm your identity to turn on app lock",
                                                onSuccess = {
                                                    viewModel.setBiometricEnabled(true)
                                                    android.widget.Toast.makeText(context, "Biometric unlock enabled", android.widget.Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        }
                                    }
                                    is BiometricHelper.Status.NoHardware ->
                                        android.widget.Toast.makeText(context, "No biometric hardware on this device", android.widget.Toast.LENGTH_LONG).show()
                                    is BiometricHelper.Status.NoEnrolled ->
                                        android.widget.Toast.makeText(context, "No fingerprint enrolled — add one in system Settings first", android.widget.Toast.LENGTH_LONG).show()
                                    is BiometricHelper.Status.HwUnavailable ->
                                        android.widget.Toast.makeText(context, "Biometrics temporarily unavailable", android.widget.Toast.LENGTH_LONG).show()
                                    else ->
                                        android.widget.Toast.makeText(context, "Biometric unlock is not supported", android.widget.Toast.LENGTH_LONG).show()
                                }
                            } else {
                                viewModel.setBiometricEnabled(false)
                            }
                        }
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

/** Modal with All / A-F section options and a Save button. */
@Composable
private fun SectionSelectorModal(
    current: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf(AppSettingsEntity.ALL_SECTIONS) + AppSettingsEntity.SECTIONS.map(Char::toString)
    var selected by remember { mutableStateOf(current) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Your Section") },
        text = {
            Column {
                options.forEach { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = selected == option,
                            onClick = { selected = option }
                        )
                        Text(option, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(selected) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
