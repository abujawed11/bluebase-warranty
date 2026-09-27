package com.sunrack.bluebase.data.api

import com.sunrack.bluebase.data.model.QrPayload
import com.sunrack.bluebase.data.model.ScanResultResponse
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ScannerApi {
    @POST("save-order/")
    suspend fun saveOrder(@Body body: QrPayload): ScanResultResponse

    @Multipart
    @POST("upload-qr/")
    suspend fun uploadQr(@Part file: MultipartBody.Part): ScanResultResponse
}
