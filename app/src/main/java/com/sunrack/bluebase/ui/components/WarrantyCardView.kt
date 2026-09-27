package com.sunrack.bluebase.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sunrack.bluebase.core.util.formatDateOnly
import com.sunrack.bluebase.data.model.WarrantyCardDetail
import com.sunrack.bluebase.ui.theme.Black
import com.sunrack.bluebase.ui.theme.Gray300
import com.sunrack.bluebase.ui.theme.White
import java.util.Date

private val CertBlue = Color(0xFF1E3A8A)
private val CoverageBg = Color(0xFFEFF6FF)
private val RowLabelBg = Color(0xFFF9FAFB)

private val COVERAGE_POINTS = listOf(
    "Structural Integrity & Strength",
    "Manufacturing & Workmanship",
    "Corrosion Resistance",
)

private data class CardStatus(val label: String, val color: Color)

private fun warrantyStatus(startIso: String, endIso: String): CardStatus {
    val now = Date()
    val start = com.sunrack.bluebase.core.util.parseIsoDateOrNull(startIso)
    val end = com.sunrack.bluebase.core.util.parseIsoDateOrNull(endIso)
    return when {
        start != null && now.before(start) -> CardStatus("Not Yet Active", Color(0xFFF59E0B))
        end != null && now.after(end) -> CardStatus("Expired", Color(0xFFEF4444))
        else -> CardStatus("Active", Color(0xFF22C55E))
    }
}

private fun warrantyPeriodLabel(months: Int): String {
    val years = months / 12
    return if (years > 0) "$years Year${if (years > 1) "s" else ""}" else "$months Month${if (months > 1) "s" else ""}"
}

/** Ports `components/WarrantyCard.tsx`: the certificate-styled card shown in My Warranty Cards and the single-card detail screen. */
@Composable
fun WarrantyCardView(card: WarrantyCardDetail, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val status = warrantyStatus(card.warranty_started_at, card.expires_at)
    val installationLocation = if (card.installation_latitude != null && card.installation_longitude != null) {
        "${card.installation_latitude}, ${card.installation_longitude}"
    } else {
        "N/A"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Gray300, RoundedCornerShape(12.dp))
            .background(White, RoundedCornerShape(12.dp)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CertBlue, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Shield, contentDescription = null, tint = Color(0xFFFBBF24))
                Text(
                    "WARRANTY CERTIFICATE",
                    color = White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            Box(
                modifier = Modifier
                    .background(status.color.copy(alpha = 0.15f), RoundedCornerShape(50))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(status.label, color = status.color, style = MaterialTheme.typography.labelSmall)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(1.dp, Gray300, RoundedCornerShape(8.dp)),
        ) {
            CertificateRow("Certificate No.", card.certificate_no)
            CertificateRow("Date of Issue", formatDateOnly(card.issued_at))
            CertificateRow("Invoice No.", card.invoice_no ?: "N/A")
            CertificateRow("Invoice Date", card.invoice_date?.let { formatDateOnly(it) } ?: "N/A")
            CertificateRow("Customer Name", card.company_name ?: "N/A")
            CertificateRow("Client ID", card.client_id ?: "N/A")
            CertificateRow("Product Name / Model No.", card.kit_id ?: card.serial_number ?: "N/A")
            CertificateRow("Project ID", card.project_id ?: "N/A")
            CertificateRow("Installation Location", installationLocation)
            CertificateRow("Warranty Start Date", formatDateOnly(card.warranty_started_at))
            CertificateRow(
                "Warranty Period",
                "${warrantyPeriodLabel(card.warranty_duration_months)} (Until ${formatDateOnly(card.expires_at)})",
                isLast = true,
            )
        }

        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Text("Coverage Scope:", color = Black, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .background(CoverageBg, RoundedCornerShape(8.dp))
                    .padding(12.dp),
            ) {
                COVERAGE_POINTS.forEach { point ->
                    Row(modifier = Modifier.padding(bottom = 4.dp)) {
                        Text("• ", color = Color(0xFF2563EB))
                        Text(point, color = Color(0xFF1E40AF), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text("Transferable: ", color = Color(0xFF6B7280), style = MaterialTheme.typography.bodySmall)
            Text(
                if (card.is_transferable) "Yes" else "No",
                color = if (card.is_transferable) Color(0xFF16A34A) else Black,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
            )
        }

        card.terms_document_url?.let { url ->
            Button(
                onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) },
                colors = ButtonDefaults.buttonColors(containerColor = CertBlue, contentColor = White),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 16.dp),
            ) {
                Icon(Icons.Filled.Download, contentDescription = null, tint = Color(0xFFFBBF24))
                Text("Download Official Certificate", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
private fun CertificateRow(label: String, value: String, isLast: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.weight(1f).background(RowLabelBg).padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(label, color = Black, style = MaterialTheme.typography.labelSmall)
        }
        Box(modifier = Modifier.weight(2f).background(White).padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(value, color = Color(0xFF333333), style = MaterialTheme.typography.labelSmall)
        }
    }
}
