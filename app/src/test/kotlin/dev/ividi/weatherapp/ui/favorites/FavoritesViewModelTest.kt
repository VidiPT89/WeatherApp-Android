package dev.ividi.weatherapp.ui.favorites

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dev.ividi.weatherapp.data.model.GeocodingResult
import dev.ividi.weatherapp.data.network.ApiException
import dev.ividi.weatherapp.data.network.WeatherApiService
import dev.ividi.weatherapp.data.repository.FavoritesRepository
import dev.ividi.weatherapp.data.repository.GeocodingRepository
import dev.ividi.weatherapp.ui.common.UiState
import dev.ividi.weatherapp.util.ErrorMessageResolver
import dev.ividi.weatherapp.util.StringResolver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit

/** Returns a fixed, recognizable string instead of resolving a real Android string resource, so
 * assertions can confirm the ViewModel actually routed the error through this collaborator. */
private class FakeErrorMessageResolver : ErrorMessageResolver {
    override fun messageFor(error: ApiException, fallbackRes: Int): String = "friendly:${error.message}"
}

/** Formats a fixed, recognizable string instead of resolving a real Android string resource. */
private class FakeStringResolver : StringResolver {
    override fun get(resId: Int, vararg formatArgs: Any): String = "str:$resId:${formatArgs.joinToString()}"
}

/**
 * With [UnconfinedTestDispatcher] against a *real* (if local) HTTP round-trip, the request can
 * finish before a collector even subscribes, so the transient [UiState.Loading] emission is not
 * guaranteed to be observed -- skip it if present instead of asserting it's always there.
 */
private suspend fun <T> ReceiveTurbine<UiState<T>>.awaitSettled(): UiState<T> {
    val first = awaitItem()
    return if (first is UiState.Loading) awaitItem() else first
}

