package JunZi.Pixiv.ui.theme

import JunZi.Pixiv.PuxivThemeMode
import JunZi.Pixiv.PuxivThemePalette
import JunZi.Pixiv.PuxivCustomPalette
import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = Color(0xFF1A9AFC),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE5F4FF),
    onPrimaryContainer = Color(0xFF00507E),
    secondary = Color(0xFF5F6872),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFECEFF2),
    onSecondaryContainer = Color(0xFF232A31),
    tertiary = Color(0xFFFF5A7A),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE8EE),
    onTertiaryContainer = Color(0xFF6E1028),
    background = Color(0xFFF6F6F6),
    onBackground = Color(0xFF1F2328),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1F2328),
    surfaceVariant = Color(0xFFF0F2F5),
    onSurfaceVariant = Color(0xFF66717C),
    outline = Color(0xFFC9CED4),
    outlineVariant = Color(0xFFE3E6EA),
    error = Color(0xFFB3261E),
    errorContainer = Color(0xFFFFEDEA),
    onErrorContainer = Color(0xFF8C1D18),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF78C8FF),
    onPrimary = Color(0xFF003A63),
    primaryContainer = Color(0xFF005A91),
    onPrimaryContainer = Color(0xFFE1F2FF),
    secondary = Color(0xFFB9C5CE),
    onSecondary = Color(0xFF24323B),
    secondaryContainer = Color(0xFF394954),
    onSecondaryContainer = Color(0xFFD5E3EC),
    tertiary = Color(0xFFFFB2C7),
    onTertiary = Color(0xFF5D1130),
    tertiaryContainer = Color(0xFF7C2946),
    onTertiaryContainer = Color(0xFFFFD9E4),
    background = Color(0xFF111315),
    onBackground = Color(0xFFE7EAED),
    surface = Color(0xFF1B1D20),
    onSurface = Color(0xFFE7EAED),
    surfaceVariant = Color(0xFF2D3035),
    onSurfaceVariant = Color(0xFFC4C9CE),
    outline = Color(0xFF858B92),
    outlineVariant = Color(0xFF42474D),
)

private val SakuraLightColors = lightColorScheme(
    primary = Color(0xFFD7336F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFD9E5),
    onPrimaryContainer = Color(0xFF52001F),
    secondary = Color(0xFF75565F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFD9E2),
    onSecondaryContainer = Color(0xFF2B151C),
    tertiary = Color(0xFF755A00),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE08B),
    onTertiaryContainer = Color(0xFF241A00),
    background = Color(0xFFFFF8F9),
    onBackground = Color(0xFF21191C),
    surface = Color.White,
    onSurface = Color(0xFF21191C),
    surfaceVariant = Color(0xFFF3DDE3),
    onSurfaceVariant = Color(0xFF51434A),
    outline = Color(0xFF83737A),
    outlineVariant = Color(0xFFD6C2C8),
    error = Color(0xFFB3261E),
    errorContainer = Color(0xFFFFEDEA),
    onErrorContainer = Color(0xFF8C1D18),
)

private val SakuraDarkColors = darkColorScheme(
    primary = Color(0xFFFFB1C8),
    onPrimary = Color(0xFF65002B),
    primaryContainer = Color(0xFF970044),
    onPrimaryContainer = Color(0xFFFFD9E5),
    secondary = Color(0xFFE4BDC7),
    onSecondary = Color(0xFF432931),
    secondaryContainer = Color(0xFF5B3F47),
    onSecondaryContainer = Color(0xFFFFD9E2),
    tertiary = Color(0xFFECC56C),
    onTertiary = Color(0xFF3E2E00),
    tertiaryContainer = Color(0xFF5A4300),
    onTertiaryContainer = Color(0xFFFFE08B),
    background = Color(0xFF181113),
    onBackground = Color(0xFFF0DDE2),
    surface = Color(0xFF21191C),
    onSurface = Color(0xFFF0DDE2),
    surfaceVariant = Color(0xFF51434A),
    onSurfaceVariant = Color(0xFFD6C2C8),
    outline = Color(0xFFA08C93),
    outlineVariant = Color(0xFF51434A),
)

