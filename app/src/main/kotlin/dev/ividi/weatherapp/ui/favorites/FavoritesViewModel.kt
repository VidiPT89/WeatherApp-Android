package dev.ividi.weatherapp.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ividi.weatherapp.R
import dev.ividi.weatherapp.data.model.FavoriteEntry
import dev.ividi.weatherapp.data.network.ApiException
import dev.ividi.weatherapp.data.repository.FavoritesRepository
import dev.ividi.weatherapp.ui.common.UiState
import dev.ividi.weatherapp.util.ErrorMessageProvider
import dev.ividi.weatherapp.util.StringProvider
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val HTTP_CONFLICT = 409

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoritesRepository: FavoritesRepository,
    private val errorMessageProvider: ErrorMessageProvider,
    private val stringProvider: StringProvider,
) : ViewModel() {

    private val _favoritesState = MutableStateFlow<UiState<List<FavoriteEntry>>>(UiState.Loading)
    val favoritesState: StateFlow<UiState<List<FavoriteEntry>>> = _favoritesState.asStateFlow()

    private val _addFavoriteMessage = MutableStateFlow<String?>(null)
    val addFavoriteMessage: StateFlow<String?> = _addFavoriteMessage.asStateFlow()

    init {
        loadFavorites()
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
}
