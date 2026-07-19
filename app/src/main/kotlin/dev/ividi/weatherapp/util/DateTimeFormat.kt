package dev.ividi.weatherapp.util

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private const val PLACEHOLDER_PAD = 2

/** Formats an [Instant] as "dd/MM/yyyy HH:mm" in the device's local timezone. */
fun Instant.toDisplayDateTime(): String {
    val local = this.toLocalDateTime(TimeZone.currentSystemDefault())
    return local.toDisplayDateTime()
}

/** Formats a local [LocalDateTime] as "dd/MM/yyyy HH:mm". */
fun LocalDateTime.toDisplayDateTime(): String {
    val day = dayOfMonth.toString().padStart(PLACEHOLDER_PAD, '0')
    val month = monthNumber.toString().padStart(PLACEHOLDER_PAD, '0')
    return "$day/$month/$year ${toDisplayTime()}"
}

/** Formats a local [LocalDateTime] as "HH:mm" -- used for sunrise/sunset display. */
fun LocalDateTime.toDisplayTime(): String {
    val hour = hour.toString().padStart(PLACEHOLDER_PAD, '0')
    val minute = minute.toString().padStart(PLACEHOLDER_PAD, '0')
    return "$hour:$minute"
}
