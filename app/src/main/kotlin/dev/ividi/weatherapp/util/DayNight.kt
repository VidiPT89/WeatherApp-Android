package dev.ividi.weatherapp.util

import dev.ividi.weatherapp.data.model.ForecastResponse
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.asTimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Whether [observedAt] falls outside today's sunrise/sunset. Sunrise and sunset are the city's
 * local times with no offset, so the instant is converted with the city's own UTC offset --
 * comparing it in UTC or in the device's zone would be hours off for a city elsewhere. Without
 * the offset (an older backend), the device's zone is the best remaining guess.
 */
fun ForecastResponse.isNightAt(observedAt: Instant): Boolean {
    val today = daily.firstOrNull() ?: return false
    val zone = utcOffsetSeconds?.let { UtcOffset(seconds = it).asTimeZone() } ?: TimeZone.currentSystemDefault()
    val local = observedAt.toLocalDateTime(zone)
    return local < today.sunrise || local > today.sunset
}
