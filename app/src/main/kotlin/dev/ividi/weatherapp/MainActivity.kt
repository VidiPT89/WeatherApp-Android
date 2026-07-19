package dev.ividi.weatherapp

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import dev.ividi.weatherapp.data.repository.ThemeMode
import dev.ividi.weatherapp.ui.navigation.WeatherAppNavGraph
import dev.ividi.weatherapp.ui.theme.WeatherAppTheme
import java.util.Locale
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Extends [AppCompatActivity] (not [androidx.activity.ComponentActivity]) so the per-app
 * language backport (`AppCompatDelegate.setApplicationLocales`) works below Android 13 --
 * `setContent {}` for Compose is unaffected by this base-class change.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    /**
     * [WeatherApplication.onCreate] already calls `AppCompatDelegate.setApplicationLocales()`
     * for the persisted language, but that call happens before any Activity exists and does
     * NOT reliably affect *this* very first Activity's initial `Resources` -- `setApplicationLocales`
     * applies through an activity-recreate cycle, and there's no "previous" instance of this,
     * the very first one, to recreate. Wrapping `newBase` in a `Configuration` carrying the
     * persisted locale directly -- synchronously, before `super.attachBaseContext` -- guarantees
     * this first cold-started screen's own `Resources` resolve strings in the right language
     * instead of falling back to the device locale.
     *
     * Hilt's field injection for this Activity hasn't run yet at this point in the lifecycle,
     * so this reads the repository via the Application instance, which Hilt has already
     * populated by the time any Activity's `attachBaseContext` runs.
     */
    override fun attachBaseContext(newBase: Context) {
        val app = newBase.applicationContext as WeatherApplication
        val languageTag = runBlocking { app.appPreferencesRepository.language.first().languageTag }

        val locale = Locale.forLanguageTag(languageTag)
        Locale.setDefault(locale)
        val config = Configuration(newBase.resources.configuration).apply { setLocale(locale) }
        val localizedContext = newBase.createConfigurationContext(config)

        super.attachBaseContext(localizedContext)
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageTag))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by mainViewModel.themeMode.collectAsStateWithLifecycle()
            val systemInDarkTheme = isSystemInDarkTheme()
            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> systemInDarkTheme
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            WeatherAppTheme(darkTheme = darkTheme) {
                WeatherAppNavGraph()
            }
        }
    }
}
