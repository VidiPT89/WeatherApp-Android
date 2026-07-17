package dev.ividi.weatherapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CompareResponse(
    val city: String,
    val results: List<ProviderResult>,
)

@Serializable
data class ProviderResult(
    val provider: String,
    val success: Boolean,
    val weather: WeatherResponse?,
    val errorMessage: String?,
)
