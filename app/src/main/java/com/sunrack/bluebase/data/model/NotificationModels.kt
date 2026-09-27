package com.sunrack.bluebase.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UnreadCountResponse(
    val unread_count: Int,
)
