package com.raila.app

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val RoyalPurpleLightColors = lightColorScheme(
    primary = Color(0xFF6750A4),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF625B71),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = Color(0xFF7D5260),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFD8E4),
    onTertiaryContainer = Color(0xFF31111D),
    background = Color(0xFFFEF7FF),
    onBackground = Color(0xFF1D1B20),
    surface = Color(0xFFFEF7FF),
    onSurface = Color(0xFF1D1B20),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    surfaceContainer = Color(0xFFF3EDF7),
    surfaceContainerHigh = Color(0xFFECE6F0),
    surfaceContainerLow = Color(0xFFF7F2FA),
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0),
    error = Color(0xFFB3261E),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B)
)

private val RoyalPurpleDarkColors = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFFCCC2DC),
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = Color(0xFFEFB8C8),
    onTertiary = Color(0xFF492532),
    tertiaryContainer = Color(0xFF633B48),
    onTertiaryContainer = Color(0xFFFFD8E4),
    background = Color(0xFF141218),
    onBackground = Color(0xFFE6E0E9),
    surface = Color(0xFF141218),
    onSurface = Color(0xFFE6E0E9),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    surfaceContainer = Color(0xFF211F26),
    surfaceContainerHigh = Color(0xFF2B2930),
    surfaceContainerLow = Color(0xFF1D1B20),
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F),
    error = Color(0xFFF2B8B5),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC)
)

private val OceanBlueLightColors = lightColorScheme(
    primary = Color(0xFF00639B),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFCEE5FF),
    onPrimaryContainer = Color(0xFF001D33),
    secondary = Color(0xFF51606F),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD5E4F7),
    onSecondaryContainer = Color(0xFF0E1D2A),
    tertiary = Color(0xFF68587A),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFEFDBFF),
    onTertiaryContainer = Color(0xFF231533),
    background = Color(0xFFF8F9FF),
    onBackground = Color(0xFF191C20),
    surface = Color(0xFFF8F9FF),
    onSurface = Color(0xFF191C20),
    surfaceVariant = Color(0xFFDEE3EB),
    onSurfaceVariant = Color(0xFF42474E),
    surfaceContainer = Color(0xFFEBEFF6),
    surfaceContainerHigh = Color(0xFFE5E9F0),
    surfaceContainerLow = Color(0xFFF1F5FC),
    outline = Color(0xFF72777F),
    outlineVariant = Color(0xFFC2C7CF),
    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

private val OceanBlueDarkColors = darkColorScheme(
    primary = Color(0xFF97CBFF),
    onPrimary = Color(0xFF003353),
    primaryContainer = Color(0xFF004A76),
    onPrimaryContainer = Color(0xFFCEE5FF),
    secondary = Color(0xFFB9C8DA),
    onSecondary = Color(0xFF233240),
    secondaryContainer = Color(0xFF3A4857),
    onSecondaryContainer = Color(0xFFD5E4F7),
    tertiary = Color(0xFFD3BFE6),
    onTertiary = Color(0xFF392A49),
    tertiaryContainer = Color(0xFF504061),
    onTertiaryContainer = Color(0xFFEFDBFF),
    background = Color(0xFF111418),
    onBackground = Color(0xFFE1E2E8),
    surface = Color(0xFF111418),
    onSurface = Color(0xFFE1E2E8),
    surfaceVariant = Color(0xFF42474E),
    onSurfaceVariant = Color(0xFFC2C7CF),
    surfaceContainer = Color(0xFF1D2024),
    surfaceContainerHigh = Color(0xFF282A2F),
    surfaceContainerLow = Color(0xFF191C20),
    outline = Color(0xFF8C9199),
    outlineVariant = Color(0xFF42474E),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

private val WarmOrangeLightColors = lightColorScheme(
    primary = Color(0xFF9C4100),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDBCF),
    onPrimaryContainer = Color(0xFF341000),
    secondary = Color(0xFF77574B),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDBCE),
    onSecondaryContainer = Color(0xFF2C160C),
    tertiary = Color(0xFF6A5E2F),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF4E2A7),
    onTertiaryContainer = Color(0xFF221B00),
    background = Color(0xFFFFF8F6),
    onBackground = Color(0xFF231A16),
    surface = Color(0xFFFFF8F6),
    onSurface = Color(0xFF231A16),
    surfaceVariant = Color(0xFFF5DED6),
    onSurfaceVariant = Color(0xFF53433E),
    surfaceContainer = Color(0xFFF6ECE8),
    surfaceContainerHigh = Color(0xFFF0E6E2),
    surfaceContainerLow = Color(0xFFFCF2EE),
    outline = Color(0xFF85736D),
    outlineVariant = Color(0xFFD8C2BB),
    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

