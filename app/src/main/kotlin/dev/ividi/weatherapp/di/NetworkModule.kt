package dev.ividi.weatherapp.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.ividi.weatherapp.BuildConfig
import dev.ividi.weatherapp.data.network.AuthInterceptor
import dev.ividi.weatherapp.data.network.RefreshApi
import dev.ividi.weatherapp.data.network.TokenAuthenticator
import dev.ividi.weatherapp.data.network.WeatherApiService
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.Retrofit

/**
 * Points at the live, deployed backend. `network_security_config.xml` still carries a
 * cleartext exception for `10.0.2.2`/`localhost` for anyone switching this back to a local
 * backend during development; it has no effect on this HTTPS URL.
 */
private const val BASE_URL = "https://weather-api-production-68ff.up.railway.app/"
private const val CONNECT_TIMEOUT_SECONDS = 15L
private const val READ_TIMEOUT_SECONDS = 15L
private const val JSON_MEDIA_TYPE = "application/json"

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private fun loggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor, tokenAuthenticator: TokenAuthenticator): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor())
            .authenticator(tokenAuthenticator)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
        val contentType: okhttp3.MediaType = JSON_MEDIA_TYPE.toMediaType()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideWeatherApiService(retrofit: Retrofit): WeatherApiService =
        retrofit.create(WeatherApiService::class.java)

    /**
     * No [AuthInterceptor] or [TokenAuthenticator] attached -- this is the client
     * [TokenAuthenticator] itself uses to call `/auth/refresh`, so it must not depend on the
     * authenticator it backs (that would recurse on a failed refresh).
     */
    @Provides
    @Singleton
    @RefreshApi
    fun provideRefreshOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor())
            .build()
    }

    @Provides
    @Singleton
    @RefreshApi
    fun provideRefreshRetrofit(@RefreshApi okHttpClient: OkHttpClient, json: Json): Retrofit {
        val contentType: okhttp3.MediaType = JSON_MEDIA_TYPE.toMediaType()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    @RefreshApi
    fun provideRefreshWeatherApiService(@RefreshApi retrofit: Retrofit): WeatherApiService =
        retrofit.create(WeatherApiService::class.java)
}
