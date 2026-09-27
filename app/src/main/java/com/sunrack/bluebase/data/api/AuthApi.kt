package com.sunrack.bluebase.data.api

import com.sunrack.bluebase.data.model.LoginRequest
import com.sunrack.bluebase.data.model.LoginResponse
import com.sunrack.bluebase.data.model.LogoutRequest
import com.sunrack.bluebase.data.model.RefreshRequest
import com.sunrack.bluebase.data.model.RefreshResponse
import com.sunrack.bluebase.data.model.RegisterRequest
import com.sunrack.bluebase.data.model.ResetPasswordRequest
import com.sunrack.bluebase.data.model.SendOtpRequest
import com.sunrack.bluebase.data.model.VerifyOtpRequest
import retrofit2.http.Body
import retrofit2.http.POST

/** Backed by [com.sunrack.bluebase.core.network.NetworkModule.unauthenticatedRetrofit] — none of these calls carry a bearer token, matching the RN app (`register` is explicit about this; `send-otp`/`verify-otp`/`reset-password`/`refresh` don't need one either). */
interface AuthApi {
    @POST("token/")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @POST("token/refresh/")
    suspend fun refresh(@Body body: RefreshRequest): RefreshResponse

    @POST("token/logout/")
    suspend fun logout(@Body body: LogoutRequest)

    @POST("send-otp/")
    suspend fun sendOtp(@Body body: SendOtpRequest)

    @POST("verify-otp/")
    suspend fun verifyOtp(@Body body: VerifyOtpRequest)

    @POST("reset-password/")
    suspend fun resetPassword(@Body body: ResetPasswordRequest)

    @POST("register/")
    suspend fun register(@Body body: RegisterRequest)
}