private val WarmOrangeDarkColors = darkColorScheme(
    primary = Color(0xFFFFB59B),
    onPrimary = Color(0xFF552000),
    primaryContainer = Color(0xFF782F00),
    onPrimaryContainer = Color(0xFFFFDBCF),
    secondary = Color(0xFFE7BEAF),
    onSecondary = Color(0xFF442A20),
    secondaryContainer = Color(0xFF5D4034),
    onSecondaryContainer = Color(0xFFFFDBCE),
    tertiary = Color(0xFFD7C68D),
    onTertiary = Color(0xFF3A3005),
    tertiaryContainer = Color(0xFF51461A),
    onTertiaryContainer = Color(0xFFF4E2A7),
    background = Color(0xFF1A120E),
    onBackground = Color(0xFFF1DFD9),
    surface = Color(0xFF1A120E),
    onSurface = Color(0xFFF1DFD9),
    surfaceVariant = Color(0xFF53433E),
    onSurfaceVariant = Color(0xFFD8C2BB),
    surfaceContainer = Color(0xFF271E1A),
    surfaceContainerHigh = Color(0xFF322824),
    surfaceContainerLow = Color(0xFF231A16),
    outline = Color(0xFFA08C86),
    outlineVariant = Color(0xFF53433E),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

private val BurgundyLightColors = lightColorScheme(
    primary = Color(0xFF8B1A40),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFD9E2),
    onPrimaryContainer = Color(0xFF3B0016),
    secondary = Color(0xFF74565F),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFD9E2),
    onSecondaryContainer = Color(0xFF2B151C),
    tertiary = Color(0xFF7C5635),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDCC1),
    onTertiaryContainer = Color(0xFF2E1500),
    background = Color(0xFFFFF8F8),
    onBackground = Color(0xFF201A1B),
    surface = Color(0xFFFFF8F8),
    onSurface = Color(0xFF201A1B),
    surfaceVariant = Color(0xFFF2DDE1),
    onSurfaceVariant = Color(0xFF514347),
    surfaceContainer = Color(0xFFF6EBEF),
    surfaceContainerHigh = Color(0xFFF0E5EA),
    surfaceContainerLow = Color(0xFFFCF1F5),
    outline = Color(0xFF837377),
    outlineVariant = Color(0xFFD5C2C6),
    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

