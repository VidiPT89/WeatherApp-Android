package dev.ividi.weatherapp.data.model

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherInsightsParsingTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `decodes marine response with tide events for a coastal city`() {
        val raw = """
            {
              "city": "Cascais",
              "country": "Portugal",
              "units": "metric",
              "provider": "open-meteo",
              "fromCache": false,
              "waterTemperature": 20.2,
              "waveHeightMeters": 0.86,
              "waveDirectionDegrees": 306.0,
              "wavePeriodSeconds": 5.55,
              "tideEvents": [
                {"type": "low", "time": "2026-07-24T05:00"},
                {"type": "high", "time": "2026-07-24T11:00"}
              ]
            }
        """.trimIndent()

        val marine = json.decodeFromString(MarineResponse.serializer(), raw)

        assertTrue(marine.hasData)
        assertEquals(2, marine.tideEvents.size)
        assertFalse(marine.tideEvents[0].isHigh)
        assertTrue(marine.tideEvents[1].isHigh)
    }

    @Test
    fun `decodes marine response with empty tide events for an inland city`() {
        val raw = """
            {
              "city": "Madrid",
              "country": "Spain",
              "units": "metric",
              "provider": "open-meteo",
              "fromCache": false,
              "tideEvents": []
            }
        """.trimIndent()

        val marine = json.decodeFromString(MarineResponse.serializer(), raw)

        assertFalse(marine.hasData)
        assertTrue(marine.tideEvents.isEmpty())
    }

    @Test
    fun `decodes weather insights response with a fishing condition`() {
        val raw = """
            {
              "city": "Cascais",
              "country": "Portugal",
              "moonPhase": {"phase": "Waxing Gibbous", "illuminationPercent": 76},
              "uvRiskLabel": "High",
              "outdoorActivityScore": 88,
              "outdoorActivityLabel": "Great",
              "fishingConditionLabel": "Fair"
            }
        """.trimIndent()

        val insights = json.decodeFromString(WeatherInsightsResponse.serializer(), raw)

        assertEquals("Waxing Gibbous", insights.moonPhase.phase)
        assertEquals(76, insights.moonPhase.illuminationPercent)
        assertEquals(88, insights.outdoorActivityScore)
        assertEquals("Fair", insights.fishingConditionLabel)
    }

    @Test
    fun `decodes weather insights response with null fishing condition for an inland city`() {
        val raw = """
            {
              "city": "Madrid",
              "country": "Spain",
              "moonPhase": {"phase": "Waxing Gibbous", "illuminationPercent": 76},
              "uvRiskLabel": "Very High",
              "outdoorActivityScore": 73,
              "outdoorActivityLabel": "Good"
            }
        """.trimIndent()

        val insights = json.decodeFromString(WeatherInsightsResponse.serializer(), raw)

        assertNull(insights.fishingConditionLabel)
    }
}
