package dev.ividi.weatherapp.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ividi.weatherapp.data.auth.AuthRepository
import dev.ividi.weatherapp.data.model.Units
import dev.ividi.weatherapp.data.network.ApiException
import dev.ividi.weatherapp.data.repository.PreferencesRepository
import dev.ividi.weatherapp.ui.common.UiState
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val GENERIC_ERROR_MESSAGE = "Não foi possível carregar as preferências."

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _preferencesState = MutableStateFlow<UiState<Units>>(UiState.Loading)
    val preferencesState: StateFlow<UiState<Units>> = _preferencesState.asStateFlow()

    init {
        loadPreferences()
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
