package com.crattendance.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.crattendance.data.model.StudentEntity
import com.crattendance.ui.components.AddStudentDialog
import com.crattendance.ui.components.AppBottomBar
import com.crattendance.ui.components.AppTopBar
import com.crattendance.ui.components.EmptyState
import com.crattendance.ui.components.ReorderableStudentList
import com.crattendance.ui.components.StudentPopup
import com.crattendance.ui.navigation.Routes
import com.crattendance.utils.IntentHelper
import com.crattendance.viewmodel.ManageStudentsViewModel
import com.crattendance.viewmodel.StudentSaveResult
import com.crattendance.viewmodel.activityViewModel

/** Screen 3 — add, edit, delete and drag-reorder students. */
@Composable
fun ManageStudentsScreen(
    onNavigateToTab: (String) -> Unit,
    onOpenSettings: () -> Unit
) {
    val viewModel: ManageStudentsViewModel = activityViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showAddSheet by remember { mutableStateOf(false) }
    var editingStudent by remember { mutableStateOf<StudentEntity?>(null) }
    var selectedStudent by remember { mutableStateOf<StudentEntity?>(null) }

    LaunchedEffect(Unit) {
        viewModel.saveResult.collect { result ->
            when (result) {
                is StudentSaveResult.Success -> {
                    showAddSheet = false
                    editingStudent = null
                    android.widget.Toast.makeText(context, "Student saved", android.widget.Toast.LENGTH_SHORT).show()
                }
                is StudentSaveResult.Error -> {
                    android.widget.Toast.makeText(context, result.message, android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val sheetTarget = if (showAddSheet) null else editingStudent

    selectedStudent?.let { student ->
        StudentPopup(
            student = student,
            onCopy = { IntentHelper.copyToClipboard(context, student.name, it) },
            onOpenWhatsApp = { IntentHelper.openWhatsApp(context, it) },
            onOpenPhone = { IntentHelper.openDialer(context, it) },
            onEdit = {
                editingStudent = student
                selectedStudent = null
            },
            onDelete = {
                viewModel.deleteStudent(student)
                selectedStudent = null
            },
            onDismiss = { selectedStudent = null }
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Manage Students",
                onSettings = onOpenSettings
            )
        },
        bottomBar = { AppBottomBar(Routes.STUDENTS, onNavigateToTab) }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            Button(
                onClick = { showAddSheet = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text("+ Add Student")
            }
            if (state.students.isEmpty()) {
                EmptyState("No students yet — tap \"+ Add Student\" to get started")
            } else {
                ReorderableStudentList(
                    students = state.students,
                    onMove = viewModel::reorder,
                    onRowClick = { selectedStudent = it }
                )
            }
        }
    }

    if (sheetTarget != null || showAddSheet) {
        AddStudentDialog(
            initial = sheetTarget,
            onSave = { student -> viewModel.saveStudent(student) },
            onDismiss = {
                showAddSheet = false
                editingStudent = null
            }
        )
    }
}
