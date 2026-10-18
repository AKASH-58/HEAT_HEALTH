package com.heathealth.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val HeatHealthLightColorScheme = lightColorScheme(
    primary = PrimaryTeal,
    onPrimary = SurfaceWhite,
    primaryContainer = PrimaryTealLight,
    onPrimaryContainer = PrimaryTealDark,
    secondary = HeatAmber,
    onSecondary = SurfaceWhite,
    secondaryContainer = HeatRiskBg,
    onSecondaryContainer = HeatOrange,
    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = BackgroundLight,
    onSurfaceVariant = TextSecondary,
    outline = BorderLight
)

@Composable
fun HeatHealthTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // HeatHealth is designed around a clean light health-technology aesthetic
    MaterialTheme(
        colorScheme = HeatHealthLightColorScheme,
        typography = Typography,
        content = content
    )
}
