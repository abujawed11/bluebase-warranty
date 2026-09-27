package com.sunrack.bluebase.ui.feature.client

import kotlinx.serialization.Serializable

// Destinations inside the client drawer's own nested NavHost. Mirrors the `(main)` Drawer.Screen
// list and CustomDrawer.tsx's menu; screens not yet ported render PlaceholderScreen for now.
@Serializable object ClientDashboardRoute
@Serializable object ClientOrdersRoute
@Serializable object ClientWarrantyRoute
@Serializable object ClientProductInfoRoute
@Serializable object ClientInstallationManualRoute
@Serializable object ClientAboutRoute
@Serializable object ClientSettingsRoute
@Serializable object ClientNotificationsRoute
