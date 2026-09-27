package com.sunrack.bluebase.data.api

import com.sunrack.bluebase.data.model.LoginRequest
import com.sunrack.bluebase.data.model.LoginResponse
import com.sunrack.bluebase.data.model.LogoutRequest
import com.sunrack.bluebase.data.model.RefreshRequest
import com.sunrack.bluebase.data.model.RefreshResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("token/")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @POST("token/refresh/")
    suspend fun refresh(@Body body: RefreshRequest): RefreshResponse

    @POST("token/logout/")
    suspend fun logout(@Body body: LogoutRequest)
}
