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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.ividi.weatherapp.R
import dev.ividi.weatherapp.data.model.HistoryEntry
import dev.ividi.weatherapp.ui.common.UiState
import dev.ividi.weatherapp.util.toDisplayDateTime

@Composable
fun HistoryScreen(viewModel: HistoryViewModel = hiltViewModel()) {
    val historyState by viewModel.historyState.collectAsStateWithLifecycle()
    val deleteError by viewModel.deleteError.collectAsStateWithLifecycle()
    var showClearAllConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = stringResource(R.string.history_title), style = MaterialTheme.typography.titleLarge)
            if (historyState is UiState.Success) {
                TextButton(onClick = { showClearAllConfirm = true }) {
                    Text(stringResource(R.string.history_clear_all_action))
                }
            }
        }

        deleteError?.let { message ->
            Text(text = message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            LaunchedEffect(message) {
                kotlinx.coroutines.delay(3_000)
                viewModel.consumeDeleteError()
            }
        }

        when (val state = historyState) {
            is UiState.Loading -> Box(
                Modifier.fillMaxWidth().padding(24.dp),
                Alignment.Center,
            ) { CircularProgressIndicator() }
            is UiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
            is UiState.Empty -> Text(stringResource(R.string.history_empty))
            is UiState.Success -> {
                LazyColumn(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.data, key = { it.id }) { entry ->
                        HistoryRow(entry, onRemove = { viewModel.deleteEntry(entry.id) })
                    }
                }
            }
        }
    }

    if (showClearAllConfirm) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirm = false },
            title = { Text(stringResource(R.string.history_clear_confirm_title)) },
            text = { Text(stringResource(R.string.history_clear_confirm_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearHistory()
                    showClearAllConfirm = false
                }) {
                    Text(stringResource(R.string.history_clear_confirm_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllConfirm = false }) {
                    Text(stringResource(R.string.history_clear_confirm_cancel))
                }
            },
        )
    }
}

@Composable
private fun HistoryRow(entry: HistoryEntry, onRemove: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = entry.city, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = entry.searchedAt.toDisplayDateTime(),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            Text(text = entry.units.temperatureSymbol, style = MaterialTheme.typography.labelLarge)
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.history_remove_action, entry.city),
                )
            }
        }
    }
}
