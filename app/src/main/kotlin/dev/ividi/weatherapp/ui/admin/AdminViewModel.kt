package dev.ividi.weatherapp.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ividi.weatherapp.data.auth.CurrentUserProvider
import dev.ividi.weatherapp.data.model.UserAccount
import dev.ividi.weatherapp.data.network.ApiException
import dev.ividi.weatherapp.data.repository.AdminRepository
import dev.ividi.weatherapp.ui.common.UiState
import dev.ividi.weatherapp.util.ErrorMessageResolver
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    private val currentUserProvider: CurrentUserProvider,
    private val errorMessageProvider: ErrorMessageResolver,
) : ViewModel() {

    private val _usersState = MutableStateFlow<UiState<List<UserAccount>>>(UiState.Loading)
    val usersState: StateFlow<UiState<List<UserAccount>>> = _usersState.asStateFlow()

    private val _deleteError = MutableStateFlow<String?>(null)
    val deleteError: StateFlow<String?> = _deleteError.asStateFlow()

    /** Used to hide the delete action on the caller's own row -- the backend refuses self-delete
     * with a 400, but the row is hidden client-side too rather than relying solely on that. */
    val currentUserId: Long?
        get() = currentUserProvider.currentUser.value?.id

    init {
        loadUsers()
    }

    fun loadUsers() {
        _usersState.value = UiState.Loading
        viewModelScope.launch {
            _usersState.value = try {
                val users = adminRepository.listUsers()
                if (users.isEmpty()) UiState.Empty else UiState.Success(users)
            } catch (error: ApiException) {
                UiState.Error(errorMessageProvider.messageFor(error))
            }
        }
    }

    fun deleteUser(id: Long) {
        viewModelScope.launch {
            try {
                adminRepository.deleteUser(id)
                loadUsers()
            } catch (error: ApiException) {
                _deleteError.value = errorMessageProvider.messageFor(error)
            }
        }
    }

    fun consumeDeleteError() {
        _deleteError.value = null
    }
}
