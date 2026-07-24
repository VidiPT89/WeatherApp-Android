package dev.ividi.weatherapp.data.repository

import dev.ividi.weatherapp.data.model.Units
import dev.ividi.weatherapp.data.model.WeatherInsightsResponse
import dev.ividi.weatherapp.data.network.WeatherApiService
import dev.ividi.weatherapp.data.network.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json

/** Derived weather insights (moon phase, UV risk, activity score, fishing conditions) for a city, from `/api/v1/weather/insights`. */
@Singleton
class InsightsRepository @Inject constructor(
    private val apiService: WeatherApiService,
    private val json: Json,
) {

    suspend fun getInsights(city: String, units: Units?): WeatherInsightsResponse = safeApiCall(json) {
        apiService.getInsights(city = city, units = units?.wireValue)
    }
}
