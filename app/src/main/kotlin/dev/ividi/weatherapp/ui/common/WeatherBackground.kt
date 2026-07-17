package dev.ividi.weatherapp.ui.common

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Maps a free-text weather `description` (as returned by the backend/provider) to a card
 * background gradient. Case-insensitive substring match on keyword, falling back to a neutral
 * blue gradient for anything unrecognized -- this is deliberately simple keyword matching, not
 * an exhaustive condition taxonomy, per the project's KISS/YAGNI stance.
 */
fun weatherConditionGradient(description: String): Brush {
    val normalized = description.lowercase()

    val colors = when {
        "thunderstorm" in normalized -> listOf(Color(0xFF1A1330), Color(0xFF3B2360))
        "snow" in normalized -> listOf(Color(0xFFDCEEFB), Color(0xFFF3F9FF))
        "drizzle" in normalized || "rain" in normalized -> listOf(Color(0xFF0F2A4A), Color(0xFF1E4C7A))
        "fog" in normalized || "mist" in normalized || "haze" in normalized ->
            listOf(Color(0xFF7C8791), Color(0xFFAAB4BD))
        "overcast" in normalized || "cloud" in normalized -> listOf(Color(0xFF6E7B87), Color(0xFF9AA7B2))
        "clear" in normalized || "sun" in normalized -> listOf(Color(0xFF2196F3), Color(0xFF6EC6FF))
        else -> listOf(Color(0xFF1976D2), Color(0xFF64B5F6))
    }

    return Brush.linearGradient(colors)
}

/** Whether a [weatherConditionGradient] for this description reads dark enough to need light text. */
fun weatherConditionNeedsLightText(description: String): Boolean {
    val normalized = description.lowercase()
    return "thunderstorm" in normalized ||
        "drizzle" in normalized ||
        "rain" in normalized ||
        "overcast" in normalized ||
        "cloud" in normalized ||
        "clear" in normalized ||
        "sun" in normalized
}
