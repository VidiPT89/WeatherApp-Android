package dev.ividi.weatherapp.data.network

import dev.ividi.weatherapp.data.auth.TokenStorage
import javax.inject.Inject
import okhttp3.Interceptor
import okhttp3.Response

private const val AUTH_PATH_SEGMENT = "auth"
private const val AUTHORIZATION_HEADER = "Authorization"
private const val BEARER_PREFIX = "Bearer "

/** Attaches the stored JWT (if any) to every request except calls to the auth endpoints. */
class AuthInterceptor @Inject constructor(
    private val tokenStorage: TokenStorage,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val isAuthEndpoint = originalRequest.url.pathSegments.contains(AUTH_PATH_SEGMENT)
        val token = tokenStorage.getToken()

        val requestToSend = if (isAuthEndpoint || token.isNullOrBlank()) {
            originalRequest
        } else {
            originalRequest.newBuilder()
                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + token)
                .build()
        }

        return chain.proceed(requestToSend)
    }
}
