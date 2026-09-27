package com.sunrack.bluebase.core.di

import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.sunrack.bluebase.ui.feature.auth.forgotpassword.ForgotPasswordViewModel
import com.sunrack.bluebase.ui.feature.auth.login.LoginViewModel
import com.sunrack.bluebase.ui.feature.auth.register.RegisterViewModel

/** Wires ViewModels to [AppContainer]'s dependencies without Hilt — see [AppContainer]'s KDoc for why. */
fun AppContainer.viewModelFactory() = viewModelFactory {
    initializer { LoginViewModel(sessionManager) }
    initializer { RegisterViewModel(authApi) }
    initializer { ForgotPasswordViewModel(authApi) }
}
