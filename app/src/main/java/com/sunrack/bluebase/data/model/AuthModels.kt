package com.sunrack.bluebase.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String,
    val password: String,
)

@Serializable
data class LoginResponse(
    val access: String,
    val refresh: String,
    val user: User,
)

@Serializable
data class RefreshRequest(
    val refresh: String,
)

@Serializable
data class RefreshResponse(
    val access: String,
    val refresh: String? = null,
)

@Serializable
data class LogoutRequest(
    val refresh: String,
)
