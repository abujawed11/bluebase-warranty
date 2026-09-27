package com.sunrack.bluebase.ui.feature.developer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sunrack.bluebase.core.di.AppContainer
import com.sunrack.bluebase.ui.components.PlaceholderScreen
import com.sunrack.bluebase.ui.theme.DeveloperRed
import com.sunrack.bluebase.ui.theme.White

/** Ports the `(developer)` Stack: a plain red-header stack, no drawer, matching `_layout.tsx`. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperShell(appContainer: AppContainer) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val canGoBack = navController.previousBackStackEntry != null
    val destination = backStackEntry?.destination

    val title = when {
        destination?.hierarchy?.any { it.hasRoute(DeveloperDeleteClaimsRoute::class) } == true -> "🗑️ Delete Claims"
        destination?.hierarchy?.any { it.hasRoute(DeveloperDeleteUsersRoute::class) } == true -> "👤 Delete Users"
        else -> "🔧 Developer Dashboard"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (canGoBack) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeveloperRed,
                    titleContentColor = White,
                    navigationIconContentColor = White,
                ),
            )
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = DeveloperDashboardRoute,
            modifier = Modifier.fillMaxSize().padding(innerPadding),
        ) {
            composable<DeveloperDashboardRoute> {
                DeveloperDashboardScreen(
                    sessionManager = appContainer.sessionManager,
                    onNavigateToDeleteClaims = { navController.navigate(DeveloperDeleteClaimsRoute) },
                    onNavigateToDeleteUsers = { navController.navigate(DeveloperDeleteUsersRoute) },
                )
            }
            composable<DeveloperDeleteClaimsRoute> { PlaceholderScreen("Delete Warranty Claims") }
            composable<DeveloperDeleteUsersRoute> { PlaceholderScreen("Delete Users") }
        }
    }
}
