package dev.ividi.weatherapp.data.auth

/**
 * Minimal contract [dev.ividi.weatherapp.data.network.TokenAuthenticator] needs -- extracted so
 * its refresh/retry logic can be unit tested against an in-memory fake instead of the real
 * Keystore-backed [TokenStorage], which requires an Android `Context` to construct.
 */
interface TokenStore {
    fun getToken(): String?
    fun getRefreshToken(): String?
    fun saveTokens(token: String, refreshToken: String)
    fun clearTokens()
}
