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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.sunrack.bluebase.core.di.AppContainer
import com.sunrack.bluebase.ui.components.BluebaseTopBar
import com.sunrack.bluebase.ui.components.PlaceholderScreen
import com.sunrack.bluebase.ui.feature.client.dashboard.DashboardHomeScreen
import com.sunrack.bluebase.ui.feature.client.dashboard.DashboardHomeViewModel
import com.sunrack.bluebase.ui.feature.client.kitdetails.ClientKitDetailsScreen
import com.sunrack.bluebase.ui.feature.client.kitdetails.ClientKitDetailsViewModel
import com.sunrack.bluebase.ui.feature.client.orders.ClientOrderDetailsScreen
import com.sunrack.bluebase.ui.feature.client.orders.ClientOrderDetailsViewModel
import com.sunrack.bluebase.ui.feature.client.orders.ClientOrdersScreen
import com.sunrack.bluebase.ui.feature.client.orders.ClientOrdersViewModel
import com.sunrack.bluebase.ui.feature.client.productinfo.ProductInfoScreen
import com.sunrack.bluebase.ui.feature.client.productinfo.ProductInfoViewModel
import com.sunrack.bluebase.ui.feature.client.scanner.QrScannerScreen
import com.sunrack.bluebase.ui.feature.client.scanner.QrScannerViewModel
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
                composable<ClientDashboardRoute> {
                    DashboardHomeScreen(
                        user = user,
                        onScanClick = { navController.navigate(ClientQrScannerRoute) },
                        viewModel = viewModel(factory = viewModelFactory {
                            initializer { DashboardHomeViewModel(appContainer.ordersApi) }
                        }),
                    )
                }
                composable<ClientQrScannerRoute> {
                    QrScannerScreen(
                        onNavigateToKitDetails = { route -> navController.navigate(route) },
                        viewModel = viewModel(factory = viewModelFactory {
                            initializer { QrScannerViewModel(appContainer.scannerApi) }
                        }),
                    )
                }
                composable<ClientOrdersRoute> {
                    ClientOrdersScreen(
                        onViewDetails = { orderId -> navController.navigate(ClientOrderDetailsRoute(orderId)) },
                        viewModel = viewModel(factory = viewModelFactory {
                            initializer { ClientOrdersViewModel(appContainer.ordersApi) }
                        }),
                    )
                }
                composable<ClientOrderDetailsRoute> { entry ->
                    val route = entry.toRoute<ClientOrderDetailsRoute>()
                    ClientOrderDetailsScreen(
                        viewModel = viewModel(factory = viewModelFactory {
                            initializer { ClientOrderDetailsViewModel(appContainer.ordersApi, route.orderId) }
                        }),
                    )
                }
                composable<ClientKitDetailsRoute> { entry ->
                    val route = entry.toRoute<ClientKitDetailsRoute>()
                    ClientKitDetailsScreen(
                        user = user,
                        onRequestWarranty = { navController.navigate(ClientClaimFormRoute) },
                        onShowWarrantyStatus = { warReqId -> navController.navigate(ClientWarrantyStatusPageRoute(warReqId)) },
                        viewModel = viewModel(factory = viewModelFactory {
                            initializer {
                                ClientKitDetailsViewModel(
                                    appContainer.ordersApi,
                                    appContainer.warrantyApi,
                                    route.kitId,
                                    route.scanId,
                                    route.allScanned,
                                    route.totalKits,
                                )
                            }
                        }),
                    )
                }
                composable<ClientWarrantyRoute> { PlaceholderScreen("Warranty") }
                composable<ClientClaimFormRoute> { PlaceholderScreen("Claim Warranty") }
                composable<ClientWarrantyStatusPageRoute> { PlaceholderScreen("Warranty Status") }
                composable<ClientProductInfoRoute> {
                    ProductInfoScreen(
                        viewModel = viewModel(factory = viewModelFactory {
                            initializer { ProductInfoViewModel(appContainer.ordersApi) }
                        }),
                    )
                }
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
    ClientQrScannerRoute to "Scan QR Code",
    ClientOrdersRoute to "All Orders",
    ClientOrderDetailsRoute("") to "Order Details",
    ClientKitDetailsRoute() to "Kit Details",
    ClientWarrantyRoute to "Warranty",
    ClientClaimFormRoute to "Claim Warranty",
    ClientProductInfoRoute to "Product Info",
    ClientInstallationManualRoute to "Installation Manual",
    ClientAboutRoute to "About",
    ClientSettingsRoute to "Settings",
    ClientNotificationsRoute to "Notifications",
)
