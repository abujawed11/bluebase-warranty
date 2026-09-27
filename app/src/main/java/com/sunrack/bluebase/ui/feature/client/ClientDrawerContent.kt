package com.sunrack.bluebase.ui.feature.client

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.VerifiedUser
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
import com.sunrack.bluebase.ui.theme.BrandYellow
import com.sunrack.bluebase.ui.theme.ErrorRed
import com.sunrack.bluebase.ui.theme.Gray400
import com.sunrack.bluebase.ui.theme.Gray700
import com.sunrack.bluebase.ui.theme.Gray900
import com.sunrack.bluebase.ui.theme.White

private data class DrawerEntry(val route: Any, val label: String, val icon: ImageVector)

private val CLIENT_DRAWER_ITEMS = listOf(
    DrawerEntry(ClientDashboardRoute, "Home", Icons.Filled.Home),
    DrawerEntry(ClientOrdersRoute, "Orders", Icons.Filled.Inventory2),
    DrawerEntry(ClientWarrantyRoute, "Warranty", Icons.Filled.VerifiedUser),
    DrawerEntry(ClientProductInfoRoute, "About Product", Icons.Filled.ShoppingCart),
    DrawerEntry(ClientInstallationManualRoute, "Installation Manual", Icons.Filled.Description),
    DrawerEntry(ClientAboutRoute, "About", Icons.Filled.Info),
    DrawerEntry(ClientSettingsRoute, "Settings", Icons.Filled.Settings),
)

/** Ports `CustomDrawer.tsx`: a dark drawer with a user profile header, menu items, and a pinned logout row. */
@Composable
fun ClientDrawerContent(
    user: User?,
    currentRoute: Any?,
    onNavigate: (Any) -> Unit,
    onLogout: () -> Unit,
) {
    ModalDrawerSheet(drawerContainerColor = Gray900) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(BrandYellow, CircleShape),
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
                    color = Gray400,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            HorizontalDivider(color = Gray700)

            Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp, vertical = 8.dp)) {
                CLIENT_DRAWER_ITEMS.forEach { entry ->
                    val selected = currentRoute == entry.route
                    NavigationDrawerItem(
                        label = { Text(entry.label) },
                        icon = { Icon(entry.icon, contentDescription = null) },
                        selected = selected,
                        onClick = { onNavigate(entry.route) },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = BrandYellow,
                            selectedTextColor = Black,
                            selectedIconColor = Black,
                            unselectedTextColor = BrandYellow,
                            unselectedIconColor = BrandYellow,
                        ),
                    )
                }
            }

            HorizontalDivider(color = Gray700)
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
