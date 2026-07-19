package dev.ividi.weatherapp.ui.compare

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ividi.weatherapp.data.model.CompareResponse
import dev.ividi.weatherapp.data.model.GeocodingResult
import dev.ividi.weatherapp.data.network.ApiException
import dev.ividi.weatherapp.data.repository.CompareRepository
import dev.ividi.weatherapp.data.repository.GeocodingRepository
import dev.ividi.weatherapp.ui.common.UiState
import dev.ividi.weatherapp.util.ErrorMessageProvider
import dev.ividi.weatherapp.util.citySuggestionsFlow
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class CompareViewModel @Inject constructor(
    private val compareRepository: CompareRepository,
    private val geocodingRepository: GeocodingRepository,
    private val errorMessageProvider: ErrorMessageProvider,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _suggestions = MutableStateFlow<List<GeocodingResult>>(emptyList())
    val suggestions: StateFlow<List<GeocodingResult>> = _suggestions.asStateFlow()

    private val _compareState = MutableStateFlow<UiState<CompareResponse>>(UiState.Empty)
    val compareState: StateFlow<UiState<CompareResponse>> = _compareState.asStateFlow()

    init {
        viewModelScope.launch {
            citySuggestionsFlow(_searchQuery) { geocodingRepository.searchCities(it) }
                .collect { _suggestions.value = it }
        }
    }

    fun onQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onSuggestionSelected(result: GeocodingResult) {
        _suggestions.value = emptyList()
        compare(result.name)
    }

    fun onSearchSubmit(city: String) {
        if (city.isBlank()) return
        _suggestions.value = emptyList()
        compare(city)
    }

    private fun compare(city: String) {
        _searchQuery.value = city
        _compareState.value = UiState.Loading
        viewModelScope.launch {
            _compareState.value = try {
                UiState.Success(compareRepository.compareProviders(city = city, units = null))
            } catch (error: ApiException) {
                UiState.Error(errorMessageProvider.messageFor(error))
            }
        }
    }
}
