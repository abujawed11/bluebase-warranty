package com.sunrack.bluebase.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.sunrack.bluebase.ui.theme.Black
import com.sunrack.bluebase.ui.theme.BrandYellow

/** Ports the Material Top Tabs used by `dashboard/index.tsx` and `warranty/index.tsx`. */
@Composable
fun TwoTabScaffold(
    firstLabel: String,
    secondLabel: String,
    firstContent: @Composable () -> Unit,
    secondContent: @Composable () -> Unit,
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        SecondaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Black,
            contentColor = BrandYellow,
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text(firstLabel) })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text(secondLabel) })
        }
        if (selectedTab == 0) firstContent() else secondContent()
    }
}
