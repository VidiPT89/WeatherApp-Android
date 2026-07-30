package dev.ividi.weatherapp.data.repository

import dev.ividi.weatherapp.data.model.UserAccount
import dev.ividi.weatherapp.data.network.WeatherApiService
import dev.ividi.weatherapp.data.network.safeApiCall
import dev.ividi.weatherapp.data.network.throwIfUnsuccessful
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json

/** Admin-only user management (`/api/v1/admin/users`) -- the backend enforces `ROLE_ADMIN`
 * server-side; the caller is still responsible for hiding this UI from non-admins. */
@Singleton
class AdminRepository @Inject constructor(
    private val apiService: WeatherApiService,
    private val json: Json,
) {

    suspend fun listUsers(): List<UserAccount> = safeApiCall(json) {
        apiService.listUsers()
    }

    suspend fun deleteUser(id: Long) = safeApiCall(json) {
        apiService.deleteUser(id).throwIfUnsuccessful()
    }
}
