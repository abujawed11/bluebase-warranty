package com.sunrack.bluebase.ui.feature.client.orders

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sunrack.bluebase.core.util.formatDateOnly
import com.sunrack.bluebase.core.util.formatDateTime
import com.sunrack.bluebase.ui.theme.Black
import com.sunrack.bluebase.ui.theme.BrandYellow
import com.sunrack.bluebase.ui.theme.White

private val CardBg = Color(0xFF191C20)
private val CardBgExpanded = Color(0xFF23262B)
private val MutedText = Color(0xFFE0E0E0)

@Composable
fun ClientOrderDetailsScreen(viewModel: ClientOrderDetailsViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().background(Black)) {
        when {
            uiState.loading -> CircularProgressIndicator(color = BrandYellow, modifier = Modifier.align(Alignment.Center))
            uiState.errorMessage != null || uiState.order == null -> Text(
                uiState.errorMessage ?: "No order found.",
                color = White,
                modifier = Modifier.align(Alignment.Center).padding(24.dp),
            )
            else -> {
                val order = uiState.order!!
                LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    item {
                        Text(
                            "Order Details",
                            color = BrandYellow,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                        )
                        Text("Project ID: ${order.project_id}", color = White)
                        Text("Status: ${order.status}", color = White)
                        Text("Order Date: ${formatDateTime(order.order_date)}", color = White)
                        order.billing_address?.let { Text("Billing Address: $it", color = White) }
                        order.delivery_address?.let { Text("Delivery Address: $it", color = White) }
                        order.delivery_date?.let { Text("Delivered On: ${formatDateOnly(it)}", color = White) }
                        order.remarks?.let { Text("Remarks: $it", color = White) }

                        Text(
                            "Ordered Kits (Grouped by Kit)",
                            color = White,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
                        )
                    }

                    items(uiState.kitGroups, key = { it.kitId }) { group ->
                        val expanded = group.kitId in uiState.expandedKitIds
                        KitGroupCard(group = group, expanded = expanded, onToggle = { viewModel.toggleGroup(group.kitId) })
                    }

                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .background(Color(0xFF1A1A1A), RoundedCornerShape(12.dp))
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "Total Kits Ordered: ${order.total_kits ?: order.total_quantity}",
                                color = BrandYellow,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KitGroupCard(group: KitGroup, expanded: Boolean, onToggle: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .background(
                if (expanded) CardBgExpanded else CardBg,
                RoundedCornerShape(16.dp),
            ),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(vertical = 16.dp, horizontal = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(group.kitId, color = White, fontWeight = FontWeight.Bold)
                    Text(
                        "Quantity: ${group.totalQuantity}",
                        color = Color(0xFFADC2DD),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Box(
                    modifier = Modifier
                        .background(BrandYellow, RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text(if (expanded) "−" else "+", color = Black, fontWeight = FontWeight.Bold)
                }
            }

            if (expanded) {
                Column(modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 14.dp)) {
                    KitFact("Tilt Angle", "${group.kit.tilt_angle ?: "-"}°", Color(0xFF8BC34A))
                    KitFact("Clearance", "${group.kit.clearance ?: "-"} ft", Color(0xFF8BC34A))
                    KitFact("Panels", "${group.kit.num_panels ?: "-"}", Color(0xFF6AC5F7))
                    KitFact("Region", group.kit.region ?: "-", Color(0xFFFFA500))
                    Text(
                        "Total Quantity: ${group.totalQuantity}",
                        color = Color(0xFF6EBB73),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 14.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun KitFact(label: String, value: String, dotColor: Color) {
    Row(modifier = Modifier.padding(bottom = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("•", color = dotColor, modifier = Modifier.padding(end = 10.dp))
        Text(label, color = MutedText, fontWeight = FontWeight.Bold)
        Text(": $value", color = MutedText)
    }
}
