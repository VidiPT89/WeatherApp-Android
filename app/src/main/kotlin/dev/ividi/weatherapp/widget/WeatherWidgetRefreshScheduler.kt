package dev.ividi.weatherapp.widget

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

private const val UNIQUE_WORK_NAME = "weather_widget_refresh"
private const val UNIQUE_IMMEDIATE_WORK_NAME = "weather_widget_refresh_now"
private val REFRESH_INTERVAL = 3L to TimeUnit.HOURS

/**
 * Schedules [WeatherWidgetRefreshWorker] to run periodically, keeping the home-screen widget from
 * going stale between app opens (including across a device reboot -- WorkManager re-arms its own
 * pending periodic work on boot without any extra `RECEIVE_BOOT_COMPLETED` handling needed here).
 *
 * [ExistingPeriodicWorkPolicy.KEEP] makes this idempotent: calling it on every app cold start (see
 * `WeatherApplication.onCreate`) is safe, it only actually schedules once.
 */
object WeatherWidgetRefreshScheduler {

    fun schedule(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<WeatherWidgetRefreshWorker>(REFRESH_INTERVAL.first, REFRESH_INTERVAL.second)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(UNIQUE_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    /**
     * One-off immediate refresh, in addition to the periodic schedule above. Called when a widget
     * is first placed on the home screen ([WeatherAppWidgetReceiver.onUpdate]) so it shows real
     * data right away instead of "no data yet" for up to a full [REFRESH_INTERVAL] until the next
     * periodic run.
     */
    fun refreshNow(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<WeatherWidgetRefreshWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(UNIQUE_IMMEDIATE_WORK_NAME, ExistingWorkPolicy.KEEP, request)
    }
}
