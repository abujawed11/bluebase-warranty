package com.sunrack.bluebase.core.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.sunrack.bluebase.data.model.User
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Encrypted, synchronous storage for the access/refresh tokens and the logged-in user,
 * mirroring the `access` / `refresh` / `user` SecureStore keys used by the reference RN app.
 */
class TokenStore(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "bluebase_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    private val json = Json { ignoreUnknownKeys = true }

    var accessToken: String?
        get() = prefs.getString(KEY_ACCESS, null)
        set(value) = prefs.edit().putString(KEY_ACCESS, value).apply()

    var refreshToken: String?
        get() = prefs.getString(KEY_REFRESH, null)
        set(value) = prefs.edit().putString(KEY_REFRESH, value).apply()

    var user: User?
        get() = prefs.getString(KEY_USER, null)?.let {
            runCatching { json.decodeFromString(User.serializer(), it) }.getOrNull()
        }
        set(value) = prefs.edit()
            .putString(KEY_USER, value?.let { json.encodeToString(it) })
            .apply()

    fun hasSession(): Boolean = accessToken != null && refreshToken != null && user != null

    fun clear() {
        prefs.edit()
            .remove(KEY_ACCESS)
            .remove(KEY_REFRESH)
            .remove(KEY_USER)
            .apply()
    }

    private companion object {
        const val KEY_ACCESS = "access"
        const val KEY_REFRESH = "refresh"
        const val KEY_USER = "user"
    }
}
