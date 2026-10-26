package com.kampplus.hava.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

enum class AppThemeStyle(val displayName: String) {
    SYSTEM("Sistem / Varsayılan"),
    OCEAN("Okyanus Mavisi"),
    SUNSET("Gün Batımı Moru"),
    DARK_AMOLED("Amoled Gece")
}

private val LightColors = lightColorScheme(
    primary = Teal40,
    onPrimary = Neutral99,
    primaryContainer = Teal90,
    onPrimaryContainer = Teal10,
    background = Neutral99,
    onBackground = Neutral10,
    surface = Neutral99,
    onSurface = Neutral10,
    surfaceVariant = NeutralVariant90,
    onSurfaceVariant = NeutralVariant30,
    error = Red40
)

private val DarkColors = darkColorScheme(
    primary = Teal80,
    onPrimary = Teal20,
    primaryContainer = Teal20,
    onPrimaryContainer = Teal90,
    background = Neutral10,
    onBackground = Neutral90,
    surface = Neutral10,
    onSurface = Neutral90,
    surfaceVariant = NeutralVariant30,
    onSurfaceVariant = NeutralVariant90,
    error = Red80
)

private val OceanColors = lightColorScheme(
    primary = OceanPrimary,
    primaryContainer = OceanContainer,
    onPrimaryContainer = OceanOnContainer,
    background = Neutral99,
    surface = Neutral99
)

private val SunsetColors = lightColorScheme(
    primary = SunsetPrimary,
    primaryContainer = SunsetContainer,
    onPrimaryContainer = SunsetOnContainer,
    background = Neutral99,
    surface = Neutral99
)

private val AmoledColors = darkColorScheme(
    primary = AmoledPrimary,
    background = AmoledBackground,
    surface = AmoledSurface,
    surfaceVariant = AmoledSurface
)

@Composable
fun HavaTheme(
    themeStyle: AppThemeStyle = AppThemeStyle.SYSTEM,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = when (themeStyle) {
        AppThemeStyle.SYSTEM -> if (darkTheme) DarkColors else LightColors
        AppThemeStyle.OCEAN -> OceanColors
        AppThemeStyle.SUNSET -> SunsetColors
        AppThemeStyle.DARK_AMOLED -> AmoledColors
    }

    MaterialTheme(
        colorScheme = colors,
        typography = HavaTypography,
        content = content
    )
}
