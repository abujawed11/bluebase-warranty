package com.sunrack.bluebase.data.model

import kotlinx.serialization.Serializable

@Serializable
data class WarrantyDashboardCounts(
    val pending: Int = 0,
    val under_review: Int = 0,
    val approved: Int = 0,
    val rejected: Int = 0,
    val cancelled: Int = 0,
    val applied: Int = 0,
)
