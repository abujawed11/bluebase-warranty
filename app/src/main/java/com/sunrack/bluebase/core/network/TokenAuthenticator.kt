package com.sunrack.bluebase.core.network

import com.sunrack.bluebase.core.auth.SessionManager
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * On a 401, refreshes the access token once and retries the original request, matching the
 * response interceptor in `utils/api.ts`. If refresh fails, [SessionManager.refreshAccessToken]
 * has already logged the session out, so this simply gives up (returns null) and the caller
 * surfaces the failed response.
 */
class TokenAuthenticator(private val sessionManager: SessionManager) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) return null // already retried once, don't loop

        val newAccessToken = runBlocking { sessionManager.refreshAccessToken() } ?: return null

        return response.request.newBuilder()
            .header("Authorization", "Bearer $newAccessToken")
            .build()
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
