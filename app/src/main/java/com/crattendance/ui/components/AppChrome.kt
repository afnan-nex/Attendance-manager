package com.crattendance.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.crattendance.ui.navigation.BottomTab
import com.crattendance.ui.theme.CrIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    onCalendar: (() -> Unit)? = null,
    onSettings: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.Bold) },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(CrIcons.ArrowBack, contentDescription = "Back")
                }
            }
        },
        actions = {
            if (onCalendar != null) {
                IconButton(onClick = onCalendar) {
                    Icon(CrIcons.Calendar, contentDescription = "Pick a date")
                }
            }
            if (onSettings != null) {
                IconButton(onClick = onSettings) {
                    Icon(CrIcons.Settings, contentDescription = "Settings")
                }
            }
            actions()
        },
        // Let M3 tonal-surface coloring come through automatically.
        colors = TopAppBarDefaults.topAppBarColors()
    )
}

/**
 * Bottom navigation bar.
 *
 * ### Performance improvements over the previous version
 *
 * 1. **`navController` parameter instead of `currentRoute: String?`** — the
 *    back-stack state is now observed *inside* this Composable via
 *    [currentBackStackEntryAsState].  Only `AppBottomBar` recomposes when the
 *    route changes; the entire `NavHost` (and the screens inside it) is no
 *    longer invalidated on every tab switch.
 *
 * 2. **`derivedStateOf`** — the `currentRoute` string is wrapped in a
 *    `derivedStateOf` so that even if the back-stack entry object changes (e.g.
 *    args update) but the *route* string stays the same, none of the
 *    `NavigationBarItem` lambdas are re-invoked.
 *
 * 3. **Removed `tint = Color.Gray`** — the hardcoded grey override was
 *    bypassing M3 `NavigationBarItemColors`, which already handles
 *    active/inactive icon and label tints correctly (including tonal-surface
 *    elevation and accessibility contrast).  Removing it restores proper M3
 *    theming and eliminates a per-frame `Color` allocation.
 */
@Composable
fun AppBottomBar(navController: NavController) {
    // Observe the back-stack ONLY inside this Composable.
    // Any route change recomposes AppBottomBar, not its callers.
    val backStackEntry by navController.currentBackStackEntryAsState()

    // derivedStateOf: downstream items only recompose if the route *string*
    // changes — not on every backStackEntry object replacement.
    val currentRoute by remember {
        derivedStateOf { backStackEntry?.destination?.route }
    }

    NavigationBar {
        BottomTab.all.forEach { tab ->
            // Per-item derivedStateOf: this item's lambda only reruns when its
            // own `isSelected` flips — other tab selections don't touch it.
            val isSelected by remember(tab.route) {
                derivedStateOf { currentRoute == tab.route }
            }
            NavigationBarItem(
                selected   = isSelected,
                onClick    = {
                    // Navigate only if we're not already on this tab.
                    if (!isSelected) {
                        navController.navigate(tab.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    // No manual tint — NavigationBarItemColors applies
                    // the correct M3 active/inactive colours automatically.
                    Icon(
                        imageVector    = tab.icon,
                        contentDescription = tab.label
                    )
                },
                label          = { Text(tab.label) },
                alwaysShowLabel = true  // M3 standard: labels always visible
            )
        }
    }
}
