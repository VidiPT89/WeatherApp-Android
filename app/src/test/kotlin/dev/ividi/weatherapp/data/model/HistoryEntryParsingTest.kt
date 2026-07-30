package dev.ividi.weatherapp.data.model

import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pins [HistoryEntry]'s shape against the backend's `SearchHistoryResponse` contract
 * (`GET /api/v1/weather/history`): `id`, `city`, `units`, `searchedAt`.
 */
class HistoryEntryParsingTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `decodes a history entry including its id`() {
        val raw = """
            {
              "id": 7,
              "city": "Lisboa",
              "units": "metric",
              "searchedAt": "2024-06-15T09:30:00Z"
            }
        """.trimIndent()

        val entry = json.decodeFromString(HistoryEntry.serializer(), raw)

        assertEquals(7L, entry.id)
        assertEquals("Lisboa", entry.city)
        assertEquals(Units.METRIC, entry.units)
        assertEquals(Instant.parse("2024-06-15T09:30:00Z"), entry.searchedAt)
    }

    @Test
    fun `ignores unknown fields the backend might add later`() {
        val raw = """
            {
              "id": 8,
              "city": "Porto",
              "units": "imperial",
              "searchedAt": "2024-06-15T09:30:00Z",
              "someFutureField": "ignored"
            }
        """.trimIndent()

        val entry = json.decodeFromString(HistoryEntry.serializer(), raw)

        assertEquals(8L, entry.id)
        assertEquals(Units.IMPERIAL, entry.units)
    }

    @Test
    fun `decodes a list of entries as returned by GET weather history`() {
        val raw = """
            [
              {"id": 1, "city": "Lisboa", "units": "metric", "searchedAt": "2024-02-01T00:00:00Z"},
              {"id": 2, "city": "Porto", "units": "metric", "searchedAt": "2024-01-01T00:00:00Z"}
            ]
        """.trimIndent()

        val entries = json.decodeFromString(kotlinx.serialization.builtins.ListSerializer(HistoryEntry.serializer()), raw)

        assertEquals(2, entries.size)
        assertEquals(1L, entries[0].id)
        assertEquals(2L, entries[1].id)
        assertTrue(entries.all { it.city.isNotBlank() })
    }
}
