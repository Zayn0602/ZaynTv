package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.data.model.CustomThemeColors
import com.example.data.model.ThemeType

// ZaynTV Dark Theme Palette (Default)
val ZaynDeepBackground = Color(0xFF070A10)
val ZaynSurface = Color(0xFF101726)
val ZaynCardBackground = Color(0xFF172033)
val ZaynCardHover = Color(0xFF222F4C)
val ZaynBorder = Color(0xFF283858)

// Vibrant Accent Colors
val ZaynCyanPrimary = Color(0xFF00E5FF)
val ZaynCyanSecondary = Color(0xFF18FFFF)
val ZaynViolet = Color(0xFF7C4DFF)
val ZaynVioletDeep = Color(0xFF536DFE)
val ZaynLiveRed = Color(0xFFFF2A6D)
val ZaynLivePulse = Color(0xFFFF5252)

// Text and Iconography
val ZaynTextPrimary = Color(0xFFFFFFFF)
val ZaynTextSecondary = Color(0xFF94A3B8)
val ZaynTextMuted = Color(0xFF64748B)

// Focus State for D-pad & Android TV
val ZaynFocusBorder = Color(0xFF00E5FF)
val ZaynFocusGlow = Color(0x3300E5FF)

data class AppThemeColors(
    val primary: Color,
    val secondary: Color,
    val background: Color,
    val surface: Color,
    val card: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val focusBorder: Color,
    val isDark: Boolean
)

object ThemePaletteFactory {
    fun getColors(themeType: ThemeType, custom: CustomThemeColors, systemDark: Boolean): AppThemeColors {
        return when (themeType) {
            ThemeType.ZAYN_DARK -> AppThemeColors(
                primary = Color(0xFF00E5FF),
                secondary = Color(0xFF7C4DFF),
                background = Color(0xFF070A10),
                surface = Color(0xFF101726),
                card = Color(0xFF172033),
                textPrimary = Color(0xFFFFFFFF),
                textSecondary = Color(0xFF94A3B8),
                focusBorder = Color(0xFF00E5FF),
                isDark = true
            )
            ThemeType.MIDNIGHT_BLACK -> AppThemeColors(
                primary = Color(0xFF38BDF8),
                secondary = Color(0xFF818CF8),
                background = Color(0xFF020617),
                surface = Color(0xFF0F172A),
                card = Color(0xFF1E293B),
                textPrimary = Color(0xFFF8FAFC),
                textSecondary = Color(0xFF94A3B8),
                focusBorder = Color(0xFF38BDF8),
                isDark = true
            )
            ThemeType.AMOLED_BLACK -> AppThemeColors(
                primary = Color(0xFF00F5D4),
                secondary = Color(0xFF7B2CBF),
                background = Color(0xFF000000),
                surface = Color(0xFF0A0A0A),
                card = Color(0xFF141414),
                textPrimary = Color(0xFFFFFFFF),
                textSecondary = Color(0xFFA3A3A3),
                focusBorder = Color(0xFF00F5D4),
                isDark = true
            )
            ThemeType.OCEAN_BLUE -> AppThemeColors(
                primary = Color(0xFF64FFDA),
                secondary = Color(0xFF00B4D8),
                background = Color(0xFF0A192F),
                surface = Color(0xFF112240),
                card = Color(0xFF1B3A60),
                textPrimary = Color(0xFFCCD6F6),
                textSecondary = Color(0xFF8892B0),
                focusBorder = Color(0xFF64FFDA),
                isDark = true
            )
            ThemeType.ROYAL_PURPLE -> AppThemeColors(
                primary = Color(0xFFC084FC),
                secondary = Color(0xFFF43F5E),
                background = Color(0xFF13091B),
                surface = Color(0xFF220F30),
                card = Color(0xFF311545),
                textPrimary = Color(0xFFFAF5FF),
                textSecondary = Color(0xFFD8B4FE),
                focusBorder = Color(0xFFC084FC),
                isDark = true
            )
            ThemeType.EMERALD -> AppThemeColors(
                primary = Color(0xFF34D399),
                secondary = Color(0xFF10B981),
                background = Color(0xFF041710),
                surface = Color(0xFF0A2B1F),
                card = Color(0xFF123F2F),
                textPrimary = Color(0xFFECFDF5),
                textSecondary = Color(0xFFA7F3D0),
                focusBorder = Color(0xFF34D399),
                isDark = true
            )
            ThemeType.RED_CINEMA -> AppThemeColors(
                primary = Color(0xFFE50914),
                secondary = Color(0xFFFF5252),
                background = Color(0xFF101010),
                surface = Color(0xFF181818),
                card = Color(0xFF242424),
                textPrimary = Color(0xFFFFFFFF),
                textSecondary = Color(0xFFB3B3B3),
                focusBorder = Color(0xFFE50914),
                isDark = true
            )
            ThemeType.LIGHT -> AppThemeColors(
                primary = Color(0xFF0284C7),
                secondary = Color(0xFF6366F1),
                background = Color(0xFFF8FAFC),
                surface = Color(0xFFFFFFFF),
                card = Color(0xFFEDF2F7),
                textPrimary = Color(0xFF0F172A),
                textSecondary = Color(0xFF64748B),
                focusBorder = Color(0xFF0284C7),
                isDark = false
            )
            ThemeType.SYSTEM -> {
                if (systemDark) {
                    getColors(ThemeType.ZAYN_DARK, custom, true)
                } else {
                    getColors(ThemeType.LIGHT, custom, false)
                }
            }
            ThemeType.CUSTOM -> AppThemeColors(
                primary = Color(custom.primaryColor),
                secondary = Color(custom.accentColor),
                background = Color(custom.backgroundColor),
                surface = Color(custom.cardColor),
                card = Color(custom.cardColor),
                textPrimary = Color(custom.textColor),
                textSecondary = Color(custom.textColor).copy(alpha = 0.7f),
                focusBorder = Color(custom.focusColor),
                isDark = true
            )
        }
    }
}
