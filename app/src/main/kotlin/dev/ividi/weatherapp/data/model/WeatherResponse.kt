package dev.ividi.weatherapp.data.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/** Mirrors the backend's `WeatherResponse` JSON shape returned by `/api/v1/weather`. */
@Serializable
data class WeatherResponse(
    val city: String,
    val country: String,
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val windSpeed: Double,
    val description: String,
    val units: Units,
    val provider: String,
    val observedAt: Instant,
    val fromCache: Boolean,
)
