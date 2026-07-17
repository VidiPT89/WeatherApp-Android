package dev.ividi.weatherapp.data.repository

import dev.ividi.weatherapp.data.model.GeocodingResult
import dev.ividi.weatherapp.data.network.WeatherApiService
import dev.ividi.weatherapp.data.network.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json

private const val DEFAULT_SUGGESTION_LIMIT = 5

@Singleton
class GeocodingRepository @Inject constructor(
    private val apiService: WeatherApiService,
    private val json: Json,
) {

    suspend fun searchCities(query: String, limit: Int = DEFAULT_SUGGESTION_LIMIT): List<GeocodingResult> =
        safeApiCall(json) {
            apiService.searchCities(query = query, limit = limit).results
        }
}
