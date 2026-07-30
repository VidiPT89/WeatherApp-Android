package dev.ividi.weatherapp.data.auth

import dev.ividi.weatherapp.data.model.LoginRequest
import dev.ividi.weatherapp.data.model.RefreshRequest
import dev.ividi.weatherapp.data.model.RegisterRequest
import dev.ividi.weatherapp.data.model.UserAccount
import dev.ividi.weatherapp.data.network.WeatherApiService
import dev.ividi.weatherapp.data.network.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json

/** Owns the login/register calls and the resulting session (JWT) state. */
@Singleton
class AuthRepository @Inject constructor(
    private val apiService: WeatherApiService,
    private val tokenStorage: TokenStorage,
    private val json: Json,
) : CurrentUserProvider {

    /** Emits the current JWT, or null when logged out. Drives which nav graph is shown. */
    val tokenFlow: StateFlow<String?> = tokenStorage.tokenFlow

    val isLoggedInNow: Boolean
        get() = tokenStorage.getToken() != null

    /**
     * The caller's own account (including [UserAccount.isAdmin]), refreshed once after
     * login/register and once on session restore. Null until the first successful fetch, or
     * if that fetch failed -- callers gating admin UI on this must treat null as "not admin".
     */
    private val _currentUser = MutableStateFlow<UserAccount?>(null)
    override val currentUser: StateFlow<UserAccount?> = _currentUser.asStateFlow()

    suspend fun register(email: String, password: String) {
        val response = safeApiCall(json) {
            apiService.register(RegisterRequest(email = email, password = password))
        }
        tokenStorage.saveTokens(response.token, response.refreshToken)
        refreshCurrentUser()
    }

    suspend fun login(email: String, password: String) {
        val response = safeApiCall(json) {
            apiService.login(LoginRequest(email = email, password = password))
        }
        tokenStorage.saveTokens(response.token, response.refreshToken)
        refreshCurrentUser()
    }

    /** Best-effort server-side revocation -- local state is cleared regardless of the result. */
    suspend fun logout() {
        tokenStorage.getRefreshToken()?.let { refreshToken ->
            runCatching { apiService.logout(RefreshRequest(refreshToken)) }
        }
        tokenStorage.clearTokens()
        _currentUser.value = null
    }

    /**
     * Fetches `/api/v1/user/me` and stores the result, so the rest of the app can read
     * [currentUser] (e.g. to decide whether to show the admin entry point) without every
     * screen making its own call. Best-effort: a failure just leaves [currentUser] as it was.
     */
    suspend fun refreshCurrentUser() {
        val account = runCatching { safeApiCall(json) { apiService.getCurrentUser() } }.getOrNull()
        if (account != null) _currentUser.value = account
    }
}
