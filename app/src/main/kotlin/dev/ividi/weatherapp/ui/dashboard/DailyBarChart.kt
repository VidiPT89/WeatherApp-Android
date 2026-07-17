package dev.ividi.weatherapp.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import dev.ividi.weatherapp.data.model.DailyForecastEntry
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

private const val CHART_HEIGHT_DP = 200
private const val HORIZONTAL_PADDING_DP = 24
private const val VERTICAL_PADDING_DP = 16
private const val BAR_WIDTH_FRACTION = 0.4f

/**
 * Simple hand-rolled Canvas bar chart showing min+max temperature per day (3 entries),
 * mirroring [HourlyLineChart]'s "no third-party charting dependency" approach.
 */
@Composable
fun DailyBarChart(entries: List<DailyForecastEntry>, modifier: Modifier = Modifier) {
    if (entries.isEmpty()) return

    val maxBarColor = MaterialTheme.colorScheme.primary
    val minBarColor = MaterialTheme.colorScheme.secondary

    val overallMin = entries.minOf { it.temperatureMin }
    val overallMax = entries.maxOf { it.temperatureMax }
    val tempRange = (overallMax - overallMin).takeIf { it > 0.0 } ?: 1.0

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(CHART_HEIGHT_DP.dp),
        ) {
            val horizontalPadding = HORIZONTAL_PADDING_DP.dp.toPx()
            val verticalPadding = VERTICAL_PADDING_DP.dp.toPx()
            val chartWidth = size.width - horizontalPadding * 2
            val chartHeight = size.height - verticalPadding * 2
            val slotWidth = chartWidth / entries.size
            val barWidth = slotWidth * BAR_WIDTH_FRACTION

            entries.forEachIndexed { index, entry ->
                val slotCenterX = horizontalPadding + slotWidth * index + slotWidth / 2

                val topNormalized = ((entry.temperatureMax - overallMin) / tempRange).toFloat()
                val bottomNormalized = ((entry.temperatureMin - overallMin) / tempRange).toFloat()

                val topY = verticalPadding + chartHeight * (1f - topNormalized)
                val bottomY = verticalPadding + chartHeight * (1f - bottomNormalized)

                drawRect(
                    color = maxBarColor,
                    topLeft = Offset(slotCenterX - barWidth / 2, topY),
                    size = Size(barWidth, (bottomY - topY).coerceAtLeast(4f)),
                )
                drawCircle(
                    color = minBarColor,
                    radius = 4.dp.toPx(),
                    center = Offset(slotCenterX, bottomY),
                )
            }
        }

        DayLabelsRow(entries)
    }
}

@Composable
private fun DayLabelsRow(entries: List<DailyForecastEntry>) {
    androidx.compose.foundation.layout.Row(modifier = Modifier.fillMaxWidth()) {
        entries.forEach { entry ->
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.weight(1f),
                contentAlignment = androidx.compose.ui.Alignment.Center,
            ) {
                Text(text = dayLabel(entry.date), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

private fun dayLabel(date: LocalDate): String = when (date.dayOfWeek) {
    DayOfWeek.MONDAY -> "Seg"
    DayOfWeek.TUESDAY -> "Ter"
    DayOfWeek.WEDNESDAY -> "Qua"
    DayOfWeek.THURSDAY -> "Qui"
    DayOfWeek.FRIDAY -> "Sex"
    DayOfWeek.SATURDAY -> "Sáb"
    DayOfWeek.SUNDAY -> "Dom"
    else -> ""
}
