package dev.ividi.weatherapp.data.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class HistoryEntry(
    val city: String,
    val units: Units,
    val searchedAt: Instant,
)
