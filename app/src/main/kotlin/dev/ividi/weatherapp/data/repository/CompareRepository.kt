package dev.ividi.weatherapp.data.repository

import dev.ividi.weatherapp.data.model.CompareResponse
import dev.ividi.weatherapp.data.model.Units
import dev.ividi.weatherapp.data.network.WeatherApiService
import dev.ividi.weatherapp.data.network.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json

@Singleton
class CompareRepository @Inject constructor(
    private val apiService: WeatherApiService,
    private val json: Json,
) {

    suspend fun compareProviders(city: String, units: Units?): CompareResponse = safeApiCall(json) {
        apiService.compareProviders(city = city, units = units?.wireValue)
    }
}
