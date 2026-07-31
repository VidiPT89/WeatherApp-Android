package dev.ividi.weatherapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// internal (not private): also used by WeatherGlanceWidget to theme the home-screen widget with
// the same static (non-dynamic) palette as the in-app fallback, so the widget doesn't look like a
// visually unrelated surface when dynamic color isn't available/applicable to Glance.
internal val LightColors = lightColorScheme(
    primary = WeatherBlue40,
    secondary = SkyAccent40,
    tertiary = WeatherBlueGrey40,
)

internal val DarkColors = darkColorScheme(
    primary = WeatherBlue80,
    secondary = SkyAccent80,
    tertiary = WeatherBlueGrey80,
)

@Composable
fun WeatherAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content,
    )
}
