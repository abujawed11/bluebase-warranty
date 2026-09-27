package com.sunrack.bluebase.ui.feature.client

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sunrack.bluebase.core.di.AppContainer
import com.sunrack.bluebase.ui.components.BluebaseTopBar
import com.sunrack.bluebase.ui.components.PlaceholderScreen
import kotlinx.coroutines.launch

/** The client (main) section: dark drawer + yellow top bar + its own nested NavHost, replacing the RN `(main)` Drawer stack. */
@Composable
fun ClientShell(appContainer: AppContainer) {
    val sessionManager = appContainer.sessionManager
    val user by sessionManager.user.collectAsStateWithLifecycle()

    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination
    val current = CLIENT_ROUTE_MATCHERS.firstOrNull { (route, _) ->
        destination?.hierarchy?.any { it.hasRoute(route::class) } == true
    }

    fun navigate(route: Any) {
        scope.launch { drawerState.close() }
        navController.navigate(route) {
            popUpTo(navController.graph.startDestinationId) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ClientDrawerContent(
                user = user,
                currentRoute = current?.first,
                onNavigate = ::navigate,
                onLogout = { scope.launch { sessionManager.logout() } },
            )
        },
    ) {
        Scaffold(
            topBar = {
                BluebaseTopBar(
                    title = current?.second ?: "Bluebase Warranty",
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onBellClick = { navigate(ClientNotificationsRoute) },
                    appContainer = appContainer,
                )
            },
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = ClientDashboardRoute,
                modifier = Modifier.fillMaxSize().padding(innerPadding),
            ) {
                composable<ClientDashboardRoute> { PlaceholderScreen("Dashboard") }
                composable<ClientOrdersRoute> { PlaceholderScreen("All Orders") }
                composable<ClientWarrantyRoute> { PlaceholderScreen("Warranty") }
                composable<ClientProductInfoRoute> { PlaceholderScreen("Product Info") }
                composable<ClientInstallationManualRoute> { PlaceholderScreen("Installation Manual") }
                composable<ClientAboutRoute> { PlaceholderScreen("About") }
                composable<ClientSettingsRoute> { PlaceholderScreen("Settings") }
                composable<ClientNotificationsRoute> { PlaceholderScreen("Notifications") }
            }
        }
    }
}

private val CLIENT_ROUTE_MATCHERS: List<Pair<Any, String>> = listOf(
    ClientDashboardRoute to "Dashboard",
    ClientOrdersRoute to "All Orders",
    ClientWarrantyRoute to "Warranty",
    ClientProductInfoRoute to "Product Info",
    ClientInstallationManualRoute to "Installation Manual",
    ClientAboutRoute to "About",
    ClientSettingsRoute to "Settings",
    ClientNotificationsRoute to "Notifications",
)
