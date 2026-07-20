package dev.ividi.weatherapp.data.network

import javax.inject.Qualifier

/**
 * Marks the plain (no [AuthInterceptor], no [TokenAuthenticator]) `OkHttpClient`/`Retrofit`/
 * [WeatherApiService] trio used only to call `/auth/refresh` -- attaching either of those to
 * this client would recurse back into [TokenAuthenticator] on a failed refresh.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RefreshApi
