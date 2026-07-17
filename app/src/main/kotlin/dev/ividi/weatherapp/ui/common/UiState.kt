package dev.ividi.weatherapp.ui.common

/** Generic screen-data state: every screen needs these four shapes, nothing more. */
sealed interface UiState<out T> {
    data object Empty : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}