private val MintLightColors = lightColorScheme(
    primary = Color(0xFF006C5B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF82F8DA),
    onPrimaryContainer = Color(0xFF002019),
    secondary = Color(0xFF4A635C),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCDE8DF),
    onSecondaryContainer = Color(0xFF062019),
    tertiary = Color(0xFF3F5F90),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD5E3FF),
    onTertiaryContainer = Color(0xFF001B3D),
    background = Color(0xFFF4FBF7),
    onBackground = Color(0xFF161D1A),
    surface = Color.White,
    onSurface = Color(0xFF161D1A),
    surfaceVariant = Color(0xFFDCE5DF),
    onSurfaceVariant = Color(0xFF404944),
    outline = Color(0xFF70817A),
    outlineVariant = Color(0xFFC0C9C3),
    error = Color(0xFFB3261E),
    errorContainer = Color(0xFFFFEDEA),
    onErrorContainer = Color(0xFF8C1D18),
)

private val MintDarkColors = darkColorScheme(
    primary = Color(0xFF65DBBF),
    onPrimary = Color(0xFF00382E),
    primaryContainer = Color(0xFF005143),
    onPrimaryContainer = Color(0xFF82F8DA),
    secondary = Color(0xFFB2CCC3),
    onSecondary = Color(0xFF1D352F),
    secondaryContainer = Color(0xFF344C45),
    onSecondaryContainer = Color(0xFFCDE8DF),
    tertiary = Color(0xFFA9C8FF),
    onTertiary = Color(0xFF07305F),
    tertiaryContainer = Color(0xFF264776),
    onTertiaryContainer = Color(0xFFD5E3FF),
    background = Color(0xFF0E1512),
    onBackground = Color(0xFFDDE5DF),
    surface = Color(0xFF171D1A),
    onSurface = Color(0xFFDDE5DF),
    surfaceVariant = Color(0xFF404944),
    onSurfaceVariant = Color(0xFFC0C9C3),
    outline = Color(0xFF8A938D),
    outlineVariant = Color(0xFF404944),
)

private val VioletLightColors = lightColorScheme(
    primary = Color(0xFF6750A4),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF625B71),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = Color(0xFF7D5260),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD8E4),
    onTertiaryContainer = Color(0xFF31111D),
    background = Color(0xFFFFFBFF),
    onBackground = Color(0xFF1C1B1F),
    surface = Color.White,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0),
    error = Color(0xFFB3261E),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
)

private val VioletDarkColors = darkColorScheme(
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
    background = Color(0xFF131216),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F),
)

private val AmberLightColors = lightColorScheme(
    primary = Color(0xFF825500),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDDB1),
    onPrimaryContainer = Color(0xFF291800),
    secondary = Color(0xFF6F5B40),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFADEBC),
    onSecondaryContainer = Color(0xFF271904),
    tertiary = Color(0xFF51643F),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD3EABB),
    onTertiaryContainer = Color(0xFF102004),
    background = Color(0xFFFFF8F2),
    onBackground = Color(0xFF201A13),
    surface = Color.White,
    onSurface = Color(0xFF201A13),
    surfaceVariant = Color(0xFFF0E0CF),
    onSurfaceVariant = Color(0xFF4F4539),
    outline = Color(0xFF817568),
    outlineVariant = Color(0xFFD3C4B4),
    error = Color(0xFFB3261E),
    errorContainer = Color(0xFFFFEDEA),
    onErrorContainer = Color(0xFF8C1D18),
)

private val AmberDarkColors = darkColorScheme(
    primary = Color(0xFFFFB957),
    onPrimary = Color(0xFF452B00),
    primaryContainer = Color(0xFF624000),
    onPrimaryContainer = Color(0xFFFFDDB1),
    secondary = Color(0xFFDDC2A2),
    onSecondary = Color(0xFF3E2D16),
    secondaryContainer = Color(0xFF56442A),
    onSecondaryContainer = Color(0xFFFADEBC),
    tertiary = Color(0xFFB8CEA1),
    onTertiary = Color(0xFF243515),
    tertiaryContainer = Color(0xFF3A4C29),
    onTertiaryContainer = Color(0xFFD3EABB),
    background = Color(0xFF18120B),
    onBackground = Color(0xFFEDE1D4),
    surface = Color(0xFF211A12),
    onSurface = Color(0xFFEDE1D4),
    surfaceVariant = Color(0xFF4F4539),
    onSurfaceVariant = Color(0xFFD3C4B4),
    outline = Color(0xFF9C8E80),
    outlineVariant = Color(0xFF4F4539),
)

