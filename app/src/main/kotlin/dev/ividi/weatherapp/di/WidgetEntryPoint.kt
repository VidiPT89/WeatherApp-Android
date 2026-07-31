package dev.ividi.weatherapp.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.ividi.weatherapp.data.repository.WeatherWidgetRepository

/**
 * [WeatherGlanceWidget][dev.ividi.weatherapp.widget.WeatherGlanceWidget] is instantiated by the
 * system (via [dev.ividi.weatherapp.widget.WeatherAppWidgetReceiver]), not by Hilt, so it can't
 * get its dependencies through a normal `@Inject` constructor. This lets `provideGlance` reach
 * into the app's own Hilt graph via [dagger.hilt.android.EntryPointAccessors] instead.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WeatherWidgetEntryPoint {
    fun weatherWidgetRepository(): WeatherWidgetRepository
}
