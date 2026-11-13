package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = PinkDarkPrimary,
    onPrimary = PinkDarkOnPrimary,
    primaryContainer = PinkDarkPrimaryContainer,
    onPrimaryContainer = PinkDarkOnPrimaryContainer,
    secondary = PinkDarkSecondary,
    onSecondary = PinkDarkOnSecondary,
    secondaryContainer = PinkDarkSecondaryContainer,
    onSecondaryContainer = PinkDarkOnSecondaryContainer,
    tertiary = PinkDarkTertiary,
    onTertiary = PinkDarkOnTertiary,
    tertiaryContainer = PinkDarkTertiaryContainer,
    onTertiaryContainer = PinkDarkOnTertiaryContainer,
    background = PinkDarkBackground,
    onBackground = PinkDarkOnBackground,
    surface = PinkDarkSurface,
    onSurface = PinkDarkOnSurface,
    surfaceVariant = PinkDarkSurfaceVariant,
    onSurfaceVariant = PinkDarkOnSurfaceVariant,
    outline = PinkDarkOutline,
    outlineVariant = PinkDarkOutlineVariant
)

private val LightColorScheme = lightColorScheme(
    primary = PinkLightPrimary,
    onPrimary = PinkLightOnPrimary,
    primaryContainer = PinkLightPrimaryContainer,
    onPrimaryContainer = PinkLightOnPrimaryContainer,
    secondary = PinkLightSecondary,
    onSecondary = PinkLightOnSecondary,
    secondaryContainer = PinkLightSecondaryContainer,
    onSecondaryContainer = PinkLightOnSecondaryContainer,
    tertiary = PinkLightTertiary,
    onTertiary = PinkLightOnTertiary,
    tertiaryContainer = PinkLightTertiaryContainer,
    onTertiaryContainer = PinkLightOnTertiaryContainer,
    background = PinkLightBackground,
    onBackground = PinkLightOnBackground,
    surface = PinkLightSurface,
    onSurface = PinkLightOnSurface,
    surfaceVariant = PinkLightSurfaceVariant,
    onSurfaceVariant = PinkLightOnSurfaceVariant,
    outline = PinkLightOutline,
    outlineVariant = PinkLightOutlineVariant
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Keep dynamicColor false to strictly preserve the pink/deep-berry aesthetic with no blue
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
