package com.sunrack.bluebase.ui.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sunrack.bluebase.data.api.NotificationsApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Backs the header bell badge. Refreshed on demand (e.g. on resume) rather than polled, matching `useNotificationBadge`'s `refetchOnWindowFocus`. */
class NotificationBellViewModel(private val notificationsApi: NotificationsApi) : ViewModel() {
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            runCatching { notificationsApi.unreadCount().unread_count }
                .onSuccess { _unreadCount.value = it }
        }
    }
}
