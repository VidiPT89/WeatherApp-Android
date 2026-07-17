package dev.ividi.weatherapp.data.repository

import dev.ividi.weatherapp.data.model.UnitsPreference
import dev.ividi.weatherapp.data.model.Units
import dev.ividi.weatherapp.data.network.WeatherApiService
import dev.ividi.weatherapp.data.network.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json

@Singleton
class PreferencesRepository @Inject constructor(
    private val apiService: WeatherApiService,
    private val json: Json,
) {

    suspend fun getPreferredUnits(): Units = safeApiCall(json) {
        apiService.getPreferences().units
    }

    suspend fun updatePreferredUnits(units: Units): Units = safeApiCall(json) {
        apiService.updatePreferences(UnitsPreference(units = units)).units
    }
}
