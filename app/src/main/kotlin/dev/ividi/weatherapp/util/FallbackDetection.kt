package dev.ividi.weatherapp.util

/** The backend's primary weather provider. Any other value means the secondary was used. */
const val PRIMARY_PROVIDER = "open-meteo"

/**
 * True when the backend served this response from its secondary/fallback provider
 * rather than the primary ([PRIMARY_PROVIDER]) -- the event the fallback banner must surface.
 */
fun isFallbackProvider(provider: String): Boolean = provider != PRIMARY_PROVIDER
