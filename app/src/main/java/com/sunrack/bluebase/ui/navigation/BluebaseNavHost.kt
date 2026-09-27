package com.sunrack.bluebase.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
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
import com.sunrack.bluebase.ui.feature.admin.AdminShell
import com.sunrack.bluebase.ui.feature.client.ClientShell
import com.sunrack.bluebase.ui.feature.developer.DeveloperShell

/**
 * The app's root NavHost. Reacts to [SessionManager.user]/[SessionManager.loading] to pick the
 * start destination, matching `app/index.tsx`'s splash + role-based redirect. Each role section
 * (Client/Admin/Developer) owns its own nested NavHost — see [ClientShell], [AdminShell],
 * [DeveloperShell].
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
        composable<ClientSectionRoute> { ClientShell(appContainer) }
        composable<AdminSectionRoute> { AdminShell(appContainer) }
        composable<DeveloperSectionRoute> { DeveloperShell(appContainer) }
    }
}
