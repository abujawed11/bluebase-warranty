package com.sunrack.bluebase.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sunrack.bluebase.core.di.AppContainer
import com.sunrack.bluebase.core.di.viewModelFactory
import com.sunrack.bluebase.ui.theme.ErrorRed

/**
 * The header used across the client and admin drawers, matching `CustomHeader.tsx`'s
 * `CustomMainHeader`: a hamburger that opens the drawer, a title, and a notification bell
 * with an unread-count dot that refreshes on resume (`useNotificationBadge`'s `refetchOnWindowFocus`).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluebaseTopBar(
    title: String,
    onMenuClick: () -> Unit,
    onBellClick: () -> Unit,
    appContainer: AppContainer,
    modifier: Modifier = Modifier,
) {
    val bellViewModel: NotificationBellViewModel =
        viewModel(factory = appContainer.viewModelFactory())
    val unreadCount by bellViewModel.unreadCount.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { bellViewModel.refresh() }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { bellViewModel.refresh() }

    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Filled.Menu, contentDescription = "Open menu")
            }
        },
        actions = {
            IconButton(onClick = onBellClick) {
                Box {
                    Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                    if (unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = (-2).dp, y = 2.dp)
                                .background(ErrorRed, CircleShape),
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        modifier = modifier,
    )
}
