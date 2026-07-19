package dev.ividi.weatherapp.ui.compare

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.ividi.weatherapp.R
import dev.ividi.weatherapp.data.model.ProviderResult
import dev.ividi.weatherapp.ui.common.SearchAutocompleteField
import dev.ividi.weatherapp.ui.common.UiState
import dev.ividi.weatherapp.util.PRIMARY_PROVIDER
import dev.ividi.weatherapp.util.averageTemperatureAcrossProviders
import kotlin.math.round

@Composable
fun CompareScreen(viewModel: CompareViewModel = hiltViewModel()) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val suggestions by viewModel.suggestions.collectAsStateWithLifecycle()
    val compareState by viewModel.compareState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = stringResource(R.string.compare_title), style = MaterialTheme.typography.titleLarge)

        SearchAutocompleteField(
            query = searchQuery,
            onQueryChange = viewModel::onQueryChange,
            suggestions = suggestions,
            onSuggestionSelected = viewModel::onSuggestionSelected,
            onSearchSubmit = viewModel::onSearchSubmit,
            placeholder = stringResource(R.string.search_placeholder),
        )

        when (val state = compareState) {
            is UiState.Empty -> Text(stringResource(R.string.compare_idle_prompt))
            is UiState.Loading -> Box(Modifier.fillMaxWidth().padding(24.dp), Alignment.Center) {
                CircularProgressIndicator()
            }
            is UiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
            is UiState.Success -> {
                val average = averageTemperatureAcrossProviders(state.data.results)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    state.data.results.forEach { result -> ProviderResultCard(result) }
                    average?.let {
                        Text(
                            text = stringResource(R.string.compare_average, round(it).toInt().toString()),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProviderResultCard(result: ProviderResult) {
    val isPrimary = result.provider == PRIMARY_PROVIDER
    val containerColor = if (result.success) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.errorContainer
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .background(containerColor, RoundedCornerShape(12.dp)),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = result.provider + if (isPrimary) stringResource(R.string.compare_primary_suffix) else "",
                    fontWeight = FontWeight.Bold,
                )
            }

            if (result.success && result.weather != null) {
                Text(
                    text = "${round(result.weather.temperature).toInt()}${result.weather.units.temperatureSymbol}"
                )
                Text(text = result.weather.description.replaceFirstChar { it.uppercase() })
            } else {
                Text(
                    text = result.errorMessage ?: stringResource(R.string.compare_unavailable),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
