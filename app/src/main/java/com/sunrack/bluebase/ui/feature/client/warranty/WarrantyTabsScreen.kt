package com.sunrack.bluebase.ui.feature.client.warranty

import androidx.compose.runtime.Composable
import com.sunrack.bluebase.ui.components.TwoTabScaffold

/** Ports `warranty/index.tsx`'s Material Top Tabs: Warranty Status + My Warranty Cards. */
@Composable
fun WarrantyTabsScreen(
    onViewDetails: (warReqId: String) -> Unit,
    onViewCard: (warReqId: String) -> Unit,
    claimStatusViewModel: ClaimStatusViewModel,
    myWarrantyCardsViewModel: MyWarrantyCardsViewModel,
) {
    TwoTabScaffold(
        firstLabel = "Warranty Status",
        secondLabel = "My Warranty Cards",
        firstContent = {
            ClaimStatusScreen(
                onViewDetails = onViewDetails,
                onViewCard = onViewCard,
                viewModel = claimStatusViewModel,
            )
        },
        secondContent = { MyWarrantyCardsScreen(viewModel = myWarrantyCardsViewModel) },
    )
}
