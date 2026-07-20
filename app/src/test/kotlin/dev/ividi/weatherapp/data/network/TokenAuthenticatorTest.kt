package dev.ividi.weatherapp.data.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dev.ividi.weatherapp.data.auth.TokenStore
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit

/** In-memory [TokenStore] fake -- the real [dev.ividi.weatherapp.data.auth.TokenStorage] needs a
 * live Android Keystore and can't be constructed in a plain JVM test. */
private class FakeTokenStore(
    private var token: String?,
    private var refreshToken: String?,
) : TokenStore {
    var savedTokens: Pair<String, String>? = null
        private set
    var cleared = false
        private set

    override fun getToken(): String? = token
    override fun getRefreshToken(): String? = refreshToken
    override fun saveTokens(token: String, refreshToken: String) {
        this.token = token
        this.refreshToken = refreshToken
        savedTokens = token to refreshToken
    }
    override fun clearTokens() {
        token = null
        refreshToken = null
        cleared = true
    }
}

/**
 * Verifies [TokenAuthenticator]'s refresh-and-retry behavior against a real (mock) HTTP server
 * for the `/auth/refresh` call, with a [FakeTokenStore] standing in for the Keystore-backed one.
 */
class TokenAuthenticatorTest {

    private lateinit var server: MockWebServer
    private lateinit var refreshApiService: WeatherApiService

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val json = Json { ignoreUnknownKeys = true }
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        refreshApiService = retrofit.create(WeatherApiService::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun fakeFailedResponse(authHeader: String?, priorResponse: Response? = null): Response {
        val requestBuilder = Request.Builder().url(server.url("/api/v1/weather"))
        if (authHeader != null) requestBuilder.header("Authorization", authHeader)
        val builder = Response.Builder()
            .request(requestBuilder.build())
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
        if (priorResponse != null) builder.priorResponse(priorResponse)
        return builder.build()
    }

    @Test
    fun `refreshes and retries with the new token when the refresh call succeeds`() {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"token":"new-access-token","tokenType":"Bearer","expiresInSeconds":3600,"refreshToken":"new-refresh-token"}"""
            )
        )
        val tokenStore = FakeTokenStore(token = "expired-token", refreshToken = "valid-refresh-token")
        val authenticator = TokenAuthenticator(tokenStore, refreshApiService)

        val retriedRequest = authenticator.authenticate(null, fakeFailedResponse("Bearer expired-token"))

        assertEquals("Bearer new-access-token", retriedRequest?.header("Authorization"))
        assertEquals("new-access-token" to "new-refresh-token", tokenStore.savedTokens)
    }

    @Test
    fun `clears tokens and gives up when the refresh token itself is rejected`() {
        server.enqueue(
            MockResponse().setResponseCode(401).setBody(
                """{"timestamp":"2024-01-01T00:00:00Z","status":401,"error":"Unauthorized","message":"Invalid refresh token","path":"/api/v1/auth/refresh","errorCode":"INVALID_REFRESH_TOKEN"}"""
            )
        )
        val tokenStore = FakeTokenStore(token = "expired-token", refreshToken = "revoked-refresh-token")
        val authenticator = TokenAuthenticator(tokenStore, refreshApiService)

        val retriedRequest = authenticator.authenticate(null, fakeFailedResponse("Bearer expired-token"))

        assertNull(retriedRequest)
        assertTrue(tokenStore.cleared)
    }

    @Test
    fun `gives up without calling refresh again when the retried request also failed`() {
        val tokenStore = FakeTokenStore(token = "expired-token", refreshToken = "valid-refresh-token")
        val authenticator = TokenAuthenticator(tokenStore, refreshApiService)
        val firstFailure = fakeFailedResponse("Bearer expired-token")
        val secondFailure = fakeFailedResponse("Bearer new-access-token", priorResponse = firstFailure)

        val retriedRequest = authenticator.authenticate(null, secondFailure)

        assertNull(retriedRequest)
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `reuses an already-refreshed token instead of refreshing again`() {
        val tokenStore = FakeTokenStore(token = "already-refreshed-token", refreshToken = "valid-refresh-token")
        val authenticator = TokenAuthenticator(tokenStore, refreshApiService)

        val retriedRequest = authenticator.authenticate(
            null,
            fakeFailedResponse("Bearer stale-token-from-a-sibling-request"),
        )

        assertEquals("Bearer already-refreshed-token", retriedRequest?.header("Authorization"))
        assertEquals(0, server.requestCount)
    }
}
