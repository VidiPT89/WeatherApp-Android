package dev.ividi.weatherapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class AuthResponse(
    val token: String,
    val tokenType: String,
    val expiresInSeconds: Long,
    val refreshToken: String,
)

@Serializable
data class RefreshRequest(
    val refreshToken: String,
)
