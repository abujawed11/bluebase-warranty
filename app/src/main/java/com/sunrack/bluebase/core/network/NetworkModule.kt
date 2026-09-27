package com.sunrack.bluebase.core.network

import com.sunrack.bluebase.BuildConfig
import com.sunrack.bluebase.core.auth.SessionManager
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Builds the app's two Retrofit clients:
 * - [unauthenticatedRetrofit] for the `token` endpoints, which must not depend on [SessionManager]
 *   (that would be circular, since it needs this client to refresh tokens).
 * - An authenticated client (build via [authenticatedRetrofit]) that attaches the bearer token
 *   and retries once on 401, for every other endpoint.
 *
 * The RN app's axios timeout is 5 minutes to accommodate large media uploads (see `utils/api.ts`).
 */
object NetworkModule {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val jsonMediaType = "application/json".toMediaType()

    private fun okHttpBuilder(): OkHttpClient.Builder = OkHttpClient.Builder()
        .connectTimeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(
                    HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
                )
            }
        }

    val unauthenticatedRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("${BuildConfig.BASE_URL}/")
            .client(okHttpBuilder().build())
            .addConverterFactory(json.asConverterFactory(jsonMediaType))
            .build()
    }

    fun authenticatedRetrofit(sessionManager: SessionManager): Retrofit {
        val client = okHttpBuilder()
            .addInterceptor(AuthInterceptor(sessionManager))
            .authenticator(TokenAuthenticator(sessionManager))
            .build()
        return Retrofit.Builder()
            .baseUrl("${BuildConfig.BASE_URL}/")
            .client(client)
            .addConverterFactory(json.asConverterFactory(jsonMediaType))
            .build()
    }

    private const val REQUEST_TIMEOUT_SECONDS = 300L
}
