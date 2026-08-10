package com.crattendance.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.crattendance.ui.theme.CrIcons

object Routes {
    const val HOME = "home"
    const val ATTENDANCE = "attendance"
    const val STUDENTS = "students"
    const val CLASSES = "classes"
    const val SETTINGS = "settings"
}

/** The four bottom-navigation destinations. */
sealed class BottomTab(val route: String, val label: String, val icon: ImageVector) {
    data object Home : BottomTab(Routes.HOME, "Home", CrIcons.Home)
    data object Attendance : BottomTab(Routes.ATTENDANCE, "Attendance", CrIcons.Assignment)
    data object Students : BottomTab(Routes.STUDENTS, "Manage Students", CrIcons.PersonAdd)
    data object Classes : BottomTab(Routes.CLASSES, "Manage Classes", CrIcons.Class)

    companion object {
        val all = listOf(Home, Attendance, Students, Classes)
    }
}
