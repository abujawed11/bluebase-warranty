package com.sunrack.bluebase.ui.feature.client.kitdetails

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import com.sunrack.bluebase.core.util.mapCodeToCity
import com.sunrack.bluebase.data.model.User
import com.sunrack.bluebase.ui.theme.Black
import com.sunrack.bluebase.ui.theme.BrandYellow
import com.sunrack.bluebase.ui.theme.White

/** Ports `kit-details.tsx`. */
@Composable
fun ClientKitDetailsScreen(
    user: User?,
    onRequestWarranty: () -> Unit,
    onShowWarrantyStatus: (warReqId: String) -> Unit,
    viewModel: ClientKitDetailsViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val (allScanned, totalKits) = viewModel.allScannedInfo

    Box(modifier = Modifier.fillMaxSize().background(BrandYellow)) {
        when {
            uiState.loading -> CircularProgressIndicator(color = Black, modifier = Modifier.align(Alignment.Center))
            uiState.errorMessage != null -> Column(
                modifier = Modifier.align(Alignment.Center).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(uiState.errorMessage ?: "", color = Black, textAlign = TextAlign.Center)
                Button(
                    onClick = viewModel::load,
                    colors = ButtonDefaults.buttonColors(containerColor = Black, contentColor = White),
                    modifier = Modifier.padding(top = 16.dp),
                ) { Text("Retry") }
            }
            else -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 24.dp),
                    ) {
                        Text(
                            "Kit Details",
                            color = Black,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(bottom = 20.dp),
                        )

                        InfoCard {
                            if (uiState.prodUnit.isNotBlank()) {
                                InfoRow("Kit ID", uiState.kitId)
                                if (allScanned) {
                                    InfoRow("Total Kits", "${totalKits ?: ""}")
                                } else {
                                    InfoRow("Kit No", uiState.kitNo)
                                }
                                InfoRow("Production Unit", mapCodeToCity(uiState.prodUnit))
                                InfoRow("Warehouse", mapCodeToCity(uiState.warehouse))
                                InfoRow("Project ID", uiState.projectId)
                                InfoRow("Purchase Date", uiState.purchaseDate)
                            } else {
                                InfoRow("Kit ID", uiState.kitId)
                                Text(
                                    "This is a read-only preview. You are not authorized to view order information.",
                                    color = Black,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 8.dp),
                                )
                            }
                        }

                        Text(
                            "Kit Configuration",
                            color = Black,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
                        )
                        InfoCard {
                            InfoRow("Tilt Angle", "${uiState.kit?.tilt_angle ?: "-"}°")
                            InfoRow("Clearance", "${uiState.kit?.clearance ?: "-"} mm")
                            InfoRow("Configuration", uiState.kit?.configuration ?: "-")
                            InfoRow("No. of Panels", "${uiState.kit?.num_panels ?: "-"}")
                            InfoRow("Region", uiState.kit?.region ?: "-")
                        }

                        if (allScanned) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .background(Color(0xFFFFF8E1), RoundedCornerShape(10.dp))
                                    .padding(12.dp),
                            ) {
                                Text(
                                    "All kits (${totalKits ?: ""}) already scanned under this project.",
                                    color = Color(0xFFB28900),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    }

                    val isClientMatch = uiState.projectId.substringBefore('/').trim() == user?.client_id
                    if (viewModel.hasScanId() && uiState.projectId.isNotBlank() && isClientMatch && !allScanned) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BrandYellow)
                                .padding(vertical = 24.dp),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            if (uiState.isClaimed) {
                                Button(
                                    onClick = { uiState.warReqId?.let(onShowWarrantyStatus) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Black, contentColor = White),
                                ) {
                                    Icon(Icons.Filled.Visibility, contentDescription = null)
                                    Text("Show Warranty Status", modifier = Modifier.padding(start = 8.dp))
                                }
                            } else {
                                Button(
                                    onClick = onRequestWarranty,
                                    colors = ButtonDefaults.buttonColors(containerColor = Black, contentColor = White),
                                ) {
                                    Text("Request Warranty")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)
            .background(White, RoundedCornerShape(12.dp))
            .padding(20.dp),
        content = content,
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.padding(bottom = 12.dp)) {
        Text("$label: ", color = Black, style = MaterialTheme.typography.bodyMedium)
        Text(value, color = Black, style = MaterialTheme.typography.bodyMedium)
    }
}
