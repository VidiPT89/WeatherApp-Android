package dev.ividi.weatherapp.util

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private const val MINUTE_PLACEHOLDER_PAD = 2

/** Formats an [Instant] as "dd/MM/yyyy HH:mm" in the device's local timezone. */
fun Instant.toDisplayDateTime(): String {
    val local = this.toLocalDateTime(TimeZone.currentSystemDefault())
    val day = local.dayOfMonth.toString().padStart(MINUTE_PLACEHOLDER_PAD, '0')
    val month = local.monthNumber.toString().padStart(MINUTE_PLACEHOLDER_PAD, '0')
    val hour = local.hour.toString().padStart(MINUTE_PLACEHOLDER_PAD, '0')
    val minute = local.minute.toString().padStart(MINUTE_PLACEHOLDER_PAD, '0')
    return "$day/$month/${local.year} $hour:$minute"
}
