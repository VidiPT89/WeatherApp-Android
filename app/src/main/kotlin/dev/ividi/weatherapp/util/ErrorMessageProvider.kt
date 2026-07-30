package dev.ividi.weatherapp.util

import android.content.Context
import androidx.annotation.StringRes
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.ividi.weatherapp.R
import dev.ividi.weatherapp.data.network.ApiException
import dev.ividi.weatherapp.data.network.ErrorCode
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Translates a backend [ApiException] into a localized, user-facing string.
 *
 * The backend's raw `message` field is English-only and sometimes carries dynamic/untranslatable
 * content (e.g. validation details), so it must never be shown directly. Instead, every
 * [ApiException.HttpError] is expected to carry a stable `errorCode` (e.g. "CITY_NOT_FOUND") that
 * is mapped here to a localized string resource; unknown/missing codes and non-HTTP failures
 * (network, serialization) fall back to a generic localized message.
 */
@Singleton
class ErrorMessageProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) : ErrorMessageResolver {

    override fun messageFor(error: ApiException, @StringRes fallbackRes: Int): String {
        val errorCode = (error as? ApiException.HttpError)?.errorCode?.let(ErrorCode::fromWireValue)
        val stringRes = errorCode?.let(::stringResFor) ?: fallbackRes
        return context.getString(stringRes)
    }

    @StringRes
    private fun stringResFor(errorCode: ErrorCode): Int = when (errorCode) {
        ErrorCode.CITY_NOT_FOUND -> R.string.error_city_not_found
        ErrorCode.PROVIDER_UNAVAILABLE -> R.string.error_provider_unavailable
        ErrorCode.PROVIDER_QUOTA_EXCEEDED -> R.string.error_provider_quota_exceeded
        ErrorCode.VALIDATION_FAILED -> R.string.error_validation_failed
        ErrorCode.EMAIL_ALREADY_REGISTERED -> R.string.error_email_already_registered
        ErrorCode.INVALID_CREDENTIALS -> R.string.error_invalid_credentials
        ErrorCode.INVALID_REFRESH_TOKEN -> R.string.error_invalid_refresh_token
        ErrorCode.FAVORITE_ALREADY_EXISTS -> R.string.error_favorite_already_exists
        ErrorCode.FAVORITE_NOT_FOUND -> R.string.error_favorite_not_found
        ErrorCode.SEARCH_HISTORY_ENTRY_NOT_FOUND -> R.string.error_search_history_entry_not_found
        ErrorCode.USER_NOT_FOUND -> R.string.error_user_not_found
        ErrorCode.CONFLICT -> R.string.error_conflict
        ErrorCode.UNAUTHENTICATED -> R.string.error_unauthenticated
        ErrorCode.ACCESS_DENIED -> R.string.error_access_denied
        ErrorCode.RATE_LIMIT_EXCEEDED -> R.string.error_rate_limit_exceeded
        ErrorCode.INTERNAL_ERROR -> R.string.error_internal_error
    }
}
