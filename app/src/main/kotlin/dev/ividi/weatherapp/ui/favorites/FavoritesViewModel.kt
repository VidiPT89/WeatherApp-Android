package dev.ividi.weatherapp.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ividi.weatherapp.data.model.FavoriteEntry
import dev.ividi.weatherapp.data.network.ApiException
import dev.ividi.weatherapp.data.repository.FavoritesRepository
import dev.ividi.weatherapp.ui.common.UiState
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val GENERIC_ERROR_MESSAGE = "Não foi possível carregar os favoritos."
private const val DUPLICATE_MESSAGE_SUFFIX = "já é um favorito."

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoritesRepository: FavoritesRepository,
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
                UiState.Error(error.message ?: GENERIC_ERROR_MESSAGE)
            }
        }
    }

    fun addFavorite(city: String) {
        if (city.isBlank()) return
        viewModelScope.launch {
            try {
                favoritesRepository.addFavorite(city.trim())
                _addFavoriteMessage.value = "\"$city\" adicionada aos favoritos."
                loadFavorites()
            } catch (error: ApiException.HttpError) {
                _addFavoriteMessage.value = if (error.statusCode == 409) {
                    "\"$city\" $DUPLICATE_MESSAGE_SUFFIX"
                } else {
                    error.message
                }
            } catch (error: ApiException) {
                _addFavoriteMessage.value = error.message
            }
        }
    }

    fun consumeAddFavoriteMessage() {
        _addFavoriteMessage.value = null
    }
}
