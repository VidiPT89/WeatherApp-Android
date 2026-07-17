package dev.ividi.weatherapp.data.auth

import dev.ividi.weatherapp.data.model.LoginRequest
import dev.ividi.weatherapp.data.model.RegisterRequest
import dev.ividi.weatherapp.data.network.WeatherApiService
import dev.ividi.weatherapp.data.network.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json

/** Owns the login/register calls and the resulting session (JWT) state. */
@Singleton
class AuthRepository @Inject constructor(
    private val apiService: WeatherApiService,
    private val tokenStorage: TokenStorage,
    private val json: Json,
) {

    /** Emits the current JWT, or null when logged out. Drives which nav graph is shown. */
    val tokenFlow: StateFlow<String?> = tokenStorage.tokenFlow

    val isLoggedInNow: Boolean
        get() = tokenStorage.getToken() != null

    suspend fun register(email: String, password: String) {
        val response = safeApiCall(json) {
            apiService.register(RegisterRequest(email = email, password = password))
        }
        tokenStorage.saveToken(response.token)
    }

    suspend fun login(email: String, password: String) {
        val response = safeApiCall(json) {
            apiService.login(LoginRequest(email = email, password = password))
        }
        tokenStorage.saveToken(response.token)
    }

    fun logout() {
        tokenStorage.clearToken()
    }
}
