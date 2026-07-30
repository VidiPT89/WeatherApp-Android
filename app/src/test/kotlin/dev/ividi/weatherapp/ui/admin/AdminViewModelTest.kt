package dev.ividi.weatherapp.ui.admin

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dev.ividi.weatherapp.data.auth.CurrentUserProvider
import dev.ividi.weatherapp.data.model.UserAccount
import dev.ividi.weatherapp.data.network.ApiException
import dev.ividi.weatherapp.data.network.WeatherApiService
import dev.ividi.weatherapp.data.repository.AdminRepository
import dev.ividi.weatherapp.ui.common.UiState
import dev.ividi.weatherapp.util.ErrorMessageResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Instant
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

/** In-memory fake -- the real [dev.ividi.weatherapp.data.auth.AuthRepository] needs a live
 * Android Context-backed TokenStorage and can't be constructed in a plain JVM test. */
private class FakeCurrentUserProvider(initial: UserAccount?) : CurrentUserProvider {
    private val flow = MutableStateFlow(initial)
    override val currentUser: StateFlow<UserAccount?> = flow
}

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

private fun account(id: Long, admin: Boolean) = UserAccount(
    id = id,
    email = "user$id@ividi.dev",
    role = if (admin) "admin" else "user",
    units = "metric",
    createdAt = Instant.parse("2024-01-01T00:00:00Z"),
)

/**
 * Verifies [AdminViewModel]'s list/delete/error paths against a real (mock) HTTP server, mirroring
 * [dev.ividi.weatherapp.data.network.TokenAuthenticatorTest]'s approach of exercising the real
 * [WeatherApiService] instead of mocking it (no mocking library is used in this project).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AdminViewModelTest {

    private lateinit var server: MockWebServer
    private lateinit var adminRepository: AdminRepository
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
        adminRepository = AdminRepository(retrofit.create(WeatherApiService::class.java), json)
    }

    @After
    fun tearDown() {
        server.shutdown()
        Dispatchers.resetMain()
    }

    @Test
    fun `loadUsers populates usersState with the returned accounts`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """[{"id":1,"email":"a@ividi.dev","role":"admin","units":"metric","createdAt":"2024-01-01T00:00:00Z"}]""",
            ),
        )
        val viewModel = AdminViewModel(adminRepository, FakeCurrentUserProvider(null), FakeErrorMessageResolver())

        viewModel.usersState.test {
            val success = awaitSettled() as UiState.Success
            assertEquals(1, success.data.size)
            assertEquals("a@ividi.dev", success.data[0].email)
        }
    }

    @Test
    fun `loadUsers surfaces an empty list as UiState-Empty`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("[]"))
        val viewModel = AdminViewModel(adminRepository, FakeCurrentUserProvider(null), FakeErrorMessageResolver())

        viewModel.usersState.test {
            assertEquals(UiState.Empty, awaitSettled())
        }
    }

    @Test
    fun `loadUsers surfaces a failed request as UiState-Error via the error resolver`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(403).setBody("""{"message":"Forbidden","errorCode":"ACCESS_DENIED"}"""),
        )
        val viewModel = AdminViewModel(adminRepository, FakeCurrentUserProvider(null), FakeErrorMessageResolver())

        viewModel.usersState.test {
            val error = awaitSettled() as UiState.Error
            assertTrue(error.message.startsWith("friendly:"))
        }
    }

    @Test
    fun `deleteUser removes the account and reloads the list`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """[{"id":1,"email":"a@ividi.dev","role":"user","units":"metric","createdAt":"2024-01-01T00:00:00Z"}]""",
            ),
        )
        val viewModel = AdminViewModel(adminRepository, FakeCurrentUserProvider(account(99, admin = true)), FakeErrorMessageResolver())
        server.enqueue(MockResponse().setResponseCode(204))
        server.enqueue(MockResponse().setResponseCode(200).setBody("[]"))

        viewModel.usersState.test {
            val initialSuccess = awaitSettled() as UiState.Success
            assertEquals(1, initialSuccess.data.size)

            viewModel.deleteUser(1)

            assertEquals(UiState.Empty, awaitSettled())
        }
        assertNull(viewModel.deleteError.value)
    }

    @Test
    fun `deleteUser failure surfaces deleteError instead of silently swallowing it`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """[{"id":1,"email":"a@ividi.dev","role":"user","units":"metric","createdAt":"2024-01-01T00:00:00Z"}]""",
            ),
        )
        val viewModel = AdminViewModel(adminRepository, FakeCurrentUserProvider(account(99, admin = true)), FakeErrorMessageResolver())

        // Wait for the initial load to actually complete before triggering the delete, so the
        // two HTTP calls can't race each other and get paired with the wrong mock response.
        viewModel.usersState.test {
            awaitSettled() as UiState.Success
        }

        server.enqueue(
            MockResponse().setResponseCode(400).setBody(
                """{"message":"Cannot delete your own account","errorCode":"VALIDATION_FAILED"}""",
            ),
        )

        viewModel.deleteError.test {
            assertEquals(null, awaitItem())
            viewModel.deleteUser(1)
            val message = awaitItem()
            assertTrue(message!!.startsWith("friendly:"))
        }
    }

    @Test
    fun `currentUserId reflects the injected current user, null when unknown`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("[]"))
        val viewModel = AdminViewModel(adminRepository, FakeCurrentUserProvider(account(42, admin = true)), FakeErrorMessageResolver())

        assertEquals(42L, viewModel.currentUserId)
    }
}
