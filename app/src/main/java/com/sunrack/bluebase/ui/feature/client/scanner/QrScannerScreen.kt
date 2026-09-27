package com.sunrack.bluebase.ui.feature.client.scanner

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sunrack.bluebase.ui.feature.client.ClientKitDetailsRoute
import com.sunrack.bluebase.ui.theme.Black
import com.sunrack.bluebase.ui.theme.BrandYellow
import com.sunrack.bluebase.ui.theme.White
import java.util.concurrent.Executors

private val OverlayColor = Color(0x99000000)

/** Ports `qr-scanner.tsx`: live QR scan via CameraX + ML Kit, gallery upload, torch toggle. */
@Composable
fun QrScannerScreen(
    onNavigateToKitDetails: (ClientKitDetailsRoute) -> Unit,
    viewModel: QrScannerViewModel,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasCameraPermission = granted
    }
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.resetForFocus() }

    LaunchedEffect(uiState.navigateTarget) {
        uiState.navigateTarget?.let {
            onNavigateToKitDetails(it)
            viewModel.consumeNavigation()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { viewModel.uploadImage(it, context.contentResolver) }
    }

    if (uiState.errorMessage != null) {
        AlertDialog(
            onDismissRequest = viewModel::dismissError,
            confirmButton = { TextButton(onClick = viewModel::dismissError) { Text("OK") } },
            title = { Text("Error") },
            text = { Text(uiState.errorMessage ?: "") },
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(Black)) {
        if (!hasCameraPermission) {
            Text(
                "Camera permission not granted.",
                color = White,
                modifier = Modifier.align(Alignment.Center),
            )
            return@Box
        }

        var controller by remember { mutableStateOf<LifecycleCameraController?>(null) }

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PreviewView(ctx).apply {
                    val cameraController = LifecycleCameraController(ctx).apply {
                        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                        setImageAnalysisAnalyzer(
                            Executors.newSingleThreadExecutor(),
                            QrCodeAnalyzer(onQrCodeDetected = viewModel::onQrCodeDetected),
                        )
                        bindToLifecycle(lifecycleOwner)
                    }
                    this.controller = cameraController
                    controller = cameraController
                }
            },
        )

        LaunchedEffect(uiState.torchEnabled, controller) {
            controller?.enableTorch(uiState.torchEnabled)
        }

        ScannerOverlay()

        Text(
            "Scan BlueBase",
            color = BrandYellow,
            fontWeight = FontWeight.Black,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 48.dp),
        )

        Row(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 112.dp),
            horizontalArrangement = Arrangement.spacedBy(48.dp),
        ) {
            ScannerAction(
                icon = Icons.Filled.Image,
                label = "Upload QR",
                tint = BrandYellow,
                onClick = { galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            )
            ScannerAction(
                icon = if (uiState.torchEnabled) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                label = if (uiState.torchEnabled) "Torch On" else "Torch Off",
                tint = if (uiState.torchEnabled) White else BrandYellow,
                onClick = viewModel::toggleTorch,
            )
        }

        Text(
            "SUN-RACK",
            color = BrandYellow,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 40.dp),
        )
    }
}

@Composable
private fun ScannerAction(icon: ImageVector, label: String, tint: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(80.dp)) {
        IconButton(onClick = onClick) {
            Icon(icon, contentDescription = label, tint = tint)
        }
        Text(label, color = tint, style = MaterialTheme.typography.labelSmall)
    }
}

/** The dark overlay + yellow-cornered scan box, matching `qr-scanner.tsx`'s pixel layout. */
@Composable
private fun ScannerOverlay() {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val boxSize = screenWidthDp * 0.7f

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth().weight(1f).background(OverlayColor))
        Row(modifier = Modifier.fillMaxWidth().height(boxSize)) {
            Box(modifier = Modifier.weight(1f).fillMaxSize().background(OverlayColor))
            Box(modifier = Modifier.width(boxSize).fillMaxSize()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val corner = 24.dp.toPx()
                    val thickness = 4.dp.toPx()
                    val w = size.width
                    val h = size.height

                    // top-left
                    drawLine(BrandYellow, androidx.compose.ui.geometry.Offset(0f, thickness / 2), androidx.compose.ui.geometry.Offset(corner, thickness / 2), thickness)
                    drawLine(BrandYellow, androidx.compose.ui.geometry.Offset(thickness / 2, 0f), androidx.compose.ui.geometry.Offset(thickness / 2, corner), thickness)
                    // top-right
                    drawLine(BrandYellow, androidx.compose.ui.geometry.Offset(w - corner, thickness / 2), androidx.compose.ui.geometry.Offset(w, thickness / 2), thickness)
                    drawLine(BrandYellow, androidx.compose.ui.geometry.Offset(w - thickness / 2, 0f), androidx.compose.ui.geometry.Offset(w - thickness / 2, corner), thickness)
                    // bottom-left
                    drawLine(BrandYellow, androidx.compose.ui.geometry.Offset(0f, h - thickness / 2), androidx.compose.ui.geometry.Offset(corner, h - thickness / 2), thickness)
                    drawLine(BrandYellow, androidx.compose.ui.geometry.Offset(thickness / 2, h - corner), androidx.compose.ui.geometry.Offset(thickness / 2, h), thickness)
                    // bottom-right
                    drawLine(BrandYellow, androidx.compose.ui.geometry.Offset(w - corner, h - thickness / 2), androidx.compose.ui.geometry.Offset(w, h - thickness / 2), thickness)
                    drawLine(BrandYellow, androidx.compose.ui.geometry.Offset(w - thickness / 2, h - corner), androidx.compose.ui.geometry.Offset(w - thickness / 2, h), thickness)
                }
            }
            Box(modifier = Modifier.weight(1f).fillMaxSize().background(OverlayColor))
        }
        Box(modifier = Modifier.fillMaxWidth().weight(1f).background(OverlayColor))
    }
}
