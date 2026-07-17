package dev.ividi.weatherapp.util

import dev.ividi.weatherapp.data.model.ProviderResult

private const val MIN_SUCCESSFUL_RESULTS_FOR_AVERAGE = 2

/**
 * Average current temperature across every provider that succeeded, or null when fewer than
 * [MIN_SUCCESSFUL_RESULTS_FOR_AVERAGE] providers succeeded (a single data point isn't an "average").
 */
fun averageTemperatureAcrossProviders(results: List<ProviderResult>): Double? {
    val successfulTemperatures = results.mapNotNull { result ->
        if (result.success) result.weather?.temperature else null
    }
    if (successfulTemperatures.size < MIN_SUCCESSFUL_RESULTS_FOR_AVERAGE) return null
    return successfulTemperatures.average()
}