private val SlateLightColors = lightColorScheme(
    primary = Color(0xFF006B7D),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB0ECFF),
    onPrimaryContainer = Color(0xFF001F27),
    secondary = Color(0xFF4D6269),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD0E7EF),
    onSecondaryContainer = Color(0xFF081F25),
    tertiary = Color(0xFF62597C),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE8DDFF),
    onTertiaryContainer = Color(0xFF1E1635),
    background = Color(0xFFF5FAFC),
    onBackground = Color(0xFF171C1E),
    surface = Color.White,
    onSurface = Color(0xFF171C1E),
    surfaceVariant = Color(0xFFDCE4E8),
    onSurfaceVariant = Color(0xFF40484C),
    outline = Color(0xFF70787C),
    outlineVariant = Color(0xFFC0C8CC),
    error = Color(0xFFB3261E),
    errorContainer = Color(0xFFFFEDEA),
    onErrorContainer = Color(0xFF8C1D18),
)

private val SlateDarkColors = darkColorScheme(
    primary = Color(0xFF5BD7F0),
    onPrimary = Color(0xFF00363F),
    primaryContainer = Color(0xFF004E5B),
    onPrimaryContainer = Color(0xFFB0ECFF),
    secondary = Color(0xFFB4CAD1),
    onSecondary = Color(0xFF1F343A),
    secondaryContainer = Color(0xFF354A51),
    onSecondaryContainer = Color(0xFFD0E7EF),
    tertiary = Color(0xFFCBC0E9),
    onTertiary = Color(0xFF332B4B),
    tertiaryContainer = Color(0xFF4A4163),
    onTertiaryContainer = Color(0xFFE8DDFF),
    background = Color(0xFF0F1416),
    onBackground = Color(0xFFDFE3E6),
    surface = Color(0xFF171C1E),
    onSurface = Color(0xFFDFE3E6),
    surfaceVariant = Color(0xFF40484C),
    onSurfaceVariant = Color(0xFFC0C8CC),
    outline = Color(0xFF8A9296),
    outlineVariant = Color(0xFF40484C),
)

internal fun PuxivThemePalette.puxivColorScheme(
    darkTheme: Boolean,
    customPalette: PuxivCustomPalette = PuxivCustomPalette(),
): ColorScheme {
    return when (this) {
        PuxivThemePalette.Puxiv -> if (darkTheme) DarkColors else LightColors
        PuxivThemePalette.Sakura -> if (darkTheme) SakuraDarkColors else SakuraLightColors
        PuxivThemePalette.Mint -> if (darkTheme) MintDarkColors else MintLightColors
        PuxivThemePalette.Violet -> if (darkTheme) VioletDarkColors else VioletLightColors
        PuxivThemePalette.Amber -> if (darkTheme) AmberDarkColors else AmberLightColors
        PuxivThemePalette.Slate -> if (darkTheme) SlateDarkColors else SlateLightColors
        PuxivThemePalette.Custom -> customPalette.toColorScheme(darkTheme)
    }
}

