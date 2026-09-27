package com.sunrack.bluebase.ui.feature.client.orders

import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sunrack.bluebase.core.util.formatDateOnly
import com.sunrack.bluebase.data.model.Order
import com.sunrack.bluebase.ui.theme.Black
import com.sunrack.bluebase.ui.theme.BrandYellow
import com.sunrack.bluebase.ui.theme.Gray700
import com.sunrack.bluebase.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientOrdersScreen(
    onViewDetails: (orderId: String) -> Unit,
    viewModel: ClientOrdersViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().background(Black)) {
        when {
            uiState.loading -> Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator(color = BrandYellow)
                Text("Loading orders...", color = White, modifier = Modifier.padding(top = 8.dp))
            }

            uiState.errorMessage != null -> Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(uiState.errorMessage ?: "", color = White, textAlign = TextAlign.Center)
                Button(onClick = viewModel::refresh, modifier = Modifier.padding(top = 16.dp)) {
                    Text("Retry")
                }
            }

            else -> PullToRefreshBox(
                isRefreshing = uiState.refreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                if (uiState.orders.isEmpty()) {
                    Text(
                        "No orders found.",
                        color = White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        items(uiState.orders, key = { it.order_id }) { order ->
                            OrderCard(order = order, onViewDetails = { onViewDetails(order.order_id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderCard(order: Order, onViewDetails: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            OrderCardHeader(order.project_id, onViewDetails)
            Text("Client ID: ${order.client_id}", color = Gray700, style = MaterialTheme.typography.bodySmall)
            Text("Ordered: ${formatDateOnly(order.order_date)}", color = Gray700, style = MaterialTheme.typography.bodySmall)
            order.delivery_date?.let {
                Text("Delivered: ${formatDateOnly(it)}", color = Gray700, style = MaterialTheme.typography.bodySmall)
            }
            order.po_date?.let {
                Text("PO Date: ${formatDateOnly(it)}", color = Gray700, style = MaterialTheme.typography.bodySmall)
            }
            Text(
                "Unique Kits: ${order.kit_count}",
                color = Black,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text("Total Kits: ${order.total_quantity}", color = Black, style = MaterialTheme.typography.bodyMedium)
            order.delivery_status?.let {
                Text("Delivery Status: $it", color = Gray700, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun OrderCardHeader(projectId: String, onViewDetails: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "Project ID: $projectId",
            color = Black,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
        )
        Button(onClick = onViewDetails, colors = ButtonDefaults.buttonColors(containerColor = BrandYellow, contentColor = Black)) {
            Text("View Details", style = MaterialTheme.typography.labelSmall)
        }
    }
}
