package com.sunrack.bluebase.ui.feature.client.warranty

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sunrack.bluebase.ui.components.WarrantyCardView
import com.sunrack.bluebase.ui.theme.Black

private val ScreenBg = Color(0xFFFEF3C7)

@Composable
fun WarrantyCardDetailScreen(viewModel: WarrantyCardDetailViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().background(ScreenBg).padding(16.dp)) {
        when {
            uiState.loading -> CircularProgressIndicator(color = Black, modifier = Modifier.align(Alignment.Center))
            uiState.card != null -> WarrantyCardView(card = uiState.card!!)
            else -> Text(
                uiState.errorMessage ?: "Card not found or not available.",
                color = Color(0xFFDC2626),
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}
