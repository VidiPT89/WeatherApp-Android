package dev.ividi.weatherapp.data.network

import dev.ividi.weatherapp.data.model.AuthResponse
import dev.ividi.weatherapp.data.model.CompareResponse
import dev.ividi.weatherapp.data.model.FavoriteEntry
import dev.ividi.weatherapp.data.model.FavoriteRequest
import dev.ividi.weatherapp.data.model.ForecastResponse
import dev.ividi.weatherapp.data.model.GeocodingResponse
import dev.ividi.weatherapp.data.model.HistoryEntry
import dev.ividi.weatherapp.data.model.LoginRequest
import dev.ividi.weatherapp.data.model.RegisterRequest
import dev.ividi.weatherapp.data.model.UnitsPreference
import dev.ividi.weatherapp.data.model.WeatherResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/** Retrofit description of every backend endpoint consumed by the app. */
interface WeatherApiService {

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @GET("api/v1/weather")
    suspend fun getWeather(
        @Query("city") city: String,
        @Query("units") units: String? = null,
    ): WeatherResponse

    @GET("api/v1/weather/forecast")
    suspend fun getForecast(
        @Query("city") city: String,
        @Query("units") units: String? = null,
    ): ForecastResponse

    @GET("api/v1/weather/compare")
    suspend fun compareProviders(
        @Query("city") city: String,
        @Query("units") units: String? = null,
    ): CompareResponse

    @GET("api/v1/weather/history")
    suspend fun getHistory(): List<HistoryEntry>

    @GET("api/v1/weather/favorites")
    suspend fun getFavorites(): List<FavoriteEntry>

    @POST("api/v1/weather/favorites")
    suspend fun addFavorite(@Body request: FavoriteRequest): FavoriteEntry

    @GET("api/v1/user/preferences")
    suspend fun getPreferences(): UnitsPreference

    @POST("api/v1/user/preferences")
    suspend fun updatePreferences(@Body request: UnitsPreference): UnitsPreference

    @GET("api/v1/geocoding")
    suspend fun searchCities(
        @Query("query") query: String,
        @Query("limit") limit: Int = 5,
    ): GeocodingResponse
}
