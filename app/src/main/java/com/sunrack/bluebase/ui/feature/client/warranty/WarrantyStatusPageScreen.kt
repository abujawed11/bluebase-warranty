package com.sunrack.bluebase.ui.feature.client.warranty

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sunrack.bluebase.BuildConfig
import com.sunrack.bluebase.core.util.formatDateTime
import com.sunrack.bluebase.data.model.WarrantyClaimDetail
import com.sunrack.bluebase.ui.theme.Black
import com.sunrack.bluebase.ui.theme.BrandYellow
import com.sunrack.bluebase.ui.theme.White

private val ScreenBg = Color(0xFFFEF3C7) // yellow-100

private fun statusBadge(status: String): Triple<Color, String, String> = when {
    status.contains("approved", ignoreCase = true) -> Triple(Color(0xFF22C55E), "✅", "Approved")
    status.contains("pending", ignoreCase = true) -> Triple(Color(0xFFFFD600), "⏳", "Pending Review")
    status.contains("rejected", ignoreCase = true) -> Triple(Color(0xFFEF4444), "⛔", "Rejected")
    else -> Triple(Color(0xFF64748B), "ℹ️", status)
}

/** Absolute media URL, matching `getMediaUrl` in `warranty-status-page.tsx`. */
private fun mediaUrl(path: String): String = when {
    path.startsWith("http") -> path
    path.startsWith("/media/") -> BuildConfig.DOC_BASE_URL + path
    else -> BuildConfig.DOC_BASE_URL + "/" + path.trimStart('/')
}

@Composable
fun WarrantyStatusPageScreen(viewModel: WarrantyStatusPageViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize().background(ScreenBg)) {
        when {
            uiState.loading -> CircularProgressIndicator(color = Black, modifier = Modifier.align(Alignment.Center))
            uiState.errorMessage != null || uiState.claim == null -> Column(
                modifier = Modifier.align(Alignment.Center).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    uiState.errorMessage ?: "Warranty request not found.",
                    color = Color(0xFFDC2626),
                    textAlign = TextAlign.Center,
                )
                Button(
                    onClick = viewModel::refresh,
                    colors = ButtonDefaults.buttonColors(containerColor = Black, contentColor = BrandYellow),
                    modifier = Modifier.padding(top = 16.dp),
                ) { Text("Retry") }
            }
            else -> {
                val claim = uiState.claim!!
                val (statusColor, statusIcon, statusLabel) = statusBadge(claim.status)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            "Warranty Status",
                            color = Color(0xFF161616),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                        )
                        Box(
                            modifier = Modifier
                                .padding(top = 12.dp)
                                .background(statusColor, RoundedCornerShape(32.dp))
                                .padding(horizontal = 18.dp, vertical = 6.dp),
                        ) {
                            Text("$statusIcon $statusLabel", color = Color(0xFF161616), fontWeight = FontWeight.Bold)
                        }
                        Text(
                            "Last updated: ${formatDateTime(claim.status_updated_at)}",
                            color = Color(0xFF666666),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 5.dp),
                        )
                    }

                    SectionCard {
                        InfoRow("Request ID:", claim.war_req_id)
                        InfoRow("Requested On:", formatDateTime(claim.created_at))
                        if (claim.accepted_statement) InfoRow("Accepted Statement:", "Yes")
                    }

                    SectionCard(title = "Product & Order") {
                        InfoRow("Project ID:", claim.project_id ?: "-")
                        InfoRow("Kit ID / Number:", "${claim.kit_id ?: "-"} / ${claim.kit_number ?: "-"}")
                        InfoRow("Purchase Date:", claim.purchase_date ?: "-")
                    }

                    SectionCard(title = "Contact") {
                        InfoRow("Company:", claim.company_name ?: "-")
                        InfoRow("Contact Person:", claim.contact_name ?: "-")
                        InfoRow("Phone:", claim.contact_phone ?: "-")
                        InfoRow("Email:", claim.email ?: "-")
                    }

                    if (claim.reviewed_by?.username != null || claim.review_comment != null) {
                        SectionCard(title = "Review") {
                            claim.reviewed_by?.username?.let { InfoRow("Reviewed By:", it) }
                            claim.review_comment?.let { InfoRow("Review Comment:", it) }
                        }
                    }

                    if (!claim.checklist_answers.isNullOrEmpty()) {
                        SectionCard(title = "Inspection Checklist") {
                            claim.checklist_answers.entries.forEachIndexed { index, (question, raw) ->
                                val checked = raw.normalizeToBool()
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(question, color = Color(0xFF222222), fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                    Text(
                                        if (checked) "✓" else "✗",
                                        color = if (checked) Color(0xFF16A34A) else Color(0xFFEF4444),
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                                if (index != claim.checklist_answers.size - 1) {
                                    HorizontalDivider(color = Color(0xFFEEEEEE))
                                }
                            }
                        }
                    }

                    claim.pdf_url?.let { pdfUrl ->
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(mediaUrl(pdfUrl)))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Black, contentColor = BrandYellow),
                            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp, bottom = 24.dp),
                        ) {
                            Text("📄 View PDF Report")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(title: String? = null, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)
            .background(White, RoundedCornerShape(12.dp))
            .padding(20.dp),
    ) {
        Column {
            title?.let {
                Text(it, color = BrandYellow, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 10.dp))
            }
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
        Text(label, color = Color(0xFF222222), fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 8.dp))
        Text(value, color = Color(0xFF444444))
    }
}
