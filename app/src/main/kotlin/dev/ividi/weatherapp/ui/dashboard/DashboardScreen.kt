package dev.ividi.weatherapp.ui.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.ividi.weatherapp.R
import dev.ividi.weatherapp.data.model.Units
import dev.ividi.weatherapp.ui.common.ChartSkeleton
import dev.ividi.weatherapp.ui.common.FallbackBanner
import dev.ividi.weatherapp.ui.common.SearchAutocompleteField
import dev.ividi.weatherapp.ui.common.UiState
import dev.ividi.weatherapp.ui.common.WeatherCardSkeleton
import dev.ividi.weatherapp.util.isFallbackProvider

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val suggestions by viewModel.suggestions.collectAsStateWithLifecycle()
    val units by viewModel.units.collectAsStateWithLifecycle()
    val weatherState by viewModel.weatherState.collectAsStateWithLifecycle()
    val forecastState by viewModel.forecastState.collectAsStateWithLifecycle()
    val marineState by viewModel.marineState.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SearchAutocompleteField(
                query = searchQuery,
                onQueryChange = viewModel::onQueryChange,
                suggestions = suggestions,
                onSuggestionSelected = viewModel::onSuggestionSelected,
                onSearchSubmit = viewModel::onSearchSubmit,
                placeholder = stringResource(R.string.search_placeholder),
            )
        }

        item {
            UnitsToggleRow(units = units, onToggle = viewModel::onUnitsToggled)
        }

        item {
            AnimatedContent(
                targetState = weatherState,
                label = "weatherState",
                transitionSpec = { fadeIn() togetherWith fadeOut() },
            ) { state ->
                when (state) {
                    is UiState.Empty -> EmptyStateMessage(stringResource(R.string.dashboard_idle_prompt))
                    is UiState.Loading -> WeatherCardSkeleton()
                    is UiState.Error -> ErrorStateMessage(state.message)
                    is UiState.Success -> {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (isFallbackProvider(state.data.provider)) {
                                FallbackBanner(provider = state.data.provider)
                            }
                            val todayForecast = (forecastState as? UiState.Success)?.data?.daily?.firstOrNull()
                            CurrentWeatherCard(weather = state.data, todayForecast = todayForecast)
                        }
                    }
                }
            }
        }

        item {
            when (val state = forecastState) {
                is UiState.Success -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(text = stringResource(R.string.forecast_title), style = MaterialTheme.typography.titleMedium)
                        ForecastTabRow(selectedTab = selectedTab, onTabSelected = viewModel::onTabSelected)
                        AnimatedContent(
                            targetState = selectedTab,
                            label = "forecastTab",
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                        ) { tab ->
                            when (tab) {
                                ForecastTab.HOURLY -> HourlyLineChart(entries = state.data.hourly)
                                ForecastTab.DAILY -> DailyBarChart(entries = state.data.daily)
                            }
                        }
                    }
                }
                is UiState.Loading -> ChartSkeleton()
                is UiState.Error -> ErrorStateMessage(state.message)
                is UiState.Empty -> Unit
            }
        }

        item {
            AnimatedVisibility(visible = marineState is UiState.Success) {
                (marineState as? UiState.Success)?.let { state ->
                    SeaConditionsCard(marine = state.data)
                }
            }
        }
    }
}

@Composable
private fun UnitsToggleRow(units: Units, onToggle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.units_label, units.temperatureSymbol),
            style = MaterialTheme.typography.bodyLarge,
        )
        Switch(checked = units == Units.IMPERIAL, onCheckedChange = { onToggle() })
    }
}

@Composable
private fun ForecastTabRow(selectedTab: ForecastTab, onTabSelected: (ForecastTab) -> Unit) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        ForecastTab.entries.forEachIndexed { index, tab ->
            SegmentedButton(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = ForecastTab.entries.size),
            ) {
                Text(forecastTabLabel(tab))
            }
        }
    }
}

@Composable
private fun forecastTabLabel(tab: ForecastTab): String = when (tab) {
    ForecastTab.HOURLY -> stringResource(R.string.forecast_hourly_tab)
    ForecastTab.DAILY -> stringResource(R.string.forecast_daily_tab)
}

@Composable
private fun ErrorStateMessage(message: String) {
    Text(
        text = message,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.fillMaxWidth().padding(16.dp),
    )
}

@Composable
private fun EmptyStateMessage(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.fillMaxWidth().padding(16.dp),
    )
}
