package com.sunrack.bluebase.data.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int,
    val username: String,
    val email: String,
    val client_id: String,
    val account_type: String,
    val is_active: Boolean,
    val company_name: String,
)
