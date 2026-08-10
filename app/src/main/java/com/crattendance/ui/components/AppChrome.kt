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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
        colors = TopAppBarDefaults.topAppBarColors()
    )
}

@Composable
fun AppBottomBar(
    currentRoute: String?,
    onNavigateToTab: (String) -> Unit
) {
    NavigationBar {
        BottomTab.all.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.route,
                onClick = { onNavigateToTab(tab.route) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = if (currentRoute == tab.route) Color.Unspecified else Color.Gray
                    )
                },
                label = { Text(tab.label) }
            )
        }
    }
}
