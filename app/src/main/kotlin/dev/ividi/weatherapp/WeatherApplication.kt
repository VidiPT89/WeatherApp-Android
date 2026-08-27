package dev.ividi.weatherapp

import android.app.Application
import androidx.work.Configuration
import androidx.hilt.work.HiltWorkerFactory
import dagger.hilt.android.HiltAndroidApp
import dev.ividi.weatherapp.data.repository.AppPreferencesRepository
import dev.ividi.weatherapp.widget.WeatherWidgetRefreshScheduler
import javax.inject.Inject
import kotlinx.coroutines.runBlocking

@HiltAndroidApp
class WeatherApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var appPreferencesRepository: AppPreferencesRepository

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        // A tiny synchronous local-disk read at cold start, before any Activity/Compose UI is
        // created, so the very first frame is already in the user's chosen language. AppCompat's
        // own auto-store service (see AndroidManifest) also persists/restores this locale
        // independently below API 33; this call keeps our DataStore-backed Settings UI and the
        // actual applied locale in sync on every launch.
        runBlocking { appPreferencesRepository.applyPersistedLanguage() }

        WeatherWidgetRefreshScheduler.schedule(this)
    }
}
