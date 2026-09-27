package com.sunrack.bluebase.ui.feature.client.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sunrack.bluebase.core.util.mapCodeToCity
import com.sunrack.bluebase.data.model.SavedOrder
import com.sunrack.bluebase.ui.theme.Black
import com.sunrack.bluebase.ui.theme.BrandYellow
import com.sunrack.bluebase.ui.theme.Gray100
import com.sunrack.bluebase.ui.theme.White

private val WarrantyAppliedGray = Color(0xFF9CA3AF)
private val KitAccordionYellow = Color(0xFFCA8A04)

/** Ports `my-scans.tsx`: project → kit accordion, each scan showing Request Warranty or an "already applied" badge. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyScansScreen(
    onGoToKitDetails: (scanId: String) -> Unit,
    viewModel: MyScansViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().background(Black)) {
        when {
            uiState.loading -> Text(
                "Loading scans...",
                color = White,
                modifier = Modifier.align(Alignment.Center),
            )
            uiState.errorMessage != null -> Column(
                modifier = Modifier.align(Alignment.Center).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(uiState.errorMessage ?: "", color = White, textAlign = TextAlign.Center)
                Button(
                    onClick = viewModel::refresh,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandYellow, contentColor = Black),
                    modifier = Modifier.padding(top = 16.dp),
                ) { Text("Retry") }
            }
            else -> PullToRefreshBox(
                isRefreshing = uiState.refreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                if (uiState.isEmpty) {
                    Text(
                        "No scans found.",
                        color = White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        items(uiState.scansByProjectThenKit.keys.toList()) { projectId ->
                            ProjectCard(
                                projectId = projectId,
                                kits = uiState.scansByProjectThenKit[projectId].orEmpty(),
                                expandedProjects = uiState.expandedProjects,
                                expandedKits = uiState.expandedKits,
                                isClaimed = viewModel::isClaimed,
                                onToggleProject = viewModel::toggleProject,
                                onToggleKit = viewModel::toggleKit,
                                onGoToKitDetails = onGoToKitDetails,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectCard(
    projectId: String,
    kits: Map<String, List<SavedOrder>>,
    expandedProjects: Set<String>,
    expandedKits: Set<String>,
    isClaimed: (SavedOrder) -> Boolean,
    onToggleProject: (String) -> Unit,
    onToggleKit: (String, String) -> Unit,
    onGoToKitDetails: (String) -> Unit,
) {
    val expanded = projectId in expandedProjects
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .background(White, RoundedCornerShape(16.dp))
            .padding(8.dp),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleProject(projectId) }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Project ID: $projectId",
                    color = Black,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    if (expanded) "−" else "+",
                    color = BrandYellow,
                    style = MaterialTheme.typography.headlineSmall,
                )
            }

            if (expanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    kits.forEach { (kitId, scans) ->
                        KitAccordion(
                            projectId = projectId,
                            kitId = kitId,
                            scans = scans,
                            expanded = "$projectId|$kitId" in expandedKits,
                            isClaimed = isClaimed,
                            onToggle = { onToggleKit(projectId, kitId) },
                            onGoToKitDetails = onGoToKitDetails,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KitAccordion(
    projectId: String,
    kitId: String,
    scans: List<SavedOrder>,
    expanded: Boolean,
    isClaimed: (SavedOrder) -> Boolean,
    onToggle: () -> Unit,
    onGoToKitDetails: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Kit ID: $kitId",
                color = Black,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            Text(if (expanded) "−" else "+", color = KitAccordionYellow)
        }

        if (expanded) {
            Column(modifier = Modifier.padding(start = 16.dp, top = 2.dp)) {
                scans.forEach { scan ->
                    ScanRow(scan = scan, claimed = isClaimed(scan), onGoToKitDetails = onGoToKitDetails)
                }
            }
        }
    }
}

@Composable
private fun ScanRow(scan: SavedOrder, claimed: Boolean, onGoToKitDetails: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .background(Gray100, RoundedCornerShape(12.dp))
            .padding(8.dp),
    ) {
        Column {
            Text("Kit No: ${scan.kit_no ?: "-"}", color = Black, style = MaterialTheme.typography.bodyMedium)
            Text("Prod Unit: ${mapCodeToCity(scan.prod_unit)}", color = Black, style = MaterialTheme.typography.bodySmall)
            Text("Warehouse: ${mapCodeToCity(scan.warehouse)}", color = Black, style = MaterialTheme.typography.bodySmall)

            if (claimed) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .background(WarrantyAppliedGray, RoundedCornerShape(12.dp))
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Warranty Applied", color = Black, style = MaterialTheme.typography.labelLarge)
                }
            } else {
                Button(
                    onClick = { onGoToKitDetails(scan.scan_id) },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandYellow, contentColor = Black),
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                ) {
                    Text("Request Warranty")
                }
            }
        }
    }
}
