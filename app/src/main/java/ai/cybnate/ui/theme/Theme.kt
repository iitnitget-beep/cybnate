package ai.cybnate.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CybNateColorScheme = darkColorScheme(
    primary = CybNateCyan,
    onPrimary = Color.Black,
    secondary = CybNateNeonGreen,
    onSecondary = Color.Black,
    tertiary = CybNatePurple,
    background = CybNateVoid,
    onBackground = Color.White,
    surface = CybNateSurface,
    onSurface = Color.White,
    surfaceVariant = CybNateSurfaceVariant,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = CybNateBorder,
    error = CybNateError
)

@Composable
fun CybNateTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CybNateColorScheme,
        typography = Typography,
        content = content
    )
}