private val BurgundyDarkColors = darkColorScheme(
    primary = Color(0xFFFFB1C8),
    onPrimary = Color(0xFF550326),
    primaryContainer = Color(0xFF700633),
    onPrimaryContainer = Color(0xFFFFD9E2),
    secondary = Color(0xFFE2BDC6),
    onSecondary = Color(0xFF422931),
    secondaryContainer = Color(0xFF5A3F47),
    onSecondaryContainer = Color(0xFFFFD9E2),
    tertiary = Color(0xFFEFBD94),
    onTertiary = Color(0xFF472A0B),
    tertiaryContainer = Color(0xFF613F1F),
    onTertiaryContainer = Color(0xFFFFDCC1),
    background = Color(0xFF191113),
    onBackground = Color(0xFFEFE0E2),
    surface = Color(0xFF191113),
    onSurface = Color(0xFFEFE0E2),
    surfaceVariant = Color(0xFF514347),
    onSurfaceVariant = Color(0xFFD5C2C6),
    surfaceContainer = Color(0xFF251D20),
    surfaceContainerHigh = Color(0xFF30282A),
    surfaceContainerLow = Color(0xFF21191C),
    outline = Color(0xFF9E8C90),
    outlineVariant = Color(0xFF514347),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

private val ForestGreenLightColors = lightColorScheme(
    primary = Color(0xFF006C4C),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF8CF8C7),
    onPrimaryContainer = Color(0xFF002114),
    secondary = Color(0xFF4D6356),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD0E8D7),
    onSecondaryContainer = Color(0xFF0B1F15),
    tertiary = Color(0xFF386567),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFBCEBEB),
    onTertiaryContainer = Color(0xFF002021),
    background = Color(0xFFF6FBF6),
    onBackground = Color(0xFF181D1A),
    surface = Color(0xFFF6FBF6),
    onSurface = Color(0xFF181D1A),
    surfaceVariant = Color(0xFFDBE5DD),
    onSurfaceVariant = Color(0xFF404943),
    surfaceContainer = Color(0xFFEBEFEA),
    surfaceContainerHigh = Color(0xFFE5EAE5),
    surfaceContainerLow = Color(0xFFF0F5F0),
    outline = Color(0xFF707973),
    outlineVariant = Color(0xFFBFC9C2),
    error = Color(0xFF2E6B4F),
    errorContainer = Color(0xFFD8EEDF),
    onErrorContainer = Color(0xFF0F3824)
)

private val ForestGreenDarkColors = darkColorScheme(
    primary = Color(0xFF6FDBAC),
    onPrimary = Color(0xFF003825),
    primaryContainer = Color(0xFF005238),
    onPrimaryContainer = Color(0xFF8CF8C7),
    secondary = Color(0xFFB4CCBC),
    onSecondary = Color(0xFF20352A),
    secondaryContainer = Color(0xFF364B3F),
    onSecondaryContainer = Color(0xFFD0E8D7),
    tertiary = Color(0xFFA0CFCF),
    onTertiary = Color(0xFF003738),
    tertiaryContainer = Color(0xFF1E4D4F),
    onTertiaryContainer = Color(0xFFBCEBEB),
    background = Color(0xFF0F1512),
    onBackground = Color(0xFFE0E3DE),
    surface = Color(0xFF0F1512),
    onSurface = Color(0xFFE0E3DE),
    surfaceVariant = Color(0xFF1E2B23),
    onSurfaceVariant = Color(0xFFC0C9C2),
    surfaceContainer = Color(0xFF16211B),
    surfaceContainerHigh = Color(0xFF1B2821),
    surfaceContainerLow = Color(0xFF121A15),
    outline = Color(0xFF8A938C),
    outlineVariant = Color(0xFF404943),
    error = Color(0xFF8BD3AA),
    errorContainer = Color(0xFF1B402E),
    onErrorContainer = Color(0xFFB8EACC)
)

@Composable
fun RailaTheme(
    themeColor: AppColorTheme = AppColorTheme.ROYAL_PURPLE,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme: ColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> when (themeColor) {
            AppColorTheme.ROYAL_PURPLE -> RoyalPurpleDarkColors
            AppColorTheme.OCEAN_BLUE -> OceanBlueDarkColors
            AppColorTheme.WARM_ORANGE -> WarmOrangeDarkColors
            AppColorTheme.BURGUNDY -> BurgundyDarkColors
            AppColorTheme.FOREST_GREEN -> ForestGreenDarkColors
        }
        else -> when (themeColor) {
            AppColorTheme.ROYAL_PURPLE -> RoyalPurpleLightColors
            AppColorTheme.OCEAN_BLUE -> OceanBlueLightColors
            AppColorTheme.WARM_ORANGE -> WarmOrangeLightColors
            AppColorTheme.BURGUNDY -> BurgundyLightColors
            AppColorTheme.FOREST_GREEN -> ForestGreenLightColors
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
