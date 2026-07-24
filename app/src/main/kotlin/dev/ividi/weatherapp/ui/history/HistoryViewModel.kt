package dev.ividi.weatherapp.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ividi.weatherapp.data.model.HistoryEntry
import dev.ividi.weatherapp.data.network.ApiException
import dev.ividi.weatherapp.data.repository.HistoryRepository
import dev.ividi.weatherapp.ui.common.UiState
import dev.ividi.weatherapp.util.ErrorMessageProvider
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val errorMessageProvider: ErrorMessageProvider,
) : ViewModel() {

    private val _historyState = MutableStateFlow<UiState<List<HistoryEntry>>>(UiState.Loading)
    val historyState: StateFlow<UiState<List<HistoryEntry>>> = _historyState.asStateFlow()

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
}
