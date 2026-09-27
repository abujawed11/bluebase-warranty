package com.sunrack.bluebase.data.api

import com.sunrack.bluebase.data.model.WarrantyClaimStatusItem
import retrofit2.http.GET

/** Only the one endpoint kit-details needs; the rest of the warranty flow's endpoints land in later phases. */
interface WarrantyApi {
    @GET("warranty-claims-status/")
    suspend fun warrantyClaimsStatus(): List<WarrantyClaimStatusItem>
}
