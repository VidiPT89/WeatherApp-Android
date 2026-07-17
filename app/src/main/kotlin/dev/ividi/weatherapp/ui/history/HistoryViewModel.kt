package dev.ividi.weatherapp.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ividi.weatherapp.data.model.HistoryEntry
import dev.ividi.weatherapp.data.network.ApiException
import dev.ividi.weatherapp.data.repository.HistoryRepository
import dev.ividi.weatherapp.ui.common.UiState
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val GENERIC_ERROR_MESSAGE = "Não foi possível carregar o histórico."

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
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
                UiState.Error(error.message ?: GENERIC_ERROR_MESSAGE)
            }
        }
    }
}
