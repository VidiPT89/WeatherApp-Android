package dev.ividi.weatherapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class MoonPhaseInfo(
    val phase: String,
    val illuminationPercent: Int,
)

/**
 * Mirrors the backend's `WeatherInsightsResponse` JSON shape returned by
 * `/api/v1/weather/insights` -- a handful of derived "nice to know" indicators computed
 * server-side from data already fetched for the dashboard. [fishingConditionLabel] is `null`
 * for inland/non-coastal cities with no marine data; the other fields apply to any city.
 */
@Serializable
data class WeatherInsightsResponse(
    val city: String,
    val country: String,
    val moonPhase: MoonPhaseInfo,
    val uvRiskLabel: String,
    val outdoorActivityScore: Int,
    val outdoorActivityLabel: String,
    val fishingConditionLabel: String? = null,
)
