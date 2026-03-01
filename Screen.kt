package com.soundscape.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Все маршруты (экраны) приложения.
 */
sealed class Screen(val route: String) {

    // === Bottom Navigation ===
    data object Discover : Screen("discover")
    data object Library : Screen("library")
    data object Search : Screen("search")
    data object Store : Screen("store")
    data object Settings : Screen("settings")

    // === Вложенные экраны ===
    data object Player : Screen("player/{soundscapeId}") {
        fun createRoute(soundscapeId: String) = "player/$soundscapeId"
    }
    data object Timer : Screen("timer")
}

/**
 * Элемент Bottom Navigation Bar
 */
data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(
        screen = Screen.Discover,
        label = "Discover",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    BottomNavItem(
        screen = Screen.Library,
        label = "Library",
        selectedIcon = Icons.Filled.LibraryMusic,
        unselectedIcon = Icons.Outlined.LibraryMusic
    ),
    BottomNavItem(
        screen = Screen.Search,
        label = "Search",
        selectedIcon = Icons.Filled.Search,
        unselectedIcon = Icons.Outlined.Search
    ),
    BottomNavItem(
        screen = Screen.Store,
        label = "Buy now",
        selectedIcon = Icons.Filled.Star,
        unselectedIcon = Icons.Outlined.StarOutline
    ),
    BottomNavItem(
        screen = Screen.Settings,
        label = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
)
