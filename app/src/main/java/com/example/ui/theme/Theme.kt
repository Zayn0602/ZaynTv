package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.data.model.CustomThemeColors
import com.example.data.model.ThemeType

val LocalAppThemeColors = staticCompositionLocalOf {
    ThemePaletteFactory.getColors(ThemeType.ZAYN_DARK, CustomThemeColors(), true)
}

@Composable
fun ZaynTVTheme(
    themeType: ThemeType = ThemeType.ZAYN_DARK,
    customThemeColors: CustomThemeColors = CustomThemeColors(),
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val appColors = ThemePaletteFactory.getColors(themeType, customThemeColors, systemDark)

    val colorScheme = if (appColors.isDark) {
        darkColorScheme(
            primary = appColors.primary,
            onPrimary = appColors.background,
            primaryContainer = appColors.secondary,
            onPrimaryContainer = appColors.textPrimary,
            secondary = appColors.secondary,
            onSecondary = appColors.textPrimary,
            background = appColors.background,
            onBackground = appColors.textPrimary,
            surface = appColors.surface,
            onSurface = appColors.textPrimary,
            surfaceVariant = appColors.card,
            onSurfaceVariant = appColors.textSecondary,
            error = ZaynLiveRed,
            onError = appColors.textPrimary
        )
    } else {
        lightColorScheme(
            primary = appColors.primary,
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = appColors.secondary,
            onPrimaryContainer = Color(0xFFFFFFFF),
            secondary = appColors.secondary,
            onSecondary = Color(0xFFFFFFFF),
            background = appColors.background,
            onBackground = appColors.textPrimary,
            surface = appColors.surface,
            onSurface = appColors.textPrimary,
            surfaceVariant = appColors.card,
            onSurfaceVariant = appColors.textSecondary,
            error = ZaynLiveRed,
            onError = Color(0xFFFFFFFF)
        )
    }

    CompositionLocalProvider(LocalAppThemeColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    ZaynTVTheme(content = content)
}
