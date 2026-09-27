package com.sunrack.bluebase.data.api

import com.sunrack.bluebase.data.model.UnreadCountResponse
import retrofit2.http.GET

/** Full notifications list/mark-read/delete endpoints land in Phase 8; only the bell badge count is needed for the Phase 3 shell. */
interface NotificationsApi {
    @GET("notifications/unread_count/")
    suspend fun unreadCount(): UnreadCountResponse
}
