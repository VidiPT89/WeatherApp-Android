package dev.ividi.weatherapp.data.model

/**
 * The last weather the main app itself successfully fetched, persisted purely so the home-screen
 * widget (see the `widget` package) has something to show. Per this feature's explicit scope, the
 * widget never fetches on its own -- it only ever mirrors whatever the Dashboard last loaded, and
 * only updates when the app itself pushes a fresh snapshot (see
 * [dev.ividi.weatherapp.data.repository.WeatherWidgetRepository]).
 */
data class WeatherWidgetSnapshot(
    val city: String,
    val country: String,
    val temperature: Double,
    val temperatureSymbol: String,
    /** Free-text condition description from the backend (e.g. "light rain") -- both the display
     * copy and the keyword source for the widget's condition icon/emoji. */
    val description: String,
    val lastUpdatedEpochMillis: Long,
)
