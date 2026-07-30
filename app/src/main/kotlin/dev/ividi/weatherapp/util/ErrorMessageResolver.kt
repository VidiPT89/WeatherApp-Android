package dev.ividi.weatherapp.util

import androidx.annotation.StringRes
import dev.ividi.weatherapp.R
import dev.ividi.weatherapp.data.network.ApiException

/**
 * Minimal contract for [ErrorMessageProvider] -- extracted (mirroring
 * [dev.ividi.weatherapp.data.auth.TokenStore]) so a ViewModel can be unit tested against an
 * in-memory fake instead of the concrete class, which needs a real Android `Context`.
 */
interface ErrorMessageResolver {
    fun messageFor(error: ApiException, @StringRes fallbackRes: Int = R.string.error_generic): String
}
