package dev.ividi.weatherapp.data.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class FavoriteEntry(
    val city: String,
    val createdAt: Instant,
)

@Serializable
data class FavoriteRequest(
    val city: String,
)
