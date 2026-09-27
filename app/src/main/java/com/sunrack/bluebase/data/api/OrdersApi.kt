package com.sunrack.bluebase.data.api

import com.sunrack.bluebase.data.model.KitInfo
import com.sunrack.bluebase.data.model.KitScanDetailsResponse
import com.sunrack.bluebase.data.model.Order
import com.sunrack.bluebase.data.model.ProductKit
import com.sunrack.bluebase.data.model.WarrantyDashboardCounts
import retrofit2.http.GET
import retrofit2.http.Path

interface OrdersApi {
    @GET("warranty-dashboard-counts/")
    suspend fun warrantyDashboardCounts(): WarrantyDashboardCounts

    @GET("orders/")
    suspend fun orders(): List<Order>

    @GET("orders/{order_id}/")
    suspend fun orderDetails(@Path("order_id") orderId: String): Order

    @GET("kits/")
    suspend fun productKits(): List<ProductKit>

    @GET("kit/{kit_id}/")
    suspend fun kitById(@Path("kit_id") kitId: String): KitInfo

    @GET("kit-scan-details/{scan_id}/")
    suspend fun kitScanDetails(@Path("scan_id") scanId: String): KitScanDetailsResponse
}
