package dev.ividi.weatherapp.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ividi.weatherapp.data.model.HistoryEntry
import dev.ividi.weatherapp.data.network.ApiException
import dev.ividi.weatherapp.data.network.ErrorCode
import dev.ividi.weatherapp.data.repository.HistoryRepository
import dev.ividi.weatherapp.ui.common.UiState
import dev.ividi.weatherapp.util.ErrorMessageResolver
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val errorMessageProvider: ErrorMessageResolver,
) : ViewModel() {

    private val _historyState = MutableStateFlow<UiState<List<HistoryEntry>>>(UiState.Loading)
    val historyState: StateFlow<UiState<List<HistoryEntry>>> = _historyState.asStateFlow()

    private val _deleteError = MutableStateFlow<String?>(null)
    val deleteError: StateFlow<String?> = _deleteError.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        _historyState.value = UiState.Loading
        viewModelScope.launch {
            _historyState.value = try {
                val history = historyRepository.getHistory()
                if (history.isEmpty()) UiState.Empty else UiState.Success(history)
            } catch (error: ApiException) {
                UiState.Error(errorMessageProvider.messageFor(error))
            }
        }
    }

    fun deleteEntry(id: Long) {
        viewModelScope.launch {
            try {
                historyRepository.deleteEntry(id)
                loadHistory()
            } catch (error: ApiException.HttpError) {
                if (error.errorCode == ErrorCode.SEARCH_HISTORY_ENTRY_NOT_FOUND.wireValue) {
                    // Already removed elsewhere (e.g. another device/race) -- from the user's
                    // perspective the delete succeeded, so just refresh instead of showing an error.
                    loadHistory()
                } else {
                    _deleteError.value = errorMessageProvider.messageFor(error)
                }
            } catch (error: ApiException) {
                _deleteError.value = errorMessageProvider.messageFor(error)
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            try {
                historyRepository.clearHistory()
                loadHistory()
            } catch (error: ApiException) {
                _deleteError.value = errorMessageProvider.messageFor(error)
            }
        }
    }

    fun consumeDeleteError() {
        _deleteError.value = null
    }
}
