package com.sunrack.bluebase.ui.feature.client.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sunrack.bluebase.data.model.User
import com.sunrack.bluebase.ui.theme.Black
import com.sunrack.bluebase.ui.theme.BrandYellow
import com.sunrack.bluebase.ui.theme.ErrorRed
import com.sunrack.bluebase.ui.theme.White

/** Ports `dashboard/home.tsx`: black background, greeting, and the yellow warranty-counts card + scan FAB. */
@Composable
fun DashboardHomeScreen(
    user: User?,
    onScanClick: () -> Unit,
    viewModel: DashboardHomeViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().background(Black)) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 32.dp)) {
            Text("Hi, ${user?.username.orEmpty()}", color = White, style = MaterialTheme.typography.headlineSmall)
            Text("Welcome to Sunrack Warranty App", color = White, style = MaterialTheme.typography.headlineSmall)
            Text(
                "Client ID: ${user?.client_id.orEmpty()}",
                color = BrandYellow,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text("Company: ${user?.company_name.orEmpty()}", color = BrandYellow)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp)
                    .background(BrandYellow, RoundedCornerShape(16.dp))
                    .padding(24.dp),
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Verified, contentDescription = null, tint = Black)
                        Text(
                            "Warranty Status",
                            color = Black,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }

                    when {
                        uiState.loading -> CircularProgressIndicator(
                            color = Black,
                            modifier = Modifier.padding(top = 24.dp).align(Alignment.CenterHorizontally),
                        )
                        uiState.errorMessage != null -> Text(
                            uiState.errorMessage ?: "Failed to load warranty data",
                            color = ErrorRed,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                        )
                        else -> {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text("Applied", color = Black, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "${uiState.counts.applied}",
                                    color = Black,
                                    style = MaterialTheme.typography.displaySmall,
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                CountColumn("Pending", uiState.counts.pending, Modifier.weight(1f))
                                CountColumn("Review", uiState.counts.under_review, Modifier.weight(1f))
                                CountColumn("Approved", uiState.counts.approved, Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onScanClick,
            containerColor = BrandYellow,
            contentColor = Black,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
                .size(80.dp),
        ) {
            Icon(Icons.Filled.QrCodeScanner, contentDescription = "Scan QR code", modifier = Modifier.size(36.dp))
        }
    }
}

@Composable
private fun CountColumn(label: String, value: Int, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Black, style = MaterialTheme.typography.bodyMedium)
        Text("$value", color = Black, style = MaterialTheme.typography.headlineMedium)
    }
}
