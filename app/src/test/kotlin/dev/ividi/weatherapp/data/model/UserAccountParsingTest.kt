package dev.ividi.weatherapp.data.model

import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pins [UserAccount]'s shape against the backend's `UserResponse` contract (`GET /api/v1/user/me`
 * and each entry of `GET /api/v1/admin/users`): `id`, `email`, `role`, `units`, `createdAt`.
 */
class UserAccountParsingTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `decodes an admin account and isAdmin is true`() {
        val raw = """
            {
              "id": 1,
              "email": "admin@ividi.dev",
              "role": "admin",
              "units": "metric",
              "createdAt": "2024-01-01T00:00:00Z"
            }
        """.trimIndent()

        val account = json.decodeFromString(UserAccount.serializer(), raw)

        assertEquals(1L, account.id)
        assertEquals("admin@ividi.dev", account.email)
        assertEquals(Instant.parse("2024-01-01T00:00:00Z"), account.createdAt)
        assertTrue(account.isAdmin)
    }

    @Test
    fun `decodes a regular account and isAdmin is false`() {
        val raw = """
            {
              "id": 2,
              "email": "user@ividi.dev",
              "role": "user",
              "units": "imperial",
              "createdAt": "2024-06-15T09:30:00Z"
            }
        """.trimIndent()

        val account = json.decodeFromString(UserAccount.serializer(), raw)

        assertEquals("user", account.role)
        assertFalse(account.isAdmin)
    }

    @Test
    fun `ignores unknown fields the backend might add later`() {
        val raw = """
            {
              "id": 3,
              "email": "extra@ividi.dev",
              "role": "user",
              "units": "metric",
              "createdAt": "2024-06-15T09:30:00Z",
              "someFutureField": "ignored"
            }
        """.trimIndent()

        val account = json.decodeFromString(UserAccount.serializer(), raw)

        assertEquals(3L, account.id)
    }

    @Test
    fun `decodes a list of accounts as returned by GET admin users`() {
        val raw = """
            [
              {"id": 1, "email": "a@ividi.dev", "role": "admin", "units": "metric", "createdAt": "2024-01-01T00:00:00Z"},
              {"id": 2, "email": "b@ividi.dev", "role": "user", "units": "metric", "createdAt": "2024-02-01T00:00:00Z"}
            ]
        """.trimIndent()

        val accounts = json.decodeFromString(kotlinx.serialization.builtins.ListSerializer(UserAccount.serializer()), raw)

        assertEquals(2, accounts.size)
        assertTrue(accounts[0].isAdmin)
        assertFalse(accounts[1].isAdmin)
    }
}
