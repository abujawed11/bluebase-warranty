package com.sunrack.bluebase.core.util

import android.util.Base64
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

/** Decodes the `exp` claim (seconds since epoch) from a JWT's payload, without verifying its signature. */
fun decodeJwtExpMillis(token: String): Long? {
    val parts = token.split(".")
    if (parts.size != 3) return null
    return try {
        val payload = Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
        val json = Json.parseToJsonElement(String(payload, Charsets.UTF_8)) as JsonObject
        json["exp"]?.jsonPrimitive?.longOrNull?.times(1000)
    } catch (_: Exception) {
        null
    }
}
