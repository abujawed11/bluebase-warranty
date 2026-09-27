package com.sunrack.bluebase.core.auth

import com.sunrack.bluebase.core.util.decodeJwtExpMillis
import com.sunrack.bluebase.data.api.AuthApi
import com.sunrack.bluebase.data.model.LoginRequest
import com.sunrack.bluebase.data.model.LogoutRequest
import com.sunrack.bluebase.data.model.RefreshRequest
import com.sunrack.bluebase.data.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Owns the logged-in session end to end: in-memory + encrypted-on-disk tokens, the exposed
 * [user] state that navigation reacts to, and the proactive/on-401 refresh flow. Mirrors
 * `AuthContext.tsx` in the reference RN app.
 *
 * [authApi] must come from an OkHttp client with no [com.sunrack.bluebase.core.network.AuthInterceptor]
 * attached, to avoid a circular dependency between the authenticated Retrofit client and this class.
 */
class SessionManager(
    private val tokenStore: TokenStore,
    private val authApi: AuthApi,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val refreshMutex = Mutex()
    private var refreshTimerJob: Job? = null

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    /** Cached in memory so [com.sunrack.bluebase.core.network.AuthInterceptor] can read it synchronously. */
    @Volatile
    var accessToken: String? = null
        private set

    /** Loads any persisted session on app start, matching the RN app's silent-login behavior. */
    fun initialize() {
        if (tokenStore.hasSession()) {
            accessToken = tokenStore.accessToken
            _user.value = tokenStore.user
            scheduleTokenRefresh(accessToken)
        }
        _loading.value = false
    }

    suspend fun login(username: String, password: String): Result<User> = runCatching {
        val response = authApi.login(LoginRequest(username, password))
        persistSession(response.access, response.refresh, response.user)
        response.user
    }

    /**
     * Refreshes the access token, coalescing concurrent callers (e.g. several parallel 401s)
     * into a single network call. Returns the new access token, or null if refresh failed
     * (in which case the session has already been logged out).
     */
    suspend fun refreshAccessToken(): String? = refreshMutex.withLock {
        val refresh = tokenStore.refreshToken ?: return@withLock null
        try {
            val response = authApi.refresh(RefreshRequest(refresh))
            val newRefresh = response.refresh ?: refresh
            tokenStore.accessToken = response.access
            tokenStore.refreshToken = newRefresh
            accessToken = response.access
            scheduleTokenRefresh(response.access)
            response.access
        } catch (_: Exception) {
            logout()
            null
        }
    }

    suspend fun logout() {
        val refresh = tokenStore.refreshToken
        if (refresh != null) {
            runCatching { authApi.logout(LogoutRequest(refresh)) }
        }
        refreshTimerJob?.cancel()
        tokenStore.clear()
        accessToken = null
        _user.value = null
    }

    private fun persistSession(access: String, refresh: String, user: User) {
        tokenStore.accessToken = access
        tokenStore.refreshToken = refresh
        tokenStore.user = user
        accessToken = access
        _user.value = user
        scheduleTokenRefresh(access)
    }

    private fun scheduleTokenRefresh(token: String?) {
        refreshTimerJob?.cancel()
        val expMillis = token?.let { decodeJwtExpMillis(it) } ?: return
        val delayMillis = expMillis - System.currentTimeMillis() - REFRESH_LEAD_MILLIS
        refreshTimerJob = scope.launch {
            if (delayMillis > 0) {
                kotlinx.coroutines.delay(delayMillis)
            }
            refreshAccessToken()
        }
    }

    private companion object {
        const val REFRESH_LEAD_MILLIS = 10_000L
    }
}
