package dev.ividi.weatherapp.data.network

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.Retrofit
import retrofit2.http.GET

@Serializable
private data class Ping(val message: String)

private interface PingService {
    @GET("/ping")
    suspend fun ping(): Ping
}

/**
 * Verifies [safeApiCall]'s error-parsing path against a real (mock) HTTP server: a non-2xx
 * response with the backend's shared error envelope must surface as an [ApiException.HttpError]
 * carrying its `message` field.
 */
class SafeApiCallTest {

    private lateinit var server: MockWebServer
    private lateinit var service: PingService
    private val json = Json { ignoreUnknownKeys = true }

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        service = retrofit.create(PingService::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `non-2xx response with error envelope throws HttpError carrying the backend message`() = runBlocking {
        val errorBody = """
            {
              "timestamp": "2024-06-01T12:00:00Z",
              "status": 404,
              "error": "Not Found",
              "message": "City not found",
              "path": "/api/v1/weather"
            }
        """.trimIndent()
        server.enqueue(MockResponse().setResponseCode(404).setBody(errorBody))

        try {
            safeApiCall(json) { service.ping() }
            fail("Expected an ApiException.HttpError to be thrown")
        } catch (error: ApiException.HttpError) {
            assertEquals(404, error.statusCode)
            assertEquals("City not found", error.backendMessage)
            assertEquals("/api/v1/weather", error.path)
        }
    }

    @Test
    fun `successful response decodes normally without throwing`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"message":"pong"}"""))

        val result = safeApiCall(json) { service.ping() }

        assertEquals("pong", result.message)
    }

    @Test
    fun `non-2xx response with an unparsable body falls back to a generic message`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(500).setBody("not json"))

        try {
            safeApiCall(json) { service.ping() }
            fail("Expected an ApiException.HttpError to be thrown")
        } catch (error: ApiException.HttpError) {
            assertEquals(500, error.statusCode)
            assertTrue(error.backendMessage.isNotBlank())
        }
    }
}
