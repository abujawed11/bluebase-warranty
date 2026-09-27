package com.sunrack.bluebase.core.network

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.HttpException

/**
 * Extracts a user-facing message from a failed API call, mirroring the RN screens' handling of
 * DRF error bodies: `{"error": "..."}` (most custom endpoints), `{"detail": "..."}` (SimpleJWT),
 * or a field-error map like `{"username": ["This field is required."]}`.
 */
fun Throwable.apiErrorMessage(fallback: String): String {
    val httpException = this as? HttpException ?: return fallback
    val body = httpException.response()?.errorBody()?.string().orEmpty()
    if (body.isBlank()) return fallback
    return runCatching {
        val json = Json.parseToJsonElement(body) as JsonObject
        json["error"]?.jsonPrimitive?.content
            ?: json["detail"]?.jsonPrimitive?.content
            ?: json.entries.firstOrNull()?.let { (key, value) ->
                val message = when (value) {
                    is JsonArray -> (value.firstOrNull() as? JsonPrimitive)?.content
                    is JsonPrimitive -> value.content
                    else -> null
                }
                message?.let { "$key: $it" }
            }
    }.getOrNull() ?: fallback
}
