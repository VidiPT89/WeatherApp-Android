package dev.ividi.weatherapp.util

import kotlinx.datetime.Instant

private const val SECONDS_PER_MINUTE = 60L
private const val SECONDS_PER_HOUR = 3600L

/** Non-negative age, in whole seconds, of a cached value. */
@JvmInline
value class CacheAge(val totalSeconds: Long) {
    init {
        require(totalSeconds >= 0) { "totalSeconds must not be negative, was $totalSeconds" }
    }
}

/** Pure function: how old is a cached [observedAt] timestamp relative to [now]? */
fun cacheAgeBetween(observedAt: Instant, now: Instant): CacheAge {
    val elapsedSeconds = (now - observedAt).inWholeSeconds
    return CacheAge(elapsedSeconds.coerceAtLeast(0))
}

/** Renders as "Xs" / "Xmin" / "Xh", matching the dashboard's cache badge copy. */
fun CacheAge.toCompactDisplayString(): String = when {
    totalSeconds < SECONDS_PER_MINUTE -> "${totalSeconds}s"
    totalSeconds < SECONDS_PER_HOUR -> "${totalSeconds / SECONDS_PER_MINUTE}min"
    else -> "${totalSeconds / SECONDS_PER_HOUR}h"
}
