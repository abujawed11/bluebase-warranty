package com.sunrack.bluebase.ui.feature.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.sunrack.bluebase.data.model.User
import com.sunrack.bluebase.ui.theme.Black
import com.sunrack.bluebase.ui.theme.BrandYellowLight
import com.sunrack.bluebase.ui.theme.ErrorRed
import com.sunrack.bluebase.ui.theme.White

private data class AdminDrawerEntry(val route: Any, val label: String, val icon: ImageVector)

private val ADMIN_DRAWER_ITEMS = listOf(
    AdminDrawerEntry(AdminDashboardRoute, "Admin Home", Icons.Filled.Home),
    AdminDrawerEntry(AdminManageClientsRoute, "Manage Clients", Icons.Filled.People),
    AdminDrawerEntry(AdminManageOrdersRoute, "Manage Orders", Icons.AutoMirrored.Filled.ListAlt),
    AdminDrawerEntry(AdminManageKitsRoute, "Manage Kits", Icons.Filled.Widgets),
    AdminDrawerEntry(AdminReviewClaimsRoute, "Review Claims", Icons.AutoMirrored.Filled.Assignment),
)

/** Ports `CustomAdminDrawer.tsx`: black drawer, yellow accents, same profile-header + logout pattern as the client drawer. */
@Composable
fun AdminDrawerContent(
    user: User?,
    currentRoute: Any?,
    onNavigate: (Any) -> Unit,
    onLogout: () -> Unit,
) {
    ModalDrawerSheet(drawerContainerColor = Black) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier.size(80.dp).background(BrandYellowLight, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = Black)
                }
                Text(
                    text = user?.username?.uppercase().orEmpty(),
                    color = White,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 12.dp),
                )
                Text(
                    text = user?.email.orEmpty(),
                    color = White,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            HorizontalDivider(color = BrandYellowLight)

            Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp, vertical = 8.dp)) {
                ADMIN_DRAWER_ITEMS.forEach { entry ->
                    NavigationDrawerItem(
                        label = { Text(entry.label) },
                        icon = { Icon(entry.icon, contentDescription = null) },
                        selected = currentRoute == entry.route,
                        onClick = { onNavigate(entry.route) },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = BrandYellowLight,
                            selectedTextColor = Black,
                            selectedIconColor = Black,
                            unselectedTextColor = BrandYellowLight,
                            unselectedIconColor = BrandYellowLight,
                        ),
                    )
                }
            }

            HorizontalDivider(color = BrandYellowLight)
            NavigationDrawerItem(
                label = { Text("Logout") },
                icon = { Icon(Icons.Filled.PowerSettingsNew, contentDescription = null, tint = ErrorRed) },
                selected = false,
                onClick = onLogout,
                colors = NavigationDrawerItemDefaults.colors(
                    unselectedTextColor = ErrorRed,
                    unselectedIconColor = ErrorRed,
                ),
                modifier = Modifier.padding(8.dp),
            )
        }
    }
}
