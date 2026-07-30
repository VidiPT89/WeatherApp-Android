package dev.ividi.weatherapp.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

class LocationUnavailableException : Exception("Não foi possível obter a localização.")

private const val LOCATION_TIMEOUT_MS = 10_000L

/**
 * One-shot location fix via the platform LocationManager (no Play Services dependency, since
 * this app has no other use for it). The caller is responsible for having already obtained
 * ACCESS_COARSE_LOCATION -- this class assumes permission is granted.
 */
@Singleton
class LocationService @Inject constructor(@ApplicationContext private val context: Context) {

    /**
     * Resolves within [LOCATION_TIMEOUT_MS], throwing [LocationUnavailableException] if no fix
     * arrives in time -- e.g. a provider that's enabled but never actually produces a location
     * (common on emulators with no injected location), which would otherwise hang forever.
     */
    suspend fun getCurrentLocation(): Location =
        withTimeoutOrNull(LOCATION_TIMEOUT_MS) { awaitLocationFix() } ?: throw LocationUnavailableException()

    @SuppressLint("MissingPermission")
    private suspend fun awaitLocationFix(): Location = suspendCancellableCoroutine { continuation ->
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val provider = when {
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            else -> null
        }

        if (provider == null) {
            continuation.resumeWithException(LocationUnavailableException())
            return@suspendCancellableCoroutine
        }

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                locationManager.removeUpdates(this)
                if (continuation.isActive) continuation.resume(location)
            }
        }

        continuation.invokeOnCancellation { locationManager.removeUpdates(listener) }
        locationManager.requestLocationUpdates(provider, 0L, 0f, listener, Looper.getMainLooper())
    }
}
