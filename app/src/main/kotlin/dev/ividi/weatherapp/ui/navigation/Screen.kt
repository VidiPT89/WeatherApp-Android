package dev.ividi.weatherapp.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import dev.ividi.weatherapp.R

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")

    data object Dashboard : Screen("dashboard?city={city}") {
        const val CITY_ARG = "city"
        fun routeFor(city: String? = null): String =
            if (city.isNullOrBlank()) "dashboard" else "dashboard?city=$city"
    }

    data object Favorites : Screen("favorites")
    data object History : Screen("history")
    data object Settings : Screen("settings")
}

/** The four destinations shown in the bottom navigation bar once the user is logged in. */
data class BottomNavItem(
    val screen: Screen,
    val baseRoute: String,
    @param:StringRes val labelRes: Int,
    val icon: ImageVector,
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Dashboard, "dashboard", R.string.nav_home, Icons.Filled.Home),
    BottomNavItem(Screen.Favorites, "favorites", R.string.nav_favorites, Icons.Filled.Favorite),
    BottomNavItem(Screen.History, "history", R.string.nav_history, Icons.Filled.History),
    BottomNavItem(Screen.Settings, "settings", R.string.nav_settings, Icons.Filled.Settings),
)
