package com.sunrack.bluebase.ui.feature.client

import kotlinx.serialization.Serializable

// Destinations inside the client drawer's own nested NavHost. Mirrors the `(main)` Drawer.Screen
// list and CustomDrawer.tsx's menu; screens not yet ported render PlaceholderScreen for now.
@Serializable object ClientDashboardRoute
@Serializable object ClientQrScannerRoute
@Serializable object ClientOrdersRoute
@Serializable data class ClientOrderDetailsRoute(val orderId: String)
@Serializable data class ClientKitDetailsRoute(
    val kitId: String? = null,
    val scanId: String? = null,
    val allScanned: Boolean = false,
    val totalKits: Int? = null,
)
@Serializable object ClientWarrantyRoute
@Serializable object ClientClaimFormRoute
@Serializable data class ClientWarrantyStatusPageRoute(val warReqId: String)
@Serializable object ClientProductInfoRoute
@Serializable object ClientInstallationManualRoute
@Serializable object ClientAboutRoute
@Serializable object ClientSettingsRoute
@Serializable object ClientNotificationsRoute
