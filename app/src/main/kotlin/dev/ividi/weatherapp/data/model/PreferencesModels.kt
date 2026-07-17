package dev.ividi.weatherapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UnitsPreference(
    val units: Units,
)
