package dev.ividi.weatherapp.data.repository

import dev.ividi.weatherapp.data.model.HistoryEntry
import dev.ividi.weatherapp.data.network.WeatherApiService
import dev.ividi.weatherapp.data.network.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json

@Singleton
class HistoryRepository @Inject constructor(
    private val apiService: WeatherApiService,
    private val json: Json,
) {

    /** Newest first, per the backend contract. */
    suspend fun getHistory(): List<HistoryEntry> = safeApiCall(json) {
        apiService.getHistory().sortedByDescending { it.searchedAt }
    }
}
