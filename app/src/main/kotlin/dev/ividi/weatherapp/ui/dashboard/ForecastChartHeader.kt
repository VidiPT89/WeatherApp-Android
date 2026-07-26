package dev.ividi.weatherapp.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import dev.ividi.weatherapp.R

/**
 * Shared header row for the hourly/daily forecast charts: shows the visible
 * date/time range as a caption and a pair of paging chevrons that jump one
 * full window forward/backward. Discrete taps rather than requiring the
 * reader to drag-scroll to reach later days/hours.
 */
@Composable
fun ForecastChartHeader(
    rangeLabel: String,
    canPageBackward: Boolean,
    canPageForward: Boolean,
    onPageBackward: () -> Unit,
    onPageForward: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = rangeLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row {
            IconButton(
                onClick = onPageBackward,
                enabled = canPageBackward,
                modifier = Modifier.semantics { testTag = "forecast.chart.pageBackward" },
            ) {
                Icon(
                    imageVector = Icons.Filled.ChevronLeft,
                    contentDescription = stringResource(R.string.forecast_page_previous),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            IconButton(
                onClick = onPageForward,
                enabled = canPageForward,
                modifier = Modifier.semantics { testTag = "forecast.chart.pageForward" },
            ) {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = stringResource(R.string.forecast_page_next),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
