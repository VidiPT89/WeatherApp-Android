package dev.ividi.weatherapp.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FallbackDetectionTest {

    @Test
    fun `open-meteo is not a fallback`() {
        assertFalse(isFallbackProvider("open-meteo"))
    }

    @Test
    fun `open-weather-map is a fallback`() {
        assertTrue(isFallbackProvider("open-weather-map"))
    }

    @Test
    fun `any provider other than the primary is treated as a fallback`() {
        assertTrue(isFallbackProvider("some-other-provider"))
    }
}
