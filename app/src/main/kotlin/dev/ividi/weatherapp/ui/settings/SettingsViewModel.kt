package dev.ividi.weatherapp.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ividi.weatherapp.data.auth.AuthRepository
import dev.ividi.weatherapp.data.model.Units
import dev.ividi.weatherapp.data.network.ApiException
import dev.ividi.weatherapp.data.repository.AppLanguage
import dev.ividi.weatherapp.data.repository.AppPreferencesRepository
import dev.ividi.weatherapp.data.repository.PreferencesRepository
import dev.ividi.weatherapp.data.repository.ThemeMode
import dev.ividi.weatherapp.ui.common.UiState
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val GENERIC_ERROR_MESSAGE = "Não foi possível carregar as preferências."

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val authRepository: AuthRepository,
    private val appPreferencesRepository: AppPreferencesRepository,
) : ViewModel() {

    private val _preferencesState = MutableStateFlow<UiState<Units>>(UiState.Loading)
    val preferencesState: StateFlow<UiState<Units>> = _preferencesState.asStateFlow()

    val themeMode: StateFlow<ThemeMode> = appPreferencesRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.SYSTEM)

    val language: StateFlow<AppLanguage> = appPreferencesRepository.language
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppLanguage.PT)

    init {
        loadPreferences()
    }

    fun updateThemeMode(mode: ThemeMode) {
        viewModelScope.launch { appPreferencesRepository.setThemeMode(mode) }
    }

    fun updateLanguage(language: AppLanguage) {
        viewModelScope.launch { appPreferencesRepository.setLanguage(language) }
    }

    fun loadPreferences() {
        _preferencesState.value = UiState.Loading
        viewModelScope.launch {
            _preferencesState.value = try {
                UiState.Success(preferencesRepository.getPreferredUnits())
            } catch (error: ApiException) {
                UiState.Error(error.message ?: GENERIC_ERROR_MESSAGE)
            }
        }
    }

    fun updateUnits(units: Units) {
        val previousState = _preferencesState.value
        _preferencesState.value = UiState.Success(units)
        viewModelScope.launch {
            try {
                preferencesRepository.updatePreferredUnits(units)
            } catch (error: ApiException) {
                _preferencesState.value = previousState
            }
        }
    }

    fun logout() {
        authRepository.logout()
    }
}
