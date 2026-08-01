package com.example.ui.navigation

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

object Screen {
    const val Home = "home"
    const val Library = "library"
    const val Equalizer = "equalizer"
    const val Search = "search"
    const val Settings = "settings"
    const val Player = "player"
    const val Lyrics = "lyrics"
    const val Privacy = "privacy"
}

class NavigationActions(private val navController: NavHostController) {
    
    fun navigateToTopLevelDestination(route: String) {
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        if (currentRoute == route) {
            // Re-selecting current top-level destination resets its backstack to root
            navController.popBackStack(route, inclusive = false)
            return
        }

        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navigateTo(route: String) {
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        if (currentRoute == route) return // Prevent duplicate route pushes
        
        navController.navigate(route) {
            launchSingleTop = true
        }
    }

    fun popBack() {
        val popped = navController.popBackStack()
        if (!popped) {
            // Fallback to Home root if back stack is empty (e.g., deep link launch)
            navigateToTopLevelDestination(Screen.Home)
        }
    }
}
