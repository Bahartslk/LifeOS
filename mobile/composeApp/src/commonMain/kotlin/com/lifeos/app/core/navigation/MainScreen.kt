package com.lifeos.app.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lifeos.app.core.designsystem.components.AppBottomNavigation
import com.lifeos.app.core.designsystem.components.AppBottomNavigationItem

private data class MainTab(val destination: Destination, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val mainTabs = listOf(
    MainTab(Destination.Dashboard, "Ana Sayfa", Icons.Filled.Home),
    MainTab(Destination.Travel, "Seyahat", Icons.Filled.FlightTakeoff),
    MainTab(Destination.Planner, "Ajanda", Icons.Filled.CalendarMonth),
    MainTab(Destination.AiChat, "AI Asistan", Icons.Filled.AutoAwesome),
    MainTab(Destination.Profile, "Profil", Icons.Filled.Person),
)

/**
 * The post-authentication app shell: a bottom-nav Scaffold hosting its own
 * nested [NavHost], independent of the outer auth→main transition — the
 * standard Compose Navigation pattern for a bottom-nav-owned back stack.
 * Reuses [AppBottomNavigation] from the Design System; tab content is
 * registered per [mainGraph].
 */
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            AppBottomNavigation(
                items = mainTabs.map { tab ->
                    AppBottomNavigationItem(
                        label = tab.label,
                        icon = tab.icon,
                        selected = currentRoute == tab.destination.route,
                        onClick = { navController.navigateToMainTab(tab.destination.route) },
                    )
                },
            )
        },
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Destination.Dashboard.route,
            modifier = Modifier.padding(paddingValues),
        ) {
            mainGraph(navController)
        }
    }
}
