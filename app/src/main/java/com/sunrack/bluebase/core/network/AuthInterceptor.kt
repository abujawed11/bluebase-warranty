package com.sunrack.bluebase.core.network

import com.sunrack.bluebase.core.auth.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

/** Attaches `Authorization: Bearer <access>` to every authenticated request, matching `utils/api.ts`. */
class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val token = sessionManager.accessToken
        val authed = if (token != null) {
            request.newBuilder().header("Authorization", "Bearer $token").build()
        } else {
            request
        }
        return chain.proceed(authed)
    }
}
