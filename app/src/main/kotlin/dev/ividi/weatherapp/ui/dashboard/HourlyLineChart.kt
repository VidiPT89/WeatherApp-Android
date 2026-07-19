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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import dev.ividi.weatherapp.data.model.HourlyForecastEntry

private const val CHART_HEIGHT_DP = 200
private const val VERTICAL_PADDING_DP = 16
private const val STROKE_WIDTH_DP = 3
private const val POINT_RADIUS_DP = 2
private const val HOUR_SLOT_WIDTH_DP = 28
private const val PRECIPITATION_BAR_MAX_HEIGHT_DP = 40
private const val HOUR_LABEL_STEP = 3

/**
 * Hand-rolled Canvas line chart for the full hourly forecast (up to 48 points, per the
 * backend's extended forecast). No third-party charting dependency, per the project's KISS/
 * YAGNI stance. Each hour gets a fixed-width slot so the chart can grow past one screen width;
 * it scrolls horizontally rather than truncating or squeezing the data. A light precipitation-
 * probability bar is drawn beneath the temperature line for each point.
 */
@Composable
fun HourlyLineChart(entries: List<HourlyForecastEntry>, modifier: Modifier = Modifier) {
    if (entries.isEmpty()) return

    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val precipitationColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f)
    val scrollState = rememberScrollState()

    val minTemp = entries.minOf { it.temperature }
    val maxTemp = entries.maxOf { it.temperature }
    val tempRange = (maxTemp - minTemp).takeIf { it > 0.0 } ?: 1.0

    val chartWidth = remember(entries.size) { (HOUR_SLOT_WIDTH_DP * entries.size).dp }

    Column(modifier = modifier.fillMaxWidth().horizontalScroll(scrollState)) {
        Canvas(
            modifier = Modifier
                .width(chartWidth)
                .height(CHART_HEIGHT_DP.dp),
        ) {
            val verticalPadding = VERTICAL_PADDING_DP.dp.toPx()
            val slotWidth = size.width / entries.size
            val chartHeight = size.height - verticalPadding * 2
            val precipitationBarMaxHeight = PRECIPITATION_BAR_MAX_HEIGHT_DP.dp.toPx()

            drawLine(
                color = gridColor,
                start = Offset(0f, size.height - verticalPadding),
                end = Offset(size.width, size.height - verticalPadding),
                strokeWidth = 1.dp.toPx(),
            )

            entries.forEachIndexed { index, entry ->
                val barHeight = precipitationBarMaxHeight * (entry.precipitationProbability / 100f)
                val slotCenterX = slotWidth * index + slotWidth / 2
                drawRect(
                    color = precipitationColor,
                    topLeft = Offset(
                        slotCenterX - slotWidth * 0.3f,
                        size.height - verticalPadding - barHeight,
                    ),
                    size = androidx.compose.ui.geometry.Size(slotWidth * 0.6f, barHeight),
                )
            }

            val points = entries.mapIndexed { index, entry ->
                val normalizedY = ((entry.temperature - minTemp) / tempRange).toFloat()
                val x = slotWidth * index + slotWidth / 2
                val y = verticalPadding + chartHeight * (1f - normalizedY)
                Offset(x, y)
            }

            for (i in 0 until points.size - 1) {
                drawLine(
                    color = lineColor,
                    start = points[i],
                    end = points[i + 1],
                    strokeWidth = STROKE_WIDTH_DP.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }

            points.forEach { point ->
                drawCircle(color = lineColor, radius = POINT_RADIUS_DP.dp.toPx(), center = point)
            }
        }

        Row(modifier = Modifier.width(chartWidth)) {
            entries.forEachIndexed { index, entry ->
                Box(
                    modifier = Modifier.width(HOUR_SLOT_WIDTH_DP.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (index % HOUR_LABEL_STEP == 0) {
                        Text(text = hourLabel(entry), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

private fun hourLabel(entry: HourlyForecastEntry): String =
    entry.time.hour.toString().padStart(2, '0')
