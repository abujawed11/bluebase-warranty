package com.sunrack.bluebase.data.api

import com.sunrack.bluebase.data.model.WarrantyCardDetail
import com.sunrack.bluebase.data.model.WarrantyClaimDetail
import com.sunrack.bluebase.data.model.WarrantyClaimStatusItem
import retrofit2.http.GET
import retrofit2.http.Path

interface WarrantyApi {
    @GET("warranty-claims-status/")
    suspend fun warrantyClaimsStatus(): List<WarrantyClaimStatusItem>

    @GET("warranty-claims-status-byid/{war_req_id}/")
    suspend fun warrantyClaimStatusById(@Path("war_req_id") warReqId: String): WarrantyClaimDetail

    @GET("warranty-cards/my/")
    suspend fun myWarrantyCards(): List<WarrantyCardDetail>

    @GET("warranty-cards/by-claim/{war_req_id}/")
    suspend fun warrantyCardByClaim(@Path("war_req_id") warReqId: String): WarrantyCardDetail
}
