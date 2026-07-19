package dev.ividi.weatherapp.data.model

import kotlinx.serialization.Serializable

/** Mirrors the shared error envelope returned by every backend error response. */
@Serializable
data class ApiErrorBody(
    val timestamp: String? = null,
    val status: Int? = null,
    val error: String? = null,
    val message: String? = null,
    val path: String? = null,
    val errorCode: String? = null,
)
