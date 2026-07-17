package dev.ividi.weatherapp.data.repository

import dev.ividi.weatherapp.data.model.FavoriteEntry
import dev.ividi.weatherapp.data.model.FavoriteRequest
import dev.ividi.weatherapp.data.network.WeatherApiService
import dev.ividi.weatherapp.data.network.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json

@Singleton
class FavoritesRepository @Inject constructor(
    private val apiService: WeatherApiService,
    private val json: Json,
) {

    suspend fun getFavorites(): List<FavoriteEntry> = safeApiCall(json) {
        apiService.getFavorites()
    }

    suspend fun addFavorite(city: String): FavoriteEntry = safeApiCall(json) {
        apiService.addFavorite(FavoriteRequest(city = city))
    }
}
