package dev.ividi.weatherapp.ui.dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ividi.weatherapp.data.model.ForecastResponse
import dev.ividi.weatherapp.data.model.GeocodingResult
import dev.ividi.weatherapp.data.model.Units
import dev.ividi.weatherapp.data.model.WeatherResponse
import dev.ividi.weatherapp.data.network.ApiException
import dev.ividi.weatherapp.data.repository.GeocodingRepository
import dev.ividi.weatherapp.data.repository.PreferencesRepository
import dev.ividi.weatherapp.data.repository.WeatherRepository
import dev.ividi.weatherapp.ui.common.UiState
import dev.ividi.weatherapp.ui.navigation.Screen
import dev.ividi.weatherapp.util.citySuggestionsFlow
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val GENERIC_ERROR_MESSAGE = "Não foi possível obter os dados. Tente novamente."

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val geocodingRepository: GeocodingRepository,
    private val preferencesRepository: PreferencesRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _suggestions = MutableStateFlow<List<GeocodingResult>>(emptyList())
    val suggestions: StateFlow<List<GeocodingResult>> = _suggestions.asStateFlow()

    private val _units = MutableStateFlow(Units.METRIC)
    val units: StateFlow<Units> = _units.asStateFlow()

    private val _weatherState = MutableStateFlow<UiState<WeatherResponse>>(UiState.Empty)
    val weatherState: StateFlow<UiState<WeatherResponse>> = _weatherState.asStateFlow()

    private val _forecastState = MutableStateFlow<UiState<ForecastResponse>>(UiState.Empty)
    val forecastState: StateFlow<UiState<ForecastResponse>> = _forecastState.asStateFlow()

    private val _selectedTab = MutableStateFlow(ForecastTab.HOURLY)
    val selectedTab: StateFlow<ForecastTab> = _selectedTab.asStateFlow()

    private var currentCity: String? = null

    init {
        viewModelScope.launch {
            citySuggestionsFlow(_searchQuery) { geocodingRepository.searchCities(it) }
                .collect { _suggestions.value = it }
        }
        viewModelScope.launch {
            _units.value = runCatching { preferencesRepository.getPreferredUnits() }
                .getOrDefault(Units.METRIC)
        }

        val preloadedCity = savedStateHandle.get<String>(Screen.Dashboard.CITY_ARG)
        if (!preloadedCity.isNullOrBlank()) {
            onCitySelected(preloadedCity)
        }
    }

    fun onQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onSuggestionSelected(result: GeocodingResult) {
        _suggestions.value = emptyList()
        onCitySelected(result.name)
    }

    fun onSearchSubmit(city: String) {
        if (city.isBlank()) return
        _suggestions.value = emptyList()
        onCitySelected(city)
    }

    fun onCitySelected(city: String) {
        _searchQuery.value = city
        loadWeather(city)
    }

    fun onTabSelected(tab: ForecastTab) {
        _selectedTab.value = tab
    }

    /** Toggles metric/imperial, re-fetches the current city, and fire-and-forget saves the pref. */
    fun onUnitsToggled() {
        val newUnits = _units.value.toggled()
        _units.value = newUnits

        viewModelScope.launch {
            runCatching { preferencesRepository.updatePreferredUnits(newUnits) }
        }

        currentCity?.let { loadWeather(it) }
    }

    private fun loadWeather(city: String) {
        currentCity = city
        _weatherState.value = UiState.Loading
        _forecastState.value = UiState.Loading

        viewModelScope.launch {
            val unitsToUse = _units.value
            val weatherDeferred = async { weatherRepository.getWeather(city, unitsToUse) }
            val forecastDeferred = async { weatherRepository.getForecast(city, unitsToUse) }

            _weatherState.value = try {
                UiState.Success(weatherDeferred.await())
            } catch (error: ApiException) {
                UiState.Error(error.message ?: GENERIC_ERROR_MESSAGE)
            }

            _forecastState.value = try {
                UiState.Success(forecastDeferred.await())
            } catch (error: ApiException) {
                UiState.Error(error.message ?: GENERIC_ERROR_MESSAGE)
            }
        }
    }
}
