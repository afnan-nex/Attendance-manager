package com.crattendance.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.crattendance.ui.screens.AttendanceScreen
import com.crattendance.ui.screens.HomeScreen
import com.crattendance.ui.screens.ManageClassesScreen
import com.crattendance.ui.screens.ManageStudentsScreen
import com.crattendance.ui.screens.SettingsScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        // Short fade prevents the 1-frame white flash on rapid tab switches
        enterTransition = { fadeIn(animationSpec = tween(80)) },
        exitTransition  = { fadeOut(animationSpec = tween(80)) }
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToTab = { navigateToTab(navController, it) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.ATTENDANCE) {
            AttendanceScreen(
                onNavigateToTab = { navigateToTab(navController, it) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.STUDENTS) {
            ManageStudentsScreen(
                onNavigateToTab = { navigateToTab(navController, it) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.CLASSES) {
            ManageClassesScreen(
                onNavigateToTab = { navigateToTab(navController, it) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

private fun navigateToTab(navController: NavHostController, route: String) {
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
