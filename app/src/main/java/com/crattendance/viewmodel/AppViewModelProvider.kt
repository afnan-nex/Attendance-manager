package com.crattendance.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import com.crattendance.CRAttendanceApp

/** Simple manual-DI factory that reads repositories off the Application. */
object AppViewModelProvider {

    val Factory: ViewModelProvider.Factory = viewModelFactory {

        initializer {
            val app = app(this)
            HomeViewModel(
                classRepository = app.classRepository,
                settingsRepository = app.settingsRepository
            )
        }

        initializer {
            val app = app(this)
            AttendanceViewModel(
                classRepository = app.classRepository,
                studentRepository = app.studentRepository,
                attendanceRepository = app.attendanceRepository,
                settingsRepository = app.settingsRepository
            )
        }

        initializer {
            val app = app(this)
            ManageStudentsViewModel(studentRepository = app.studentRepository)
        }

        initializer {
            val app = app(this)
            ManageClassesViewModel(classRepository = app.classRepository)
        }

        initializer {
            val app = app(this)
            SettingsViewModel(
                classRepository = app.classRepository,
                studentRepository = app.studentRepository,
                attendanceRepository = app.attendanceRepository,
                settingsRepository = app.settingsRepository
            )
        }
    }

    private fun app(extras: CreationExtras): CRAttendanceApp =
        checkNotNull(extras[APPLICATION_KEY]) as CRAttendanceApp
}

/**
 * Composable helper that scopes a screen's ViewModel to the Activity instead of
 * the nav back-stack entry. Nav-scoped ViewModels are destroyed whenever the tab
 * is popped (bottom-nav switching pops every tab), so each visit re-queries Room
 * and loses in-memory state. Activity-scoped ViewModels survive tab switches,
 * keeping the data warm and the UI responsive.
 */
@Composable
inline fun <reified VM : ViewModel> activityViewModel(
    factory: ViewModelProvider.Factory = AppViewModelProvider.Factory
): VM {
    val activity = LocalContext.current as FragmentActivity
    return viewModel(viewModelStoreOwner = activity, factory = factory)
}
