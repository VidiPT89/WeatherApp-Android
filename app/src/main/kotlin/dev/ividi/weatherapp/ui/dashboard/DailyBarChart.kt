package dev.ividi.weatherapp.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.unit.dp
import dev.ividi.weatherapp.R
import dev.ividi.weatherapp.data.model.DailyForecastEntry

private const val CHART_HEIGHT_DP = 200
private const val VERTICAL_PADDING_DP = 16
private const val DAY_SLOT_WIDTH_DP = 56
private const val BAR_WIDTH_FRACTION = 0.4f

/**
 * Hand-rolled Canvas bar chart showing min+max temperature for every daily entry (up to 16,
 * per the backend's extended forecast). Each day gets a fixed-width slot so the chart can grow
 * past one screen width; it scrolls horizontally rather than truncating or squeezing the data.
 */
@Composable
fun DailyBarChart(entries: List<DailyForecastEntry>, modifier: Modifier = Modifier) {
    if (entries.isEmpty()) return

    val maxBarColor = MaterialTheme.colorScheme.primary
    val minBarColor = MaterialTheme.colorScheme.secondary
    val dayLabels = stringArrayResource(R.array.weekday_labels_short)
    val scrollState = rememberScrollState()

    val overallMin = entries.minOf { it.temperatureMin }
    val overallMax = entries.maxOf { it.temperatureMax }
    val tempRange = (overallMax - overallMin).takeIf { it > 0.0 } ?: 1.0

    val chartWidth = remember(entries.size) { (DAY_SLOT_WIDTH_DP * entries.size).dp }

    Column(modifier = modifier.fillMaxWidth().horizontalScroll(scrollState)) {
        Canvas(
            modifier = Modifier
                .width(chartWidth)
                .height(CHART_HEIGHT_DP.dp),
        ) {
            val verticalPadding = VERTICAL_PADDING_DP.dp.toPx()
            val slotWidth = size.width / entries.size
            val chartHeight = size.height - verticalPadding * 2
            val barWidth = slotWidth * BAR_WIDTH_FRACTION

            entries.forEachIndexed { index, entry ->
                val slotCenterX = slotWidth * index + slotWidth / 2

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

        Row(modifier = Modifier.width(chartWidth)) {
            entries.forEach { entry ->
                Box(
                    modifier = Modifier.width(DAY_SLOT_WIDTH_DP.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = dayLabel(entry, dayLabels), style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

private fun dayLabel(entry: DailyForecastEntry, labels: Array<String>): String {
    val mondayFirstIndex = entry.date.dayOfWeek.value - 1
    return labels.getOrElse(mondayFirstIndex) { "" }
}
