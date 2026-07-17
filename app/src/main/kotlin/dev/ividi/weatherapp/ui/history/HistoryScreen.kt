package dev.ividi.weatherapp.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.ividi.weatherapp.data.model.HistoryEntry
import dev.ividi.weatherapp.ui.common.UiState
import dev.ividi.weatherapp.util.toDisplayDateTime

@Composable
fun HistoryScreen(viewModel: HistoryViewModel = hiltViewModel()) {
    val historyState by viewModel.historyState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(text = "Histórico de pesquisas", style = MaterialTheme.typography.titleLarge)

        when (val state = historyState) {
            is UiState.Loading -> Box(
                Modifier.fillMaxWidth().padding(24.dp),
                Alignment.Center,
            ) { CircularProgressIndicator() }
            is UiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
            is UiState.Empty -> Text("Ainda não pesquisou nenhuma cidade.")
            is UiState.Success -> {
                LazyColumn(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.data) { entry -> HistoryRow(entry) }
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(entry: HistoryEntry) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(text = entry.city, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = entry.searchedAt.toDisplayDateTime(),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            Text(text = entry.units.temperatureSymbol, style = MaterialTheme.typography.labelLarge)
        }
    }
}
