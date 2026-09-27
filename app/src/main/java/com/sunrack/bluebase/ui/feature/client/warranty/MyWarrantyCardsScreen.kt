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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sunrack.bluebase.data.model.WarrantyCardDetail
import com.sunrack.bluebase.ui.components.WarrantyCardView
import com.sunrack.bluebase.ui.theme.Black
import com.sunrack.bluebase.ui.theme.BrandYellow
import com.sunrack.bluebase.ui.theme.White

private val ProjectBg = Color(0xFF1A1A1A)
private val ProjectHeaderBg = Color(0xFF2B2B2B)
private val KitHeaderBg = Color(0xFF333333)
private val KitNoLabelBg = Color(0xFFF0F0F0)

@Composable
fun MyWarrantyCardsScreen(viewModel: MyWarrantyCardsViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().background(Black)) {
        when {
            uiState.loading -> Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator(color = BrandYellow)
                Text("Loading warranty cards...", color = BrandYellow, modifier = Modifier.padding(top = 8.dp))
            }
            uiState.errorMessage != null -> Text(
                uiState.errorMessage ?: "",
                color = Color(0xFFFF4C4C),
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center).padding(24.dp),
            )
            uiState.isEmpty -> Text(
                "No warranty cards found.",
                color = BrandYellow,
                modifier = Modifier.align(Alignment.Center),
            )
            else -> LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                items(uiState.cardsByProjectThenKit.keys.toList()) { projectId ->
                    val kits = uiState.cardsByProjectThenKit[projectId].orEmpty()
                    val companyName = kits.values.firstOrNull()?.firstOrNull()?.company_name
                    ProjectSection(
                        projectId = projectId,
                        companyName = companyName,
                        kits = kits,
                        expandedProjects = uiState.expandedProjects,
                        expandedKits = uiState.expandedKits,
                        onToggleProject = viewModel::toggleProject,
                        onToggleKit = viewModel::toggleKit,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProjectSection(
    projectId: String,
    companyName: String?,
    kits: Map<String, List<WarrantyCardDetail>>,
    expandedProjects: Set<String>,
    expandedKits: Set<String>,
    onToggleProject: (String) -> Unit,
    onToggleKit: (String) -> Unit,
) {
    val expanded = projectId in expandedProjects
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)
            .background(ProjectBg, RoundedCornerShape(16.dp)),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleProject(projectId) }
                    .background(ProjectHeaderBg)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Project: $projectId", color = BrandYellow, style = MaterialTheme.typography.titleSmall)
                    companyName?.let {
                        Text(it, color = Color(0xFFCCCCCC), style = MaterialTheme.typography.labelSmall)
                    }
                }
                Text(if (expanded) "−" else "+", color = BrandYellow, style = MaterialTheme.typography.titleMedium)
            }

            if (expanded) {
                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp)) {
                    kits.forEach { (kitId, cards) ->
                        KitSection(
                            kitId = kitId,
                            cards = cards,
                            expanded = kitId in expandedKits,
                            onToggle = { onToggleKit(kitId) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KitSection(kitId: String, cards: List<WarrantyCardDetail>, expanded: Boolean, onToggle: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 9.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .background(KitHeaderBg, RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Kit ID: $kitId", color = BrandYellow, style = MaterialTheme.typography.titleSmall)
            Text(if (expanded) "▼" else "►", color = BrandYellow)
        }

        if (expanded) {
            Column(modifier = Modifier.padding(top = 5.dp)) {
                cards.forEach { card ->
                    Column(modifier = Modifier.padding(bottom = 8.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(KitNoLabelBg, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        ) {
                            Column {
                                Text(
                                    "Kit No: ${card.kit_no ?: "N/A"}",
                                    color = Color(0xFF333333),
                                    style = MaterialTheme.typography.labelMedium,
                                )
                                Text(
                                    "Project: ${card.project_id ?: "N/A"} • Model: ${card.kit_id ?: "N/A"}",
                                    color = Color(0xFF666666),
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                        }
                        WarrantyCardView(card = card, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }
    }
}
