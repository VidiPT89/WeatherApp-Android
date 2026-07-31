package dev.ividi.weatherapp.util

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherDescriptionLocalizerTest {

    private val portuguese = Locale.forLanguageTag("pt")
    private val english = Locale.forLanguageTag("en")

    @Test
    fun `translates a known Open-Meteo phrase in Portuguese`() {
        assertEquals("Praticamente limpo", localizedWeatherDescription("mainly clear", portuguese))
    }

    @Test
    fun `lookup is case-insensitive`() {
        assertEquals("Praticamente limpo", localizedWeatherDescription("Mainly Clear", portuguese))
        assertEquals("Praticamente limpo", localizedWeatherDescription("MAINLY CLEAR", portuguese))
    }

    @Test
    fun `unmapped phrase falls back to capitalized English even in Portuguese`() {
        assertEquals("Some unmapped phrase", localizedWeatherDescription("some unmapped phrase", portuguese))
    }

    @Test
    fun `non-Portuguese locale always falls back to capitalized English`() {
        assertEquals("Mainly clear", localizedWeatherDescription("mainly clear", english))
    }

    @Test
    fun `capitalizes the English fallback regardless of input casing`() {
        assertEquals("Clear sky", localizedWeatherDescription("clear sky", english))
    }
}
