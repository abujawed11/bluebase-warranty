package com.sunrack.bluebase.ui.feature.client.productinfo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sunrack.bluebase.ui.theme.Black
import com.sunrack.bluebase.ui.theme.BrandYellow
import com.sunrack.bluebase.ui.theme.Gray400
import com.sunrack.bluebase.ui.theme.White

@Composable
fun ProductInfoScreen(viewModel: ProductInfoViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().background(Black)) {
        when {
            uiState.loading -> Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator(color = BrandYellow)
                Text("Loading kits...", color = White, modifier = Modifier.padding(top = 8.dp))
            }

            uiState.errorMessage != null -> Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(uiState.errorMessage ?: "", color = White, textAlign = TextAlign.Center)
                Button(
                    onClick = viewModel::load,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandYellow, contentColor = Black),
                    modifier = Modifier.padding(top = 16.dp),
                ) { Text("Retry") }
            }

            else -> LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 24.dp)) {
                item {
                    Text(
                        "SunRack BlueBase™ Kits",
                        color = BrandYellow,
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    )
                }
                items(uiState.groups) { group ->
                    Text(group.matrixLabel, color = White, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Ideal for: ${group.region}",
                        color = Gray400,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                    group.rows.forEach { row ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .background(White, RoundedCornerShape(16.dp))
                                .padding(16.dp),
                        ) {
                            Column {
                                Text("Clearance: ${row.clearance}", color = Black)
                                Text("Configuration: ${row.configuration}", color = Black, modifier = Modifier.padding(top = 8.dp))
                                Text("Panels: ${row.panels}", color = Black, modifier = Modifier.padding(top = 8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
