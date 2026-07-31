package dev.ividi.weatherapp.util

import android.content.Context
import androidx.annotation.StringRes
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Narrow interface extracted purely for unit-testability (mirroring
 * [dev.ividi.weatherapp.util.ErrorMessageResolver]), so ViewModel tests can supply a fixed,
 * recognizable string instead of resolving a real Android string resource.
 */
interface StringResolver {
    fun get(@StringRes resId: Int, vararg formatArgs: Any): String
}

/**
 * Lets ViewModels build localized, string-resource-backed copy for messages that are computed
 * outside of a `@Composable` (e.g. a transient "added to favorites" toast), without hand-rolling
 * per-locale text in each ViewModel.
 */
@Singleton
class StringProvider @Inject constructor(@ApplicationContext private val context: Context) : StringResolver {
    override fun get(@StringRes resId: Int, vararg formatArgs: Any): String = context.getString(resId, *formatArgs)
}
