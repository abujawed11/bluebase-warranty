package com.sunrack.bluebase.ui.feature.client.warranty

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sunrack.bluebase.core.util.formatDateTime
import com.sunrack.bluebase.data.model.WarrantyClaimStatusItem
import com.sunrack.bluebase.ui.theme.Black
import com.sunrack.bluebase.ui.theme.BrandYellow
import com.sunrack.bluebase.ui.theme.ErrorRed
import com.sunrack.bluebase.ui.theme.White

private val CardBg = Color(0xFF1E1E1E)
private val ProjectCardBg = Color(0xFF111111)
private val KitRowExpandedBg = Color(0xFF1A1A1A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClaimStatusScreen(
    onViewDetails: (warReqId: String) -> Unit,
    onViewCard: (warReqId: String) -> Unit,
    viewModel: ClaimStatusViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().background(Black)) {
        when {
            uiState.loading -> Text("Loading...", color = BrandYellow, modifier = Modifier.align(Alignment.Center))
            uiState.errorMessage != null -> Column(
                modifier = Modifier.align(Alignment.Center).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(uiState.errorMessage ?: "", color = ErrorRed, textAlign = TextAlign.Center)
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
                        "No warranty request found.",
                        color = Color(0xFF999999),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 64.dp),
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        items(uiState.claimsByProjectThenKit.keys.toList()) { projectId ->
                            val kits = uiState.claimsByProjectThenKit[projectId].orEmpty()
                            val totalClaims = kits.values.sumOf { it.size }
                            ProjectCard(
                                projectId = projectId,
                                totalClaims = totalClaims,
                                kits = kits,
                                expandedProjects = uiState.expandedProjects,
                                expandedKits = uiState.expandedKits,
                                onToggleProject = viewModel::toggleProject,
                                onToggleKit = viewModel::toggleKit,
                                onViewDetails = onViewDetails,
                                onViewCard = onViewCard,
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
    totalClaims: Int,
    kits: Map<String, List<WarrantyClaimStatusItem>>,
    expandedProjects: Set<String>,
    expandedKits: Set<String>,
    onToggleProject: (String) -> Unit,
    onToggleKit: (String, String) -> Unit,
    onViewDetails: (String) -> Unit,
    onViewCard: (String) -> Unit,
) {
    val expanded = projectId in expandedProjects
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
            .background(ProjectCardBg, RoundedCornerShape(16.dp)),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleProject(projectId) }
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Project ID: $projectId", color = White, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Warranty Requests: $totalClaims",
                        color = Color(0xFFBBBBBB),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Text(if (expanded) "▾" else "▸", color = White, style = MaterialTheme.typography.headlineSmall)
            }

            if (expanded) {
                Column(modifier = Modifier.background(KitRowExpandedBg).padding(8.dp)) {
                    kits.forEach { (kitId, claims) ->
                        KitSection(
                            projectId = projectId,
                            kitId = kitId,
                            claims = claims,
                            expanded = "$projectId|$kitId" in expandedKits,
                            onToggle = { onToggleKit(projectId, kitId) },
                            onViewDetails = onViewDetails,
                            onViewCard = onViewCard,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KitSection(
    projectId: String,
    kitId: String,
    claims: List<WarrantyClaimStatusItem>,
    expanded: Boolean,
    onToggle: () -> Unit,
    onViewDetails: (String) -> Unit,
    onViewCard: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 18.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Kit ID: $kitId", color = BrandYellow, style = MaterialTheme.typography.titleSmall)
            Text(if (expanded) "▼" else "►", color = BrandYellow)
        }

        if (expanded) {
            claims.forEach { claim -> ClaimCard(claim = claim, onViewDetails = onViewDetails, onViewCard = onViewCard) }
        }
    }
}

@Composable
private fun ClaimCard(
    claim: WarrantyClaimStatusItem,
    onViewDetails: (String) -> Unit,
    onViewCard: (String) -> Unit,
) {
    val warReqId = claim.war_req_id ?: return
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .background(CardBg, RoundedCornerShape(8.dp))
            .padding(16.dp),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Request: $warReqId", color = White, style = MaterialTheme.typography.bodyMedium)
                Box(
                    modifier = Modifier
                        .background(Color.Transparent, RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(claim.status.replaceFirstChar { it.uppercase() }, color = BrandYellow, style = MaterialTheme.typography.labelSmall)
                }
            }
            Text(
                "Kit Number: ${claim.kit_number ?: "-"}",
                color = Color(0xFFDDDDDD),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 6.dp),
            )
            Text(
                "Last Updated: ${formatDateTime(claim.status_updated_at)}",
                color = Color(0xFFAAAAAA),
                style = MaterialTheme.typography.labelSmall,
            )
            claim.review_comment?.let {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(Color(0xFF111111), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                ) {
                    Column {
                        Text("Reviewer Comment", color = ErrorRed, style = MaterialTheme.typography.labelSmall)
                        Text(it, color = ErrorRed, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Text(
                "Submitted: ${formatDateTime(claim.created_at)}",
                color = Color(0xFFAAAAAA),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 8.dp),
            )
            Row(modifier = Modifier.padding(top = 10.dp)) {
                Button(
                    onClick = { onViewDetails(warReqId) },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandYellow, contentColor = Black),
                ) { Text("View Details") }

                if (claim.status == "approved") {
                    OutlinedButton(
                        onClick = { onViewCard(warReqId) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Black, containerColor = White),
                        modifier = Modifier.padding(start = 12.dp),
                    ) { Text("View Card") }
                }
            }
        }
    }
}
