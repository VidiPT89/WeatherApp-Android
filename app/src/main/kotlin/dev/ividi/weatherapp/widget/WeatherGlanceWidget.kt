package dev.ividi.weatherapp.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.material3.ColorProviders
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import dagger.hilt.android.EntryPointAccessors
import dev.ividi.weatherapp.R
import dev.ividi.weatherapp.data.model.WeatherWidgetSnapshot
import dev.ividi.weatherapp.di.WeatherWidgetEntryPoint
import dev.ividi.weatherapp.ui.theme.DarkColors
import dev.ividi.weatherapp.ui.theme.LightColors
import dev.ividi.weatherapp.util.cacheAgeBetween
import dev.ividi.weatherapp.util.toCompactDisplayString
import kotlin.math.roundToInt
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

private val SMALL_SIZE = DpSize(110.dp, 110.dp)
private val MEDIUM_SIZE = DpSize(200.dp, 110.dp)

/**
 * Home-screen widget showing the last weather the main app itself fetched. Per this feature's
 * explicit scope, it never fetches on its own -- there's no network/work-manager code here at
 * all, only a read of [WeatherWidgetRepository][dev.ividi.weatherapp.data.repository.WeatherWidgetRepository]'s
 * persisted snapshot, refreshed only when [dev.ividi.weatherapp.ui.dashboard.DashboardViewModel]
 * pushes a new one after a successful load.
 */
class WeatherGlanceWidget : GlanceAppWidget() {

    // Responsive rather than Exact: home-screen launchers can place this widget at several grid
    // sizes, and a slightly larger layout (bigger type, room for the description line) fits
    // better once there's more than the smallest cell available.
    override val sizeMode: SizeMode = SizeMode.Responsive(setOf(SMALL_SIZE, MEDIUM_SIZE))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = EntryPointAccessors
            .fromApplication(context.applicationContext, WeatherWidgetEntryPoint::class.java)
            .weatherWidgetRepository()
        val snapshot = repository.currentSnapshot()

        provideContent {
            // Static (non-dynamic) app colors -- same fallback palette WeatherAppTheme uses when
            // dynamic color isn't available -- so the widget doesn't look visually unrelated to
            // the rest of the app.
            GlanceTheme(colors = ColorProviders(LightColors, DarkColors)) {
                WeatherWidgetContent(snapshot)
            }
        }
    }
}

@Composable
private fun WeatherWidgetContent(snapshot: WeatherWidgetSnapshot?) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.primaryContainer)
            .cornerRadius(16.dp)
            .padding(12.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (snapshot == null) {
            EmptyWidgetContent()
        } else {
            FilledWidgetContent(snapshot)
        }
    }
}

/** Fresh install / never opened the app yet -- a clear placeholder instead of a crash or blank. */
@Composable
private fun EmptyWidgetContent() {
    val context = LocalContext.current
    Text(
        text = context.getString(R.string.widget_no_data_yet),
        style = TextStyle(
            color = GlanceTheme.colors.onPrimaryContainer,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
        ),
    )
}

@Composable
private fun FilledWidgetContent(snapshot: WeatherWidgetSnapshot) {
    val context = LocalContext.current
    val age = cacheAgeBetween(Instant.fromEpochMilliseconds(snapshot.lastUpdatedEpochMillis), Clock.System.now())

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = snapshot.city,
            maxLines = 1,
            style = TextStyle(
                color = GlanceTheme.colors.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
            ),
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = widgetConditionEmoji(snapshot.description), style = TextStyle(fontSize = 26.sp))
            Spacer(modifier = GlanceModifier.width(6.dp))
            Text(
                text = "${snapshot.temperature.roundToInt()}${snapshot.temperatureSymbol}",
                style = TextStyle(
                    color = GlanceTheme.colors.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                ),
            )
        }
        Text(
            text = snapshot.description.replaceFirstChar { it.uppercase() },
            maxLines = 1,
            style = TextStyle(color = GlanceTheme.colors.onPrimaryContainer, fontSize = 12.sp),
        )
        Text(
            text = context.getString(R.string.widget_last_updated, age.toCompactDisplayString()),
            maxLines = 1,
            style = TextStyle(color = GlanceTheme.colors.onPrimaryContainer, fontSize = 10.sp),
        )
    }
}

/**
 * Same keyword-matching approach as [dev.ividi.weatherapp.ui.common.weatherConditionGradient] --
 * deliberately simple substring matching over the backend's free-text description, not an
 * exhaustive condition taxonomy. Emoji rather than a drawable icon set: Glance widgets render via
 * RemoteViews, and a plain [androidx.glance.text.Text] glyph avoids adding/maintaining a parallel
 * set of condition icon assets just for the widget.
 */
private fun widgetConditionEmoji(description: String): String {
    val normalized = description.lowercase()
    return when {
        "thunderstorm" in normalized -> "⛈️"
        "snow" in normalized -> "❄️"
        "drizzle" in normalized || "rain" in normalized -> "🌧️"
        "fog" in normalized || "mist" in normalized || "haze" in normalized -> "🌫️"
        "overcast" in normalized || "cloud" in normalized -> "☁️"
        "clear" in normalized || "sun" in normalized -> "☀️"
        else -> "🌡️"
    }
}
