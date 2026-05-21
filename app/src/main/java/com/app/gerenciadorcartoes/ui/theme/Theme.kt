package com.app.gerenciadorcartoes.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = MdThemeDarkPrimary,
    onPrimary = MdThemeDarkOnPrimary,
    primaryContainer = MdThemeDarkPrimaryContainer,
    onPrimaryContainer = MdThemeDarkOnPrimaryContainer,
    secondary = MdThemeDarkSecondary,
    onSecondary = MdThemeDarkOnSecondary,
    secondaryContainer = MdThemeDarkSecondaryContainer,
    onSecondaryContainer = MdThemeDarkOnSecondaryContainer,
    tertiary = MdThemeDarkTertiary,
    onTertiary = MdThemeDarkOnTertiary,
    tertiaryContainer = MdThemeDarkTertiaryContainer,
    onTertiaryContainer = MdThemeDarkOnTertiaryContainer,
    error = MdThemeDarkError,
    onError = MdThemeDarkOnError,
    errorContainer = MdThemeDarkErrorContainer,
    onErrorContainer = MdThemeDarkOnErrorContainer,
    background = MdThemeDarkBackground,
    onBackground = MdThemeDarkOnBackground,
    surface = MdThemeDarkSurface,
    onSurface = MdThemeDarkOnSurface,
    surfaceVariant = MdThemeDarkSurfaceVariant,
    onSurfaceVariant = MdThemeDarkOnSurfaceVariant,
    outline = MdThemeDarkOutline,
    inverseOnSurface = MdThemeDarkInverseOnSurface,
    inverseSurface = MdThemeDarkInverseSurface,
    inversePrimary = MdThemeDarkInversePrimary,
)

private val LightColorScheme = lightColorScheme(
    primary = MdThemeLightPrimary,
    onPrimary = MdThemeLightOnPrimary,
    primaryContainer = MdThemeLightPrimaryContainer,
    onPrimaryContainer = MdThemeLightOnPrimaryContainer,
    secondary = MdThemeLightSecondary,
    onSecondary = MdThemeLightOnSecondary,
    secondaryContainer = MdThemeLightSecondaryContainer,
    onSecondaryContainer = MdThemeLightOnSecondaryContainer,
    tertiary = MdThemeLightTertiary,
    onTertiary = MdThemeLightOnTertiary,
    tertiaryContainer = MdThemeLightTertiaryContainer,
    onTertiaryContainer = MdThemeLightOnTertiaryContainer,
    error = MdThemeLightError,
    onError = MdThemeLightOnError,
    errorContainer = MdThemeLightErrorContainer,
    onErrorContainer = MdThemeLightOnErrorContainer,
    background = MdThemeLightBackground,
    onBackground = MdThemeLightOnBackground,
    surface = MdThemeLightSurface,
    onSurface = MdThemeLightOnSurface,
    surfaceVariant = MdThemeLightSurfaceVariant,
    onSurfaceVariant = MdThemeLightOnSurfaceVariant,
    outline = MdThemeLightOutline,
    inverseOnSurface = MdThemeLightInverseOnSurface,
    inverseSurface = MdThemeLightInverseSurface,
    inversePrimary = MdThemeLightInversePrimary,
)

// Ponto de entrada do tema.
// Material 3 via BOM 2026.05.00 — completamente estável; TopAppBar requer @file:OptIn com Kotlin ≥ 2.x.
// Spacing e IconSize disponíveis via LocalSpacing.current / LocalIconSize.current.
@Composable
fun GerenciadorCartoesTheme(
    darkTheme    : Boolean = isSystemInDarkTheme(),
    dynamicColor : Boolean = false,
    content      : @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    CompositionLocalProvider(
        LocalSpacing  provides Spacing(),
        LocalIconSize provides IconSize(),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = Typography,
            shapes      = Shapes,
            content     = content,
        )
    }
}