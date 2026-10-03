package dev.ividi.weatherapp.data.network

import dev.ividi.weatherapp.data.auth.TokenStore
import dev.ividi.weatherapp.data.model.RefreshRequest
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.HttpException

private const val AUTHORIZATION_HEADER = "Authorization"
private const val BEARER_PREFIX = "Bearer "
private val DEFINITIVE_REFRESH_FAILURES = setOf(400, 401, 403)

/**
 * OkHttp's idiomatic hook for transparent 401 recovery: invoked automatically before a failed
 * response reaches application code, and can retry the request with new credentials. Uses
 * [refreshApiService] -- a Retrofit instance built with no [AuthInterceptor]/[TokenAuthenticator]
 * of its own (see [NetworkModule]) -- to call `/refresh` without recursing back into this class.
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenStore: TokenStore,
    @RefreshApi private val refreshApiService: WeatherApiService,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // A retried request that fails again means the refreshed token was itself rejected --
        // give up rather than looping (this is the standard OkHttp Authenticator loop guard).
        if (response.priorResponse != null) return null

        val failedToken = response.request.header(AUTHORIZATION_HEADER)?.removePrefix(BEARER_PREFIX)

        synchronized(this) {
            // Another request may already have refreshed while this one waited for the lock.
            // If the stored token has moved on from the one that just failed, retry with it
            // instead of firing a second network refresh.
            val currentToken = tokenStore.getToken()
            if (currentToken != null && currentToken != failedToken) {
                return response.request.newBuilder()
                    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + currentToken)
                    .build()
            }

            val refreshToken = tokenStore.getRefreshToken() ?: return null
            val refreshed = runBlocking {
                runCatching { refreshApiService.refresh(RefreshRequest(refreshToken)) }
            }.getOrElse { error ->
                // Only a refresh token the server actually rejected ends the session. A timeout,
                // a backend still waking up or a 5xx/429 just fails this request; the tokens stay
                // so the next request can refresh again instead of forcing a new login.
                if (error is HttpException && error.code() in DEFINITIVE_REFRESH_FAILURES) {
                    tokenStore.clearTokens()
                }
                return null
            }

            tokenStore.saveTokens(refreshed.token, refreshed.refreshToken)
            return response.request.newBuilder()
                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + refreshed.token)
                .build()
        }
    }
}