private fun PuxivCustomPalette.toColorScheme(darkTheme: Boolean): ColorScheme {
    val fallback = PuxivThemePalette.Puxiv.puxivColorScheme(darkTheme)
    val primary = primaryHex.toColorOr(fallback.primary)
    val secondary = secondaryHex.toColorOr(fallback.secondary)
    val tertiary = tertiaryHex.toColorOr(fallback.tertiary)
    val background = backgroundHex.toColorOr(fallback.background)
    val surface = surfaceHex.toColorOr(fallback.surface)
    val surfaceVariant = surface.blendWith(if (darkTheme) Color.White else Color.Black, if (darkTheme) 0.14f else 0.06f)
    val primaryContainer = primary.blendWith(background, if (darkTheme) 0.28f else 0.78f)
    val secondaryContainer = secondary.blendWith(background, if (darkTheme) 0.28f else 0.78f)
    val tertiaryContainer = tertiary.blendWith(background, if (darkTheme) 0.28f else 0.78f)

    return if (darkTheme) {
        darkColorScheme(
            primary = primary,
            onPrimary = primary.readableOnColor(),
            primaryContainer = primaryContainer,
            onPrimaryContainer = primaryContainer.readableOnColor(),
            secondary = secondary,
            onSecondary = secondary.readableOnColor(),
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = secondaryContainer.readableOnColor(),
            tertiary = tertiary,
            onTertiary = tertiary.readableOnColor(),
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = tertiaryContainer.readableOnColor(),
            background = background,
            onBackground = background.readableOnColor(),
            surface = surface,
            onSurface = surface.readableOnColor(),
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = surfaceVariant.readableOnColor(),
            outline = secondary.blendWith(surface.readableOnColor(), 0.42f),
            outlineVariant = secondary.blendWith(surface, 0.42f),
            error = fallback.error,
            errorContainer = fallback.errorContainer,
            onErrorContainer = fallback.onErrorContainer,
        )
    } else {
        lightColorScheme(
            primary = primary,
            onPrimary = primary.readableOnColor(),
            primaryContainer = primaryContainer,
            onPrimaryContainer = primaryContainer.readableOnColor(),
            secondary = secondary,
            onSecondary = secondary.readableOnColor(),
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = secondaryContainer.readableOnColor(),
            tertiary = tertiary,
            onTertiary = tertiary.readableOnColor(),
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = tertiaryContainer.readableOnColor(),
            background = background,
            onBackground = background.readableOnColor(),
            surface = surface,
            onSurface = surface.readableOnColor(),
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = surfaceVariant.readableOnColor(),
            outline = secondary.blendWith(surface.readableOnColor(), 0.34f),
            outlineVariant = secondary.blendWith(surface, 0.18f),
            error = fallback.error,
            errorContainer = fallback.errorContainer,
            onErrorContainer = fallback.onErrorContainer,
        )
    }
}

private fun String.toColorOr(fallback: Color): Color {
    val clean = trim().removePrefix("#")
    if (!Regex("""[0-9A-Fa-f]{6}""").matches(clean)) return fallback
    return Color(0xFF000000 or clean.toLong(16))
}

private fun Color.readableOnColor(): Color {
    val argb = toArgb()
    val red = (argb shr 16 and 0xFF) / 255.0
    val green = (argb shr 8 and 0xFF) / 255.0
    val blue = (argb and 0xFF) / 255.0
    val luminance = 0.2126 * red + 0.7152 * green + 0.0722 * blue
    return if (luminance > 0.58) Color(0xFF111315) else Color.White
}

private fun Color.blendWith(other: Color, amount: Float): Color {
    val start = toArgb()
    val end = other.toArgb()
    val ratio = amount.coerceIn(0f, 1f)
    val inverse = 1f - ratio
    val red = ((start shr 16 and 0xFF) * inverse + (end shr 16 and 0xFF) * ratio).toInt()
    val green = ((start shr 8 and 0xFF) * inverse + (end shr 8 and 0xFF) * ratio).toInt()
    val blue = ((start and 0xFF) * inverse + (end and 0xFF) * ratio).toInt()
    return Color(0xFF000000 or (red.toLong() shl 16) or (green.toLong() shl 8) or blue.toLong())
}

private val PuxivShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(8.dp),
    extraLarge = RoundedCornerShape(8.dp),
)

private val PuxivTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 17.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 18.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
    ),
)

@Composable
fun PuxivTheme(
    themeMode: PuxivThemeMode = PuxivThemeMode.System,
    useMaterialYou: Boolean = false,
    palette: PuxivThemePalette = PuxivThemePalette.Puxiv,
    customPalette: PuxivCustomPalette = PuxivCustomPalette(),
    content: @Composable () -> Unit,
) {
    val systemDarkTheme = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        PuxivThemeMode.System -> systemDarkTheme
        PuxivThemeMode.Light -> false
        PuxivThemeMode.Dark -> true
    }
    val context = LocalContext.current
    val colorScheme = if (useMaterialYou && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        palette.puxivColorScheme(darkTheme, customPalette)
    }
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PuxivTypography,
        shapes = PuxivShapes,
        content = content,
    )
}
