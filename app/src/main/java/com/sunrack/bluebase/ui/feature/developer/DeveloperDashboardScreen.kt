package com.sunrack.bluebase.ui.feature.developer

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sunrack.bluebase.core.auth.SessionManager
import com.sunrack.bluebase.ui.theme.DeveloperRed
import com.sunrack.bluebase.ui.theme.DeveloperSlateBg
import com.sunrack.bluebase.ui.theme.DeveloperSlateCard
import com.sunrack.bluebase.ui.theme.White
import kotlinx.coroutines.launch

private data class DeveloperAction(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val onClick: () -> Unit,
)

/** Ports `(developer)/index.tsx` verbatim: action cards, a warning banner, and an "exit developer mode" logout. */
@Composable
fun DeveloperDashboardScreen(
    sessionManager: SessionManager,
    onNavigateToDeleteClaims: () -> Unit,
    onNavigateToDeleteUsers: () -> Unit,
) {
    val user by sessionManager.user.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    var showLogsDialog by remember { mutableStateOf(false) }
    var showClearCacheDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var cacheCleared by remember { mutableStateOf(false) }

    val actions = listOf(
        DeveloperAction(
            "Delete Warranty Claims",
            "Delete warranty requests and all associated uploads",
            Icons.Filled.DeleteForever,
            Color(0xFFEF4444),
            onNavigateToDeleteClaims,
        ),
        DeveloperAction(
            "Delete Users",
            "Remove users from the system",
            Icons.Filled.PersonRemove,
            Color(0xFFF59E0B),
            onNavigateToDeleteUsers,
        ),
        DeveloperAction(
            "System Logs",
            "View application logs and debug info",
            Icons.Filled.BugReport,
            Color(0xFF8B5CF6),
            { showLogsDialog = true },
        ),
        DeveloperAction(
            "Clear App Cache",
            "Clear cached data and stored credentials",
            Icons.Filled.ClearAll,
            Color(0xFF06B6D4),
            { showClearCacheDialog = true },
        ),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeveloperSlateBg)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        Text(
            text = "🔧 DEVELOPER DASHBOARD",
            color = DeveloperRed,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        )
        Text(
            text = "Welcome, ${user?.username.orEmpty()} • ${user?.email.orEmpty()}",
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Box(
            modifier = Modifier
                .padding(top = 8.dp, bottom = 20.dp)
                .align(Alignment.CenterHorizontally)
                .background(DeveloperRed, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp),
        ) {
            Text("DEVELOPER MODE", color = White, style = MaterialTheme.typography.labelSmall)
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("⚠️ WARNING", color = Color(0xFF92400E), style = MaterialTheme.typography.titleSmall)
                Text(
                    "This is a developer-only area. Actions performed here can permanently delete data and affect all users.",
                    color = Color(0xFF92400E),
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }

        actions.forEach { action ->
            Card(
                onClick = action.onClick,
                colors = CardDefaults.cardColors(containerColor = DeveloperSlateCard),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(action.icon, contentDescription = null, tint = action.color)
                    Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                        Text(action.title, color = White, style = MaterialTheme.typography.titleSmall)
                        Text(action.description, color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodySmall)
                    }
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFF64748B))
                }
            }
        }

        Button(
            onClick = { showLogoutDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = DeveloperRed),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        ) {
            Text("🚪 Exit Developer Mode")
        }
    }

    if (showLogsDialog) {
        AlertDialog(
            onDismissRequest = { showLogsDialog = false },
            confirmButton = { TextButton(onClick = { showLogsDialog = false }) { Text("OK") } },
            title = { Text("System Logs") },
            text = { Text("This feature can be implemented to show app logs") },
        )
    }

    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showClearCacheDialog = false
                    cacheCleared = true
                }) { Text("Clear") }
            },
            dismissButton = { TextButton(onClick = { showClearCacheDialog = false }) { Text("Cancel") } },
            title = { Text("Clear Cache") },
            text = { Text("This will clear all cached data. Continue?") },
        )
    }
    if (cacheCleared) {
        AlertDialog(
            onDismissRequest = { cacheCleared = false },
            confirmButton = { TextButton(onClick = { cacheCleared = false }) { Text("OK") } },
            title = { Text("Success") },
            text = { Text("Cache cleared") },
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    scope.launch { sessionManager.logout() }
                }) { Text("Logout") }
            },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") } },
            title = { Text("Logout") },
            text = { Text("Exit developer mode?") },
        )
    }
}
