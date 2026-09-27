package com.sunrack.bluebase.ui.feature.client.dashboard

import androidx.compose.runtime.Composable
import com.sunrack.bluebase.data.model.User
import com.sunrack.bluebase.ui.components.TwoTabScaffold

/** Ports `dashboard/index.tsx`'s Material Top Tabs: Home + My Scans. */
@Composable
fun DashboardTabsScreen(
    user: User?,
    onScanClick: () -> Unit,
    onGoToKitDetails: (scanId: String) -> Unit,
    homeViewModel: DashboardHomeViewModel,
    myScansViewModel: MyScansViewModel,
) {
    TwoTabScaffold(
        firstLabel = "Home",
        secondLabel = "My Scans",
        firstContent = { DashboardHomeScreen(user = user, onScanClick = onScanClick, viewModel = homeViewModel) },
        secondContent = { MyScansScreen(onGoToKitDetails = onGoToKitDetails, viewModel = myScansViewModel) },
    )
}
