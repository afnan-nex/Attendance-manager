package com.crattendance.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.crattendance.ui.components.AppBottomBar
import com.crattendance.ui.screens.AttendanceScreen
import com.crattendance.ui.screens.HomeScreen
import com.crattendance.ui.screens.ManageClassesScreen
import com.crattendance.ui.screens.ManageStudentsScreen
import com.crattendance.ui.screens.SettingsScreen

/**
 * Top-level navigation host with a single hoisted [Scaffold] + [AppBottomBar].
 *
 * ### Performance notes
 *
 * - **Hoisted Scaffold**: The `AppBottomBar` lives here, outside the `NavHost`.
 *   It is created once and never recreated on destination changes.  Previously,
 *   each screen had its own `Scaffold` + `AppBottomBar`, which meant the bar was
 *   destroyed and re-created on every tab switch.
 *
 * - **`NavController` passed directly to `AppBottomBar`**: The bar now reads
 *   back-stack state internally via `derivedStateOf`, so route changes only
 *   recompose the bar — not the `NavHost` or the screens inside it.
 *
 * - **60ms fade transition** (down from 80ms): Imperceptible on 60Hz, but on
 *   120Hz displays the halved transition duration feels snappier without any
 *   visual jank.
 *
 * - **`Modifier.background`** on `NavHost` prevents the 1-frame transparent
 *   window flash that can appear during the cross-fade.
 */
@Composable
fun AppNavGraph(navController: NavHostController) {

    // Observe the current route here ONLY to decide whether to show the bar.
    // AppBottomBar also observes it internally for highlighting — two observers
    // is fine; both are cheap derivedStateOf reads.
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute by remember {
        derivedStateOf { backStackEntry?.destination?.route }
    }
    val showBottomBar = currentRoute != Routes.SETTINGS

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter   = slideInVertically(initialOffsetY = { it }, animationSpec = tween(200)),
                exit    = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(200))
            ) {
                AppBottomBar(navController)
            }
        }
    ) { innerPadding ->

        NavHost(
            navController     = navController,
            startDestination  = Routes.HOME,
            modifier          = Modifier
                // Apply ONLY the bottom inset (nav bar height + bottom system bar).
                // The top (status bar) inset is intentionally NOT applied here —
                // each screen's inner Scaffold + TopAppBar handles it independently,
                // so the status bar padding is added exactly once, not twice.
                .padding(bottom = innerPadding.calculateBottomPadding())
                .background(MaterialTheme.colorScheme.background),
            // 60ms fade: fast enough to feel instant, long enough to avoid a
            // hard cut that exposes the surface-background behind the screen.
            enterTransition   = { fadeIn(animationSpec  = tween(60)) },
            exitTransition    = { fadeOut(animationSpec = tween(60)) }
        ) {
            composable(Routes.HOME) {
                HomeScreen(onOpenSettings = { navController.navigate(Routes.SETTINGS) })
            }
            composable(Routes.ATTENDANCE) {
                AttendanceScreen(onOpenSettings = { navController.navigate(Routes.SETTINGS) })
            }
            composable(Routes.STUDENTS) {
                ManageStudentsScreen(onOpenSettings = { navController.navigate(Routes.SETTINGS) })
            }
            composable(Routes.CLASSES) {
                ManageClassesScreen(onOpenSettings = { navController.navigate(Routes.SETTINGS) })
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
