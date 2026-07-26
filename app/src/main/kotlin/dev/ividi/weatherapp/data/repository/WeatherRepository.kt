package dev.ividi.weatherapp.data.repository

import dev.ividi.weatherapp.data.model.ForecastResponse
import dev.ividi.weatherapp.data.model.Units
import dev.ividi.weatherapp.data.model.WeatherResponse
import dev.ividi.weatherapp.data.network.WeatherApiService
import dev.ividi.weatherapp.data.network.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json

/** Current weather + forecast for a city. Both endpoints are called concurrently by the caller. */
@Singleton
class WeatherRepository @Inject constructor(
    private val apiService: WeatherApiService,
    private val json: Json,
) {

    suspend fun getWeather(city: String, units: Units?): WeatherResponse = safeApiCall(json) {
        apiService.getWeather(city = city, units = units?.wireValue)
    }

    suspend fun getWeatherNearby(latitude: Double, longitude: Double, units: Units?): WeatherResponse =
        safeApiCall(json) {
            apiService.getWeatherNearby(lat = latitude, lon = longitude, units = units?.wireValue)
        }

    suspend fun getForecast(city: String, units: Units?): ForecastResponse = safeApiCall(json) {
        apiService.getForecast(city = city, units = units?.wireValue)
    }
}
