package dev.ividi.weatherapp.data.repository

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Manual dark/light override, independent of Material You's dynamic-color axis. */
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

/** The app's two supported UI languages. PT-PT is the default/fallback. */
enum class AppLanguage(val languageTag: String) {
    PT("pt"),
    EN("en"),
}

private val Context.appPreferencesDataStore by preferencesDataStore(name = "app_preferences")

private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
private val LANGUAGE_KEY = stringPreferencesKey("language")

/**
 * Persists the user's theme and language choices in DataStore, and applies the language
 * through [AppCompatDelegate.setApplicationLocales] (see `AppLocalesMetadataHolderService` in
 * the manifest for the cross-restart backport on API < 33).
 */
@Singleton
class AppPreferencesRepository @Inject constructor(@ApplicationContext context: Context) {

    private val dataStore = context.appPreferencesDataStore

    val themeMode: Flow<ThemeMode> = dataStore.data.map { prefs ->
        ThemeMode.entries.find { it.name == prefs[THEME_MODE_KEY] } ?: ThemeMode.SYSTEM
    }

    val language: Flow<AppLanguage> = dataStore.data.map { prefs ->
        AppLanguage.entries.find { it.languageTag == prefs[LANGUAGE_KEY] } ?: AppLanguage.PT
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[THEME_MODE_KEY] = mode.name }
    }

    suspend fun setLanguage(language: AppLanguage) {
        dataStore.edit { it[LANGUAGE_KEY] = language.languageTag }
        applyLocale(language)
    }

    /** Re-applies the currently persisted language; used at cold start (see [WeatherApplication]). */
    suspend fun applyPersistedLanguage() {
        applyLocale(language.first())
    }

    private fun applyLocale(language: AppLanguage) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.languageTag))
    }
}
