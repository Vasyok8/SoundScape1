package com.soundscape.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.soundscape.core.ui.theme.OrangeAccent
import com.soundscape.core.ui.theme.SoundScapeTheme
import com.soundscape.core.ui.theme.TextSecondary
import com.soundscape.feature.discover.DiscoverScreen
import com.soundscape.feature.library.LibraryScreen
import com.soundscape.feature.player.PlayerScreen
import com.soundscape.feature.settings.SettingsScreen
import com.soundscape.feature.timer.SearchScreen

/**
 * Главный NavHost — управляет всей навигацией приложения.
 */
@Composable
fun SoundScapeNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Скрываем Bottom Bar на экране плеера
    val showBottomBar = currentDestination?.route?.startsWith("player/") != true

    Scaffold(
        containerColor = SoundScapeTheme.colors.background,
        bottomBar = {
            if (showBottomBar) {
                SoundScapeBottomBar(
                    items = bottomNavItems,
                    currentDestination = currentDestination,
                    onNavigate = { screen ->
                        navController.navigate(screen.route) {
                            // Избегаем дублирования экранов при повторном нажатии
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Discover.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            }
        ) {
            // === Bottom Nav экраны ===
            composable(Screen.Discover.route) {
                DiscoverScreen(
                    onSoundscapeClick = { soundscapeId ->
                        navController.navigate(Screen.Player.createRoute(soundscapeId))
                    }
                )
            }
            composable(Screen.Library.route) {
                LibraryScreen(
                    onSoundscapeClick = { soundscapeId ->
                        navController.navigate(Screen.Player.createRoute(soundscapeId))
                    }
                )
            }
            composable(Screen.Search.route) {
                SearchScreen()
            }
            composable(Screen.Store.route) {
                StoreScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }

            // === Плеер ===
            composable(
                route = Screen.Player.route,
                enterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Up,
                        animationSpec = tween(400)
                    )
                },
                exitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Down,
                        animationSpec = tween(400)
                    )
                }
            ) { backStackEntry ->
                val soundscapeId = backStackEntry.arguments?.getString("soundscapeId") ?: ""
                PlayerScreen(
                    soundscapeId = soundscapeId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

/**
 * Bottom Navigation Bar в стиле myNoise.
 */
@Composable
fun SoundScapeBottomBar(
    items: List<BottomNavItem>,
    currentDestination: androidx.navigation.NavDestination?,
    onNavigate: (Screen) -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFF0D0D0D),
        tonalElevation = 0.dp
    ) {
        items.forEach { item ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = SoundScapeTheme.colors.let {
                            androidx.compose.material3.MaterialTheme.typography.labelSmall
                        }
                    )
                },
                selected = isSelected,
                onClick = { onNavigate(item.screen) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = OrangeAccent,
                    selectedTextColor = OrangeAccent,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
