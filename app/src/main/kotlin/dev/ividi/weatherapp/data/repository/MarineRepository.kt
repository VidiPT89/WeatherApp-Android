package dev.ividi.weatherapp.data.repository

import dev.ividi.weatherapp.data.model.MarineResponse
import dev.ividi.weatherapp.data.model.Units
import dev.ividi.weatherapp.data.network.WeatherApiService
import dev.ividi.weatherapp.data.network.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json

/** Sea conditions (water temperature, swell) for a city, from `/api/v1/weather/marine`. */
@Singleton
class MarineRepository @Inject constructor(
    private val apiService: WeatherApiService,
    private val json: Json,
) {

    suspend fun getMarine(city: String, units: Units?): MarineResponse = safeApiCall(json) {
        apiService.getMarine(city = city, units = units?.wireValue)
    }
}
