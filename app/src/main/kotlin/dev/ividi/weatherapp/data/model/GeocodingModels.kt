package dev.ividi.weatherapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class GeocodingResponse(
    val query: String,
    val results: List<GeocodingResult>,
)

@Serializable
data class GeocodingResult(
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
)
