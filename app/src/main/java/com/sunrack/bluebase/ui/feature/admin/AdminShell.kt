package com.sunrack.bluebase.ui.feature.admin

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
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

/** The admin section: black drawer + yellow top bar + its own nested NavHost, replacing the RN `(adminDashboard)` Drawer stack. */
@Composable
fun AdminShell(appContainer: AppContainer) {
    val sessionManager = appContainer.sessionManager
    val user by sessionManager.user.collectAsStateWithLifecycle()

    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination
    val current = ADMIN_ROUTE_MATCHERS.firstOrNull { (route, _) ->
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
            AdminDrawerContent(
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
                    title = current?.second ?: "Admin",
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onBellClick = { navigate(AdminNotificationsRoute) },
                    appContainer = appContainer,
                )
            },
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = AdminDashboardRoute,
                modifier = Modifier.fillMaxSize().padding(innerPadding),
            ) {
                composable<AdminDashboardRoute> { PlaceholderScreen("Admin Home") }
                composable<AdminManageClientsRoute> { PlaceholderScreen("Manage Clients") }
                composable<AdminManageOrdersRoute> { PlaceholderScreen("Manage Orders") }
                composable<AdminManageKitsRoute> { PlaceholderScreen("Manage Kits") }
                composable<AdminReviewClaimsRoute> { PlaceholderScreen("Review Claims") }
                composable<AdminNotificationsRoute> { PlaceholderScreen("Notifications") }
            }
        }
    }
}

private val ADMIN_ROUTE_MATCHERS: List<Pair<Any, String>> = listOf(
    AdminDashboardRoute to "Admin Home",
    AdminManageClientsRoute to "Manage Clients",
    AdminManageOrdersRoute to "Manage Orders",
    AdminManageKitsRoute to "Manage Kits",
    AdminReviewClaimsRoute to "Review Claims",
    AdminNotificationsRoute to "Notifications",
)
