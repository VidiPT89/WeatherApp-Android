package dev.ividi.weatherapp.widget

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dev.ividi.weatherapp.data.model.Units
import dev.ividi.weatherapp.data.repository.PreferencesRepository
import dev.ividi.weatherapp.data.repository.WeatherRepository
import dev.ividi.weatherapp.data.repository.WeatherWidgetRepository
import dev.ividi.weatherapp.location.LocationService
import dev.ividi.weatherapp.util.isNightAt

/**
 * Periodic background refresh so the home-screen widget doesn't keep showing stale (or, after a
 * device reboot with the app never opened, no) data until the user happens to open the app --
 * see [WeatherWidgetRepository]'s doc comment for why the widget itself never fetches on its own.
 * This worker is the one exception to that "app-driven only" rule, deliberately kept as thin as
 * possible: it just replays the same nearby-lookup + [WeatherWidgetRepository.saveSnapshot] path
 * [dev.ividi.weatherapp.ui.dashboard.DashboardViewModel] already uses, on a schedule instead of
 * "the app happened to be open".
 *
 * Silently does nothing (never [Result.failure]) when location permission isn't granted or the
 * lookup fails for any reason -- same "fails silently, don't nag" stance as the app's own
 * `loadNearbyWeather`; the next scheduled run tries again regardless.
 */
@HiltWorker
class WeatherWidgetRefreshWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val locationService: LocationService,
    private val weatherRepository: WeatherRepository,
    private val preferencesRepository: PreferencesRepository,
    private val weatherWidgetRepository: WeatherWidgetRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return Result.success()
        }

        runCatching {
            val location = locationService.getCurrentLocation()
            val units = runCatching { preferencesRepository.getPreferredUnits() }.getOrDefault(Units.METRIC)
            val weather = weatherRepository.getWeatherNearby(location.latitude, location.longitude, units)
            // Best-effort: today's sunrise/sunset (only on the forecast endpoint, not the nearby
            // one) is what CurrentWeatherCard also relies on to know it's night -- a failure here
            // just means the widget renders as if it were day, same fallback the app itself uses
            // when it has no forecast on hand yet.
            val isNight = runCatching { weatherRepository.getForecast(weather.city, units) }.getOrNull()
                ?.isNightAt(weather.observedAt) ?: false
            weatherWidgetRepository.saveSnapshot(weather, isNight)
        }

        return Result.success()
    }
}
