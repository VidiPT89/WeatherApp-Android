package dev.ividi.weatherapp.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ividi.weatherapp.R
import dev.ividi.weatherapp.data.model.FavoriteEntry
import dev.ividi.weatherapp.data.model.GeocodingResult
import dev.ividi.weatherapp.data.network.ApiException
import dev.ividi.weatherapp.data.network.ErrorCode
import dev.ividi.weatherapp.data.repository.FavoritesRepository
import dev.ividi.weatherapp.data.repository.GeocodingRepository
import dev.ividi.weatherapp.ui.common.UiState
import dev.ividi.weatherapp.util.ErrorMessageResolver
import dev.ividi.weatherapp.util.StringResolver
import dev.ividi.weatherapp.util.citySuggestionsFlow
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val HTTP_CONFLICT = 409

/**
 * The "add favorite" field used to accept arbitrary free text, which the backend stored with no
 * validation against real geocoded places -- a typo or an unresolvable name could get "added"
 * successfully and only fail later when loading its weather. It now reuses the same debounced
 * autocomplete pipeline as [dev.ividi.weatherapp.ui.dashboard.DashboardViewModel]'s search box
 * ([dev.ividi.weatherapp.util.citySuggestionsFlow]) so [addFavorite] only ever receives a name
 * that came from a real `/geocoding` suggestion.
 */
@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoritesRepository: FavoritesRepository,
    private val geocodingRepository: GeocodingRepository,
    private val errorMessageProvider: ErrorMessageResolver,
    private val stringProvider: StringResolver,
) : ViewModel() {

    private val _favoritesState = MutableStateFlow<UiState<List<FavoriteEntry>>>(UiState.Loading)
    val favoritesState: StateFlow<UiState<List<FavoriteEntry>>> = _favoritesState.asStateFlow()

    private val _addFavoriteMessage = MutableStateFlow<String?>(null)
    val addFavoriteMessage: StateFlow<String?> = _addFavoriteMessage.asStateFlow()

    private val _removeFavoriteError = MutableStateFlow<String?>(null)
    val removeFavoriteError: StateFlow<String?> = _removeFavoriteError.asStateFlow()

    // Mirrors DashboardViewModel's search-box pipeline (see util/CitySuggestions.kt) so the
    // "add favorite" field only ever submits a real geocoded place, not arbitrary free text.
    private val _newCityQuery = MutableStateFlow("")
    val newCityQuery: StateFlow<String> = _newCityQuery.asStateFlow()

    private val _suggestions = MutableStateFlow<List<GeocodingResult>>(emptyList())
    val suggestions: StateFlow<List<GeocodingResult>> = _suggestions.asStateFlow()

    init {
        loadFavorites()
        viewModelScope.launch {
            citySuggestionsFlow(_newCityQuery) { geocodingRepository.searchCities(it) }
                .collect { _suggestions.value = it }
        }
    }

    fun onNewCityQueryChange(query: String) {
        _newCityQuery.value = query
    }

    fun onSuggestionSelected(result: GeocodingResult) {
        _suggestions.value = emptyList()
        _newCityQuery.value = ""
        addFavorite(result.name)
    }

    /**
     * Handles the keyboard "search" action / IME submit. There's no free-text add anymore (see
     * class doc) -- if a suggestion is already showing, submitting picks the top one as a
     * convenience for keyboard-only users; otherwise this is a no-op, same as Dashboard's search
     * box before any suggestion has loaded.
     */
    fun onNewCitySubmit() {
        _suggestions.value.firstOrNull()?.let(::onSuggestionSelected)
    }

    fun loadFavorites() {
        _favoritesState.value = UiState.Loading
        viewModelScope.launch {
            _favoritesState.value = try {
                val favorites = favoritesRepository.getFavorites()
                if (favorites.isEmpty()) UiState.Empty else UiState.Success(favorites)
            } catch (error: ApiException) {
                UiState.Error(errorMessageProvider.messageFor(error))
            }
        }
    }

    fun addFavorite(city: String) {
        if (city.isBlank()) return
        viewModelScope.launch {
            try {
                favoritesRepository.addFavorite(city.trim())
                _addFavoriteMessage.value = stringProvider.get(R.string.favorites_added_message, city)
                loadFavorites()
            } catch (error: ApiException.HttpError) {
                _addFavoriteMessage.value = if (error.statusCode == HTTP_CONFLICT) {
                    "\"$city\"" + stringProvider.get(R.string.favorites_duplicate_suffix)
                } else {
                    errorMessageProvider.messageFor(error)
                }
            } catch (error: ApiException) {
                _addFavoriteMessage.value = errorMessageProvider.messageFor(error)
            }
        }
    }

    fun consumeAddFavoriteMessage() {
        _addFavoriteMessage.value = null
    }

    fun removeFavorite(city: String) {
        viewModelScope.launch {
            try {
                favoritesRepository.removeFavorite(city)
                loadFavorites()
            } catch (error: ApiException.HttpError) {
                if (error.errorCode == ErrorCode.FAVORITE_NOT_FOUND.wireValue) {
                    // Already removed elsewhere (e.g. another device/race) -- from the user's
                    // perspective the delete succeeded, so just refresh instead of showing an error.
                    loadFavorites()
                } else {
                    _removeFavoriteError.value = errorMessageProvider.messageFor(error)
                }
            } catch (error: ApiException) {
                _removeFavoriteError.value = errorMessageProvider.messageFor(error)
            }
        }
    }

    fun consumeRemoveFavoriteError() {
        _removeFavoriteError.value = null
    }
}
