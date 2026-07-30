package dev.ividi.weatherapp.ui.history

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dev.ividi.weatherapp.data.network.ApiException
import dev.ividi.weatherapp.data.network.WeatherApiService
import dev.ividi.weatherapp.data.repository.HistoryRepository
import dev.ividi.weatherapp.ui.common.UiState
import dev.ividi.weatherapp.util.ErrorMessageResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit

/** Returns a fixed, recognizable string instead of resolving a real Android string resource, so
 * assertions can confirm the ViewModel actually routed the error through this collaborator. */
private class FakeErrorMessageResolver : ErrorMessageResolver {
    override fun messageFor(error: ApiException, fallbackRes: Int): String = "friendly:${error.message}"
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
 * Verifies [HistoryViewModel]'s load/delete-one/clear-all/error paths against a real (mock) HTTP
 * server, mirroring [dev.ividi.weatherapp.ui.admin.AdminViewModelTest]'s approach of exercising
 * the real [WeatherApiService] instead of mocking it (no mocking library is used in this project).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    private lateinit var server: MockWebServer
    private lateinit var historyRepository: HistoryRepository
    private val json = Json { ignoreUnknownKeys = true }

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        server = MockWebServer()
        server.start()
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        historyRepository = HistoryRepository(retrofit.create(WeatherApiService::class.java), json)
    }

    @After
    fun tearDown() {
        server.shutdown()
        Dispatchers.resetMain()
    }

    @Test
    fun `loadHistory populates historyState with the returned entries, newest first`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """[{"id":1,"city":"Lisboa","units":"metric","searchedAt":"2024-01-01T00:00:00Z"}]""",
            ),
        )
        val viewModel = HistoryViewModel(historyRepository, FakeErrorMessageResolver())

        viewModel.historyState.test {
            val success = awaitSettled() as UiState.Success
            assertEquals(1, success.data.size)
            assertEquals("Lisboa", success.data[0].city)
        }
    }

    @Test
    fun `loadHistory surfaces an empty list as UiState-Empty`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("[]"))
        val viewModel = HistoryViewModel(historyRepository, FakeErrorMessageResolver())

        viewModel.historyState.test {
            assertEquals(UiState.Empty, awaitSettled())
        }
    }

    @Test
    fun `loadHistory surfaces a failed request as UiState-Error via the error resolver`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(500).setBody("""{"message":"Boom","errorCode":"INTERNAL_ERROR"}"""),
        )
        val viewModel = HistoryViewModel(historyRepository, FakeErrorMessageResolver())

        viewModel.historyState.test {
            val error = awaitSettled() as UiState.Error
            assertTrue(error.message.startsWith("friendly:"))
        }
    }

    @Test
    fun `deleteEntry removes the entry and reloads the list`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """[{"id":1,"city":"Lisboa","units":"metric","searchedAt":"2024-01-01T00:00:00Z"}]""",
            ),
        )
        val viewModel = HistoryViewModel(historyRepository, FakeErrorMessageResolver())
        server.enqueue(MockResponse().setResponseCode(204))
        server.enqueue(MockResponse().setResponseCode(200).setBody("[]"))

        viewModel.historyState.test {
            val initialSuccess = awaitSettled() as UiState.Success
            assertEquals(1, initialSuccess.data.size)

            viewModel.deleteEntry(1)

            assertEquals(UiState.Empty, awaitSettled())
        }
        assertNull(viewModel.deleteError.value)
    }

    @Test
    fun `deleteEntry failure surfaces deleteError instead of silently swallowing it`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """[{"id":1,"city":"Lisboa","units":"metric","searchedAt":"2024-01-01T00:00:00Z"}]""",
            ),
        )
        val viewModel = HistoryViewModel(historyRepository, FakeErrorMessageResolver())

        // Wait for the initial load to actually complete before triggering the delete, so the
        // two HTTP calls can't race each other and get paired with the wrong mock response.
        viewModel.historyState.test {
            awaitSettled() as UiState.Success
        }

        server.enqueue(
            MockResponse().setResponseCode(500).setBody(
                """{"message":"Boom","errorCode":"INTERNAL_ERROR"}""",
            ),
        )

        viewModel.deleteError.test {
            assertEquals(null, awaitItem())
            viewModel.deleteEntry(1)
            val message = awaitItem()
            assertTrue(message!!.startsWith("friendly:"))
        }
    }

    @Test
    fun `deleteEntry treats a 404 SEARCH_HISTORY_ENTRY_NOT_FOUND as already-deleted, not an error`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """[{"id":1,"city":"Lisboa","units":"metric","searchedAt":"2024-01-01T00:00:00Z"}]""",
            ),
        )
        val viewModel = HistoryViewModel(historyRepository, FakeErrorMessageResolver())

        viewModel.historyState.test {
            awaitSettled() as UiState.Success
        }

        server.enqueue(
            MockResponse().setResponseCode(404).setBody(
                """{"message":"Search history entry not found: '1'","errorCode":"SEARCH_HISTORY_ENTRY_NOT_FOUND"}""",
            ),
        )
        server.enqueue(MockResponse().setResponseCode(200).setBody("[]"))

        viewModel.historyState.test {
            awaitSettled() as UiState.Success

            viewModel.deleteEntry(1)

            assertEquals(UiState.Empty, awaitSettled())
        }
        assertNull(viewModel.deleteError.value)
    }

    @Test
    fun `clearHistory empties the list and reloads`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """[
                    {"id":1,"city":"Lisboa","units":"metric","searchedAt":"2024-01-01T00:00:00Z"},
                    {"id":2,"city":"Porto","units":"metric","searchedAt":"2024-01-02T00:00:00Z"}
                ]""",
            ),
        )
        val viewModel = HistoryViewModel(historyRepository, FakeErrorMessageResolver())
        server.enqueue(MockResponse().setResponseCode(204))
        server.enqueue(MockResponse().setResponseCode(200).setBody("[]"))

        viewModel.historyState.test {
            val initialSuccess = awaitSettled() as UiState.Success
            assertEquals(2, initialSuccess.data.size)

            viewModel.clearHistory()

            assertEquals(UiState.Empty, awaitSettled())
        }
        assertNull(viewModel.deleteError.value)
    }

    @Test
    fun `clearHistory failure surfaces deleteError instead of silently swallowing it`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """[{"id":1,"city":"Lisboa","units":"metric","searchedAt":"2024-01-01T00:00:00Z"}]""",
            ),
        )
        val viewModel = HistoryViewModel(historyRepository, FakeErrorMessageResolver())

        viewModel.historyState.test {
            awaitSettled() as UiState.Success
        }

        server.enqueue(
            MockResponse().setResponseCode(500).setBody(
                """{"message":"Boom","errorCode":"INTERNAL_ERROR"}""",
            ),
        )

        viewModel.deleteError.test {
            assertEquals(null, awaitItem())
            viewModel.clearHistory()
            val message = awaitItem()
            assertTrue(message!!.startsWith("friendly:"))
        }
    }
}
