package dev.ividi.weatherapp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")

    data object Dashboard : Screen("dashboard?city={city}") {
        const val CITY_ARG = "city"
        fun routeFor(city: String? = null): String =
            if (city.isNullOrBlank()) "dashboard" else "dashboard?city=$city"
    }

    data object Compare : Screen("compare")
    data object Favorites : Screen("favorites")
    data object History : Screen("history")
    data object Settings : Screen("settings")
}

/** The five destinations shown in the bottom navigation bar once the user is logged in. */
data class BottomNavItem(
    val screen: Screen,
    val baseRoute: String,
    val label: String,
    val icon: ImageVector,
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Dashboard, "dashboard", "Início", Icons.Filled.Home),
    BottomNavItem(Screen.Compare, "compare", "Comparar", Icons.Filled.CompareArrows),
    BottomNavItem(Screen.Favorites, "favorites", "Favoritos", Icons.Filled.Favorite),
    BottomNavItem(Screen.History, "history", "Histórico", Icons.Filled.History),
    BottomNavItem(Screen.Settings, "settings", "Definições", Icons.Filled.Settings),
)
