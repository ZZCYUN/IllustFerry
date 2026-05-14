package JunZi.Pixiv.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    tertiary = Color(0xFFFFB2C7),
    background = Color(0xFF111315),
    onBackground = Color(0xFFE7EAED),
    surface = Color(0xFF1B1D20),
    onSurface = Color(0xFFE7EAED),
    surfaceVariant = Color(0xFF2D3035),
    onSurfaceVariant = Color(0xFFC4C9CE),
    outline = Color(0xFF858B92),
)

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
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = PuxivTypography,
        shapes = PuxivShapes,
        content = content,
    )
}
