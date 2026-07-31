package dev.ividi.weatherapp.data.repository

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.glance.appwidget.updateAll
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.ividi.weatherapp.data.model.WeatherResponse
import dev.ividi.weatherapp.data.model.WeatherWidgetSnapshot
import dev.ividi.weatherapp.widget.WeatherGlanceWidget
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.weatherWidgetDataStore by preferencesDataStore(name = "weather_widget_snapshot")

private val CITY_KEY = stringPreferencesKey("city")
private val COUNTRY_KEY = stringPreferencesKey("country")
private val TEMPERATURE_KEY = doublePreferencesKey("temperature")
private val TEMPERATURE_SYMBOL_KEY = stringPreferencesKey("temperature_symbol")
private val DESCRIPTION_KEY = stringPreferencesKey("description")
private val LAST_UPDATED_KEY = longPreferencesKey("last_updated_epoch_millis")

/**
 * Persists the last weather the Dashboard successfully loaded, purely so the home-screen widget
 * can show *something* without ever fetching on its own -- an explicit scope decision: the widget
 * is a passive mirror of whatever the app itself last saw, never an independent network client
 * (see [WeatherWidgetSnapshot] and `res/xml/weather_app_widget_info.xml`'s
 * `updatePeriodMillis="0"`).
 *
 * [saveSnapshot] is called by [dev.ividi.weatherapp.ui.dashboard.DashboardViewModel] after every
 * successful weather load, and immediately nudges any placed widgets to redraw with the new data
 * via [updateAll] rather than waiting on the (here disabled) system periodic refresh.
 */
@Singleton
class WeatherWidgetRepository @Inject constructor(@ApplicationContext private val context: Context) {

    private val dataStore = context.weatherWidgetDataStore

    val snapshot: Flow<WeatherWidgetSnapshot?> = dataStore.data.map { it.toSnapshot() }

    suspend fun currentSnapshot(): WeatherWidgetSnapshot? = snapshot.first()

    suspend fun saveSnapshot(weather: WeatherResponse) {
        dataStore.edit { prefs ->
            prefs[CITY_KEY] = weather.city
            prefs[COUNTRY_KEY] = weather.country
            prefs[TEMPERATURE_KEY] = weather.temperature
            prefs[TEMPERATURE_SYMBOL_KEY] = weather.units.temperatureSymbol
            prefs[DESCRIPTION_KEY] = weather.description
            prefs[LAST_UPDATED_KEY] = System.currentTimeMillis()
        }
        WeatherGlanceWidget().updateAll(context)
    }

    private fun Preferences.toSnapshot(): WeatherWidgetSnapshot? {
        val city = this[CITY_KEY] ?: return null
        return WeatherWidgetSnapshot(
            city = city,
            country = this[COUNTRY_KEY].orEmpty(),
            temperature = this[TEMPERATURE_KEY] ?: 0.0,
            temperatureSymbol = this[TEMPERATURE_SYMBOL_KEY] ?: "°C",
            description = this[DESCRIPTION_KEY].orEmpty(),
            lastUpdatedEpochMillis = this[LAST_UPDATED_KEY] ?: 0L,
        )
    }
}
