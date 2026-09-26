package com.sunrack.bluebase.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// The React Native app is light-only (userInterfaceStyle: "light"), so there is no dark scheme
private val LightColors = lightColorScheme(
    primary = BrandYellow,
    onPrimary = Black,
    primaryContainer = BrandYellowLight,
    onPrimaryContainer = Black,
    secondary = Black,
    onSecondary = White,
    background = White,
    onBackground = Black,
    surface = White,
    onSurface = Black,
    surfaceVariant = Gray100,
    onSurfaceVariant = Gray700,
    outline = Gray300,
    error = ErrorRed,
    onError = White,
)

@Composable
fun BluebaseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        content = content,
    )
}
