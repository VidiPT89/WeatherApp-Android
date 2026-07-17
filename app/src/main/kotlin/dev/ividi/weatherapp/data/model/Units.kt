package dev.ividi.weatherapp.data.model

import kotlinx.serialization.Serializable

/** Mirrors the backend's `units` string enum ("metric" | "imperial"). */
@Serializable
enum class Units {
    @kotlinx.serialization.SerialName("metric")
    METRIC,

    @kotlinx.serialization.SerialName("imperial")
    IMPERIAL;

    val wireValue: String
        get() = if (this == METRIC) "metric" else "imperial"

    val temperatureSymbol: String
        get() = if (this == METRIC) "°C" else "°F"

    val windSpeedUnit: String
        get() = if (this == METRIC) "km/h" else "mph"

    fun toggled(): Units = if (this == METRIC) IMPERIAL else METRIC

    companion object {
        fun fromWireValue(value: String): Units =
            if (value.equals("imperial", ignoreCase = true)) IMPERIAL else METRIC
    }
}
