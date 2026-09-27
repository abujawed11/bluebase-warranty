package com.sunrack.bluebase.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sunrack.bluebase.core.auth.SessionManager
import com.sunrack.bluebase.core.di.AppContainer
import com.sunrack.bluebase.core.di.viewModelFactory
import com.sunrack.bluebase.ui.feature.auth.forgotpassword.ForgotPasswordScreen
import com.sunrack.bluebase.ui.feature.auth.forgotpassword.ForgotPasswordViewModel
import com.sunrack.bluebase.ui.feature.auth.login.LoginScreen
import com.sunrack.bluebase.ui.feature.auth.login.LoginViewModel
import com.sunrack.bluebase.ui.feature.auth.register.RegisterScreen
import com.sunrack.bluebase.ui.feature.auth.register.RegisterViewModel

/**
 * The app's single NavHost. Reacts to [SessionManager.user]/[SessionManager.loading] to pick the
 * start destination, matching `app/index.tsx`'s splash + role-based redirect. The role "home"
 * destinations are placeholders until Phase 3 (Navigation shell) replaces them with the real
 * client/admin/developer drawers.
 */
@Composable
fun BluebaseNavHost(appContainer: AppContainer) {
    val sessionManager = appContainer.sessionManager
    val loading by sessionManager.loading.collectAsStateWithLifecycle()
    val user by sessionManager.user.collectAsStateWithLifecycle()

    if (loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val navController = rememberNavController()

    LaunchedEffect(user) {
        val destination = user?.startRoute() ?: LoginRoute
        navController.navigate(destination) {
            popUpTo(navController.graph.startDestinationId) { inclusive = true }
            launchSingleTop = true
        }
    }

    NavHost(navController = navController, startDestination = user?.startRoute() ?: LoginRoute) {
        composable<LoginRoute> {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(RegisterRoute) },
                onNavigateToForgotPassword = { navController.navigate(ForgotPasswordRoute) },
                viewModel = viewModel<LoginViewModel>(factory = appContainer.viewModelFactory()),
            )
        }
        composable<RegisterRoute> {
            RegisterScreen(
                onRegistrationSuccess = { navController.navigate(LoginRoute) { popUpTo<LoginRoute>() } },
                onNavigateToLogin = { navController.popBackStack() },
                viewModel = viewModel<RegisterViewModel>(factory = appContainer.viewModelFactory()),
            )
        }
        composable<ForgotPasswordRoute> {
            ForgotPasswordScreen(
                onResetComplete = { navController.navigate(LoginRoute) { popUpTo<LoginRoute>() } },
                onNavigateToLogin = { navController.popBackStack() },
                viewModel = viewModel<ForgotPasswordViewModel>(factory = appContainer.viewModelFactory()),
            )
        }
        composable<ClientHomeRoute> { PlaceholderHome("Client") }
        composable<AdminHomeRoute> { PlaceholderHome("Admin") }
        composable<DeveloperHomeRoute> { PlaceholderHome("Developer") }
    }
}

@Composable
private fun PlaceholderHome(role: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("$role home — coming in Phase 3+")
    }
}
