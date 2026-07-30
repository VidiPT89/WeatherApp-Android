package dev.ividi.weatherapp.data.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/** `GET /api/v1/user/me` (the caller's own account) and each entry of `GET /api/v1/admin/users`. */
@Serializable
data class UserAccount(
    val id: Long,
    val email: String,
    val role: String,
    val units: String,
    val createdAt: Instant,
) {
    val isAdmin: Boolean get() = role == "admin"
}