/**
 * Verifies [FavoritesViewModel]'s load/add/remove paths against a real (mock) HTTP server,
 * mirroring [dev.ividi.weatherapp.ui.admin.AdminViewModelTest]'s approach of exercising the real
 * [WeatherApiService] instead of mocking it (no mocking library is used in this project).
 *
 * The "add favorite" flow used to accept arbitrary free text; it now only ever calls
 * [FavoritesRepository.addFavorite] with a name that came back from the `/geocoding` endpoint via
 * [GeocodingRepository] -- these tests exercise that suggestion pipeline (debounce, min length,
 * selection) rather than a raw string being submitted directly.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {

    private lateinit var server: MockWebServer
    private lateinit var favoritesRepository: FavoritesRepository
    private lateinit var geocodingRepository: GeocodingRepository
    private lateinit var dispatcher: TestDispatcher
    private val json = Json { ignoreUnknownKeys = true }

    @Before
    fun setUp() {
        dispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(dispatcher)
        server = MockWebServer()
        server.start()
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        val apiService = retrofit.create(WeatherApiService::class.java)
        favoritesRepository = FavoritesRepository(apiService, json)
        geocodingRepository = GeocodingRepository(apiService, json)
    }

    @After
    fun tearDown() {
        server.shutdown()
        Dispatchers.resetMain()
    }

    private fun viewModel() = FavoritesViewModel(
        favoritesRepository,
        geocodingRepository,
        FakeErrorMessageResolver(),
        FakeStringResolver(),
    )

    @Test
    fun `loadFavorites populates favoritesState with the returned entries`() = runTest(dispatcher) {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """[{"city":"Lisboa","createdAt":"2024-01-01T00:00:00Z"}]""",
            ),
        )
        val viewModel = viewModel()

        viewModel.favoritesState.test {
            val success = awaitSettled() as UiState.Success
            assertEquals(1, success.data.size)
            assertEquals("Lisboa", success.data[0].city)
        }
    }

    @Test
    fun `loadFavorites surfaces an empty list as UiState-Empty`() = runTest(dispatcher) {
        server.enqueue(MockResponse().setResponseCode(200).setBody("[]"))
        val viewModel = viewModel()

        viewModel.favoritesState.test {
            assertEquals(UiState.Empty, awaitSettled())
        }
    }

    @Test
    fun `typing a query below the minimum length never surfaces suggestions`() = runTest(dispatcher) {
        server.enqueue(MockResponse().setResponseCode(200).setBody("[]")) // initial loadFavorites
        val viewModel = viewModel()

        viewModel.onNewCityQueryChange("l")
        advanceTimeBy(500)
        runCurrent()

        assertEquals(emptyList<GeocodingResult>(), viewModel.suggestions.value)
    }

    @Test
    fun `typing a query debounces then populates suggestions from the geocoding endpoint`() = runTest(dispatcher) {
        server.enqueue(MockResponse().setResponseCode(200).setBody("[]")) // initial loadFavorites
        val viewModel = viewModel()

        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"query":"lis","results":[{"name":"Lisboa","country":"Portugal","latitude":38.7,"longitude":-9.1}]}""",
            ),
        )

        viewModel.suggestions.test {
            assertEquals(emptyList<GeocodingResult>(), awaitItem())

            viewModel.onNewCityQueryChange("lis")
            advanceTimeBy(500)
            runCurrent()

            val suggestions = awaitItem()
            assertEquals(1, suggestions.size)
            assertEquals("Lisboa", suggestions[0].name)
            assertEquals("Portugal", suggestions[0].country)
        }
    }

    @Test
    fun `selecting a suggestion adds that exact geocoded city, not free text, and clears the field`() = runTest(dispatcher) {
        server.enqueue(MockResponse().setResponseCode(200).setBody("[]")) // initial loadFavorites
        val viewModel = viewModel()

        // Deliberately never calls onNewCityQueryChange here -- onSuggestionSelected must add
        // exactly the suggestion's own name, independent of whatever free text (if any) is still
        // sitting in the query field.
        val suggestion = GeocodingResult(name = "Porto", country = "Portugal", latitude = 41.1, longitude = -8.6)
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"city":"Porto","createdAt":"2024-01-01T00:00:00Z"}"""))
        server.enqueue(MockResponse().setResponseCode(200).setBody("""[{"city":"Porto","createdAt":"2024-01-01T00:00:00Z"}]"""))

        viewModel.favoritesState.test {
            awaitSettled() // initial empty state

            viewModel.onSuggestionSelected(suggestion)

            val success = awaitSettled() as UiState.Success
            assertEquals("Porto", success.data[0].city)
        }
        assertEquals("", viewModel.newCityQuery.value)
        assertTrue(viewModel.suggestions.value.isEmpty())

        val addRequest = server.takeRequest() // loadFavorites (initial)
        assertEquals("GET", addRequest.method)
        val postRequest = server.takeRequest()
        assertEquals("POST", postRequest.method)
        assertTrue(postRequest.body.readUtf8().contains("Porto"))
    }

    @Test
    fun `submitting with no suggestion loaded is a no-op, never adds free text`() = runTest(dispatcher) {
        server.enqueue(MockResponse().setResponseCode(200).setBody("[]")) // initial loadFavorites
        val viewModel = viewModel()
        viewModel.onNewCityQueryChange("some free text city")

        viewModel.onNewCitySubmit()
        runCurrent()

        assertEquals(null, viewModel.addFavoriteMessage.value)
    }

    @Test
    fun `removeFavorite removes the entry and reloads the list`() = runTest(dispatcher) {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """[{"city":"Lisboa","createdAt":"2024-01-01T00:00:00Z"}]""",
            ),
        )
        val viewModel = viewModel()
        server.enqueue(MockResponse().setResponseCode(204))
        server.enqueue(MockResponse().setResponseCode(200).setBody("[]"))

        viewModel.favoritesState.test {
            val initialSuccess = awaitSettled() as UiState.Success
            assertEquals(1, initialSuccess.data.size)

            viewModel.removeFavorite("Lisboa")

            assertEquals(UiState.Empty, awaitSettled())
        }
    }
}
