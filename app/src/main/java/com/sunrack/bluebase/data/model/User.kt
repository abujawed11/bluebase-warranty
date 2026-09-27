package com.sunrack.bluebase.data.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int,
    val username: String,
    val email: String,
    val client_id: String,
    val account_type: String,
    // The backend doesn't always include this field (e.g. it's absent from /token/'s response) —
    // default to true rather than fail deserialization, matching TS's implicit leniency here.
    val is_active: Boolean = true,
    val company_name: String,
)
