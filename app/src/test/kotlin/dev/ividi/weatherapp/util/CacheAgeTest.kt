package dev.ividi.weatherapp.util

import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class CacheAgeTest {

    @Test
    fun `computes zero age when observed exactly now`() {
        val now = Instant.parse("2024-06-01T12:00:00Z")
        val age = cacheAgeBetween(observedAt = now, now = now)
        assertEquals(0L, age.totalSeconds)
    }

    @Test
    fun `computes elapsed seconds between observedAt and now`() {
        val observedAt = Instant.parse("2024-06-01T12:00:00Z")
        val now = Instant.parse("2024-06-01T12:00:45Z")
        val age = cacheAgeBetween(observedAt, now)
        assertEquals(45L, age.totalSeconds)
    }

    @Test
    fun `clamps a negative elapsed time (clock skew) to zero`() {
        val observedAt = Instant.parse("2024-06-01T12:00:10Z")
        val now = Instant.parse("2024-06-01T12:00:00Z")
        val age = cacheAgeBetween(observedAt, now)
        assertEquals(0L, age.totalSeconds)
    }

    @Test
    fun `rejects a negative totalSeconds`() {
        assertThrows(IllegalArgumentException::class.java) { CacheAge(-1L) }
    }

    @Test
    fun `formats seconds under a minute as Xs`() {
        assertEquals("45s", CacheAge(45L).toCompactDisplayString())
    }

    @Test
    fun `formats seconds under an hour as Xmin`() {
        assertEquals("5min", CacheAge(300L).toCompactDisplayString())
    }

    @Test
    fun `formats seconds an hour or more as Xh`() {
        assertEquals("2h", CacheAge(7_200L).toCompactDisplayString())
    }

    @Test
    fun `boundary of exactly sixty seconds formats as minutes`() {
        assertEquals("1min", CacheAge(60L).toCompactDisplayString())
    }

    @Test
    fun `boundary of exactly one hour formats as hours`() {
        assertEquals("1h", CacheAge(3_600L).toCompactDisplayString())
    }
}
