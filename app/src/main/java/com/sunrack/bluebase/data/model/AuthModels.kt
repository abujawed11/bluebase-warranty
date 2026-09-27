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

@Serializable
data class SendOtpRequest(
    val email: String,
    val purpose: String,
)

@Serializable
data class VerifyOtpRequest(
    val email: String,
    val otp: String,
)

@Serializable
data class ResetPasswordRequest(
    val email: String,
    val password: String,
)

@Serializable
data class RegisterRequest(
    val client_id: String,
    val username: String,
    val email: String,
    val password: String,
    val otp: String,
)

