package com.sunrack.bluebase.core.di

import android.content.Context
import com.sunrack.bluebase.core.auth.SessionManager
import com.sunrack.bluebase.core.auth.TokenStore
import com.sunrack.bluebase.core.network.NetworkModule
import com.sunrack.bluebase.data.api.AuthApi

/**
 * Hand-rolled dependency container (no Hilt/KSP), created once in [com.sunrack.bluebase.BluebaseApp].
 * Kept intentionally simple per CLAUDE.md §6, which allows "Hilt (or manual DI via Application)" —
 * this avoids pinning a Hilt/KSP version alongside the already-finicky AGP/Kotlin/Compose pins (§8).
 */
class AppContainer(context: Context) {
    private val tokenStore = TokenStore(context.applicationContext)

    val authApi: AuthApi = NetworkModule.unauthenticatedRetrofit.create(AuthApi::class.java)

    val sessionManager = SessionManager(tokenStore, authApi)

    val authenticatedRetrofit by lazy { NetworkModule.authenticatedRetrofit(sessionManager) }
}
