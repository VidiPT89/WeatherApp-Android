package dev.ividi.weatherapp.data.model

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CompareResponseParsingTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `decodes a mix of successful and failed provider results`() {
        val raw = """
            {
              "city": "Lisboa",
              "results": [
                {
                  "provider": "open-meteo",
                  "success": true,
                  "weather": {
                    "city": "Lisboa", "country": "Portugal", "temperature": 22.0, "feelsLike": 22.0,
                    "humidity": 50, "windSpeed": 10.0, "description": "clear sky", "units": "metric",
                    "provider": "open-meteo", "observedAt": "2024-06-01T12:00:00Z", "fromCache": false
                  },
                  "errorMessage": null
                },
                {
                  "provider": "open-weather-map",
                  "success": false,
                  "weather": null,
                  "errorMessage": "Provider unavailable"
                }
              ]
            }
        """.trimIndent()

        val compare = json.decodeFromString(CompareResponse.serializer(), raw)

        assertEquals(2, compare.results.size)

        val primary = compare.results[0]
        assertTrue(primary.success)
        assertEquals(22.0, primary.weather?.temperature)
        assertNull(primary.errorMessage)

        val secondary = compare.results[1]
        assertFalse(secondary.success)
        assertNull(secondary.weather)
        assertEquals("Provider unavailable", secondary.errorMessage)
    }
}
