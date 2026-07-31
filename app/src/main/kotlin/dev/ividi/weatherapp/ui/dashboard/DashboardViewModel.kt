package dev.ividi.weatherapp.ui.dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ividi.weatherapp.data.model.ForecastResponse
import dev.ividi.weatherapp.data.model.GeocodingResult
import dev.ividi.weatherapp.data.model.MarineResponse
import dev.ividi.weatherapp.data.model.Units
import dev.ividi.weatherapp.data.model.WeatherInsightsResponse
import dev.ividi.weatherapp.data.model.WeatherResponse
import dev.ividi.weatherapp.data.network.ApiException
import dev.ividi.weatherapp.data.repository.GeocodingRepository
import dev.ividi.weatherapp.data.repository.InsightsRepository
import dev.ividi.weatherapp.data.repository.MarineRepository
import dev.ividi.weatherapp.data.repository.PreferencesRepository
import dev.ividi.weatherapp.data.repository.WeatherRepository
import dev.ividi.weatherapp.data.repository.WeatherWidgetRepository
import dev.ividi.weatherapp.location.LocationService
import dev.ividi.weatherapp.ui.common.UiState
import dev.ividi.weatherapp.ui.navigation.Screen
import dev.ividi.weatherapp.util.ErrorMessageProvider
import dev.ividi.weatherapp.util.citySuggestionsFlow
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val marineRepository: MarineRepository,
    private val insightsRepository: InsightsRepository,
    private val geocodingRepository: GeocodingRepository,
    private val preferencesRepository: PreferencesRepository,
    private val weatherWidgetRepository: WeatherWidgetRepository,
    private val errorMessageProvider: ErrorMessageProvider,
    private val locationService: LocationService,
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

    private val _marineState = MutableStateFlow<UiState<MarineResponse>>(UiState.Empty)
    val marineState: StateFlow<UiState<MarineResponse>> = _marineState.asStateFlow()

    private val _insightsState = MutableStateFlow<UiState<WeatherInsightsResponse>>(UiState.Empty)
    val insightsState: StateFlow<UiState<WeatherInsightsResponse>> = _insightsState.asStateFlow()

    private val _selectedTab = MutableStateFlow(ForecastTab.HOURLY)
    val selectedTab: StateFlow<ForecastTab> = _selectedTab.asStateFlow()

    /** True only while attempting the initial auto-location lookup. */
    private val _isLocating = MutableStateFlow(false)
    val isLocating: StateFlow<Boolean> = _isLocating.asStateFlow()

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

    /**
     * Auto-detects the user's location and loads its weather. Called once by the screen after
     * location permission is confirmed granted. Fails silently (leaving the normal manual-search
     * empty state) if no provider is enabled or the lookup fails -- this is a convenience, not a
     * required flow.
     */
    fun loadNearbyWeather() {
        if (currentCity != null) return

        viewModelScope.launch {
            _isLocating.value = true
            val weather = runCatching {
                val location = locationService.getCurrentLocation()
                weatherRepository.getWeatherNearby(location.latitude, location.longitude, _units.value)
            }.getOrNull()
            _isLocating.value = false

            // No location provider enabled or the lookup failed -- stay on the empty state.
            weather?.let { onCitySelected(it.city) }
        }
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
        _marineState.value = UiState.Loading
        _insightsState.value = UiState.Loading

        viewModelScope.launch {
            val unitsToUse = _units.value
            // supervisorScope is required here: plain `async` children of the same `launch`
            // propagate a failure to cancel their parent *and* siblings as soon as the child
            // fails -- independent of, and before, any `.await()` call -- so a 401/500 on just
            // one of these four calls would crash the whole app instead of being caught by the
            // per-await try/catch below. A supervisor isolates each child's failure instead.
            supervisorScope {
                val weatherDeferred = async { weatherRepository.getWeather(city, unitsToUse) }
                val forecastDeferred = async { weatherRepository.getForecast(city, unitsToUse) }
                val marineDeferred = async { marineRepository.getMarine(city, unitsToUse) }
                val insightsDeferred = async { insightsRepository.getInsights(city, unitsToUse) }

                _weatherState.value = try {
                    val weather = weatherDeferred.await()
                    // Best-effort: the widget mirroring the last-seen weather is a nice-to-have,
                    // never something that should turn a successful Dashboard load into an error.
                    runCatching { weatherWidgetRepository.saveSnapshot(weather) }
                    UiState.Success(weather)
                } catch (error: ApiException) {
                    UiState.Error(errorMessageProvider.messageFor(error))
                }

                _forecastState.value = try {
                    UiState.Success(forecastDeferred.await())
                } catch (error: ApiException) {
                    UiState.Error(errorMessageProvider.messageFor(error))
                }

                _marineState.value = try {
                    UiState.Success(marineDeferred.await())
                } catch (error: ApiException) {
                    UiState.Error(errorMessageProvider.messageFor(error))
                }

                _insightsState.value = try {
                    UiState.Success(insightsDeferred.await())
                } catch (error: ApiException) {
                    UiState.Error(errorMessageProvider.messageFor(error))
                }
            }
        }
    }
}
