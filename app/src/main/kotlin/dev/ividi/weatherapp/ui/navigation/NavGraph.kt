package dev.ividi.weatherapp.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.ui.res.stringResource
import dev.ividi.weatherapp.ui.admin.AdminScreen
import dev.ividi.weatherapp.ui.auth.AuthViewModel
import dev.ividi.weatherapp.ui.auth.LoginScreen
import dev.ividi.weatherapp.ui.auth.RegisterScreen
import dev.ividi.weatherapp.ui.common.SplashScreen
import dev.ividi.weatherapp.ui.dashboard.DashboardScreen
import dev.ividi.weatherapp.ui.favorites.FavoritesScreen
import dev.ividi.weatherapp.ui.history.HistoryScreen
import dev.ividi.weatherapp.ui.settings.SettingsScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay

// Matches iOS's splash duration (see WeatherApp-iOS's RootView) so the branded splash reads
// consistently across both clients instead of Android flashing by noticeably faster.
private const val SPLASH_MINIMUM_DURATION_MS = 2_300L

/** Root graph: a branded splash, then an auth flow (login/register), then the bottom-nav'd main app. */
@Composable
fun WeatherAppNavGraph() {
    var showSplash by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(SPLASH_MINIMUM_DURATION_MS)
        showSplash = false
    }

    if (showSplash) {
        SplashScreen()
        return
    }

    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsStateWithLifecycle()

    val startDestination = if (isLoggedIn) Screen.Dashboard.routeFor() else Screen.Login.route

    // TokenAuthenticator clears the session in the background when a refresh fails (session
    // fully expired) -- without this, the user is left stranded on whatever screen they were on,
    // with every subsequent request 401ing silently.
    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) {
            val current = navController.currentDestination?.route
            if (current != Screen.Login.route && current != Screen.Register.route) {
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (isLoggedIn) {
                MainBottomBar(navController)
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Dashboard.routeFor()) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                )
            }
            composable(Screen.Register.route) {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(Screen.Dashboard.routeFor()) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.popBackStack() },
                )
            }
            composable(
                route = Screen.Dashboard.route,
                arguments = listOf(
                    navArgument(Screen.Dashboard.CITY_ARG) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                ),
            ) {
                DashboardScreen()
            }
            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    onFavoriteSelected = { city ->
                        navController.navigate(Screen.Dashboard.routeFor(city)) {
                            popUpTo(Screen.Dashboard.route) { inclusive = true }
                        }
                    },
                )
            }
            composable(Screen.History.route) { HistoryScreen() }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onLoggedOut = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToAdmin = { navController.navigate(Screen.Admin.route) },
                )
            }
            composable(Screen.Admin.route) { AdminScreen(onBack = { navController.popBackStack() }) }
        }
    }
}

@Composable
private fun MainBottomBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        bottomNavItems.forEach { item ->
            val isSelected = currentDestination?.hierarchy?.any { destination ->
                destination.route?.startsWith(item.baseRoute) == true
            } == true
            val label = stringResource(item.labelRes)

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    navController.navigate(item.screen.routeForNav()) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { androidx.compose.material3.Icon(item.icon, contentDescription = label) },
                label = { Text(label) },
            )
        }
    }
}

private fun Screen.routeForNav(): String = when (this) {
    is Screen.Dashboard -> this.routeFor()
    else -> route
}
