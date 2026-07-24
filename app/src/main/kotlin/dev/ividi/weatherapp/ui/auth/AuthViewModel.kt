package dev.ividi.weatherapp.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ividi.weatherapp.R
import dev.ividi.weatherapp.data.auth.AuthRepository
import dev.ividi.weatherapp.data.network.ApiException
import dev.ividi.weatherapp.ui.common.UiState
import dev.ividi.weatherapp.util.ErrorMessageProvider
import dev.ividi.weatherapp.util.StringProvider
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val MIN_PASSWORD_LENGTH = 8

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val errorMessageProvider: ErrorMessageProvider,
    private val stringProvider: StringProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Empty)
    val uiState: StateFlow<UiState<Unit>> = _uiState.asStateFlow()

    val isLoggedIn: StateFlow<Boolean> = authRepository.tokenFlow
        .map { it != null }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = authRepository.isLoggedInNow,
        )

    fun register(email: String, password: String) {
        validateCredentials(email, password)?.let { validationError ->
            _uiState.value = UiState.Error(validationError)
            return
        }
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                authRepository.register(email.trim(), password)
                _uiState.value = UiState.Success(Unit)
            } catch (error: ApiException) {
                _uiState.value = UiState.Error(errorMessageProvider.messageFor(error))
            }
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = UiState.Error(stringProvider.get(R.string.auth_missing_credentials))
            return
        }
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                authRepository.login(email.trim(), password)
                _uiState.value = UiState.Success(Unit)
            } catch (error: ApiException) {
                _uiState.value = UiState.Error(errorMessageProvider.messageFor(error))
            }
        }
    }

    fun resetState() {
        _uiState.value = UiState.Empty
    }

    private fun validateCredentials(email: String, password: String): String? = when {
        email.isBlank() -> stringProvider.get(R.string.auth_missing_email)
        password.length < MIN_PASSWORD_LENGTH ->
            stringProvider.get(R.string.auth_password_too_short, MIN_PASSWORD_LENGTH)
        else -> null
    }
}
