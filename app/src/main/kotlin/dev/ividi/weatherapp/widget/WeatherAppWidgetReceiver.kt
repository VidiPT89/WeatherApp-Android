package dev.ividi.weatherapp.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/** System entry point for the home-screen widget, registered in AndroidManifest.xml. */
class WeatherAppWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WeatherGlanceWidget()

    /**
     * Fires when the widget is first placed on the home screen (and on every subsequent
     * APPWIDGET_UPDATE broadcast, which the system never sends on its own timer here since
     * `updatePeriodMillis="0"` -- so in practice this is "widget just got added"). Kicks off an
     * immediate one-off refresh instead of leaving the user staring at "no data yet" until
     * [WeatherWidgetRefreshWorker]'s next periodic run, up to [WeatherWidgetRefreshScheduler]'s
     * full interval away.
     */
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        WeatherWidgetRefreshScheduler.refreshNow(context)
    }
}
