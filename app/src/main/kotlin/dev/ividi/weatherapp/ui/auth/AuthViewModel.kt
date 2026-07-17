package dev.ividi.weatherapp.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ividi.weatherapp.data.auth.AuthRepository
import dev.ividi.weatherapp.data.network.ApiException
import dev.ividi.weatherapp.ui.common.UiState
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
                _uiState.value = UiState.Error(error.message ?: "Falha no registo.")
            }
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = UiState.Error("Preencha o email e a palavra-passe.")
            return
        }
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                authRepository.login(email.trim(), password)
                _uiState.value = UiState.Success(Unit)
            } catch (error: ApiException) {
                _uiState.value = UiState.Error(error.message ?: "Falha no login.")
            }
        }
    }

    fun resetState() {
        _uiState.value = UiState.Empty
    }

    private fun validateCredentials(email: String, password: String): String? = when {
        email.isBlank() -> "Indique um email."
        password.length < MIN_PASSWORD_LENGTH -> "A palavra-passe deve ter pelo menos $MIN_PASSWORD_LENGTH caracteres."
        else -> null
    }
}
