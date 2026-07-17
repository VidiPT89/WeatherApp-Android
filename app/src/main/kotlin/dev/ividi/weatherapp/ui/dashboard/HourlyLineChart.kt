package dev.ividi.weatherapp.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import dev.ividi.weatherapp.data.model.HourlyForecastEntry

private const val CHART_HEIGHT_DP = 200
private const val HORIZONTAL_PADDING_DP = 8
private const val VERTICAL_PADDING_DP = 16
private const val STROKE_WIDTH_DP = 3
private const val POINT_RADIUS_DP = 2

/**
 * Simple hand-rolled Canvas line chart for the ~72-point hourly forecast. No third-party
 * charting dependency, per the project's KISS/YAGNI stance -- this is a straightforward
 * line render, not worth pulling in a heavy library for.
 */
@Composable
fun HourlyLineChart(entries: List<HourlyForecastEntry>, modifier: Modifier = Modifier) {
    if (entries.isEmpty()) return

    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant

    val minTemp = entries.minOf { it.temperature }
    val maxTemp = entries.maxOf { it.temperature }
    val tempRange = (maxTemp - minTemp).takeIf { it > 0.0 } ?: 1.0

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(CHART_HEIGHT_DP.dp),
    ) {
        val horizontalPadding = HORIZONTAL_PADDING_DP.dp.toPx()
        val verticalPadding = VERTICAL_PADDING_DP.dp.toPx()
        val chartWidth = size.width - horizontalPadding * 2
        val chartHeight = size.height - verticalPadding * 2

        // Baseline grid line.
        drawLine(
            color = gridColor,
            start = Offset(horizontalPadding, size.height - verticalPadding),
            end = Offset(size.width - horizontalPadding, size.height - verticalPadding),
            strokeWidth = 1.dp.toPx(),
        )

        val stepX = if (entries.size > 1) chartWidth / (entries.size - 1) else 0f
        val points = entries.mapIndexed { index, entry ->
            val normalizedY = ((entry.temperature - minTemp) / tempRange).toFloat()
            val x = horizontalPadding + stepX * index
            val y = verticalPadding + chartHeight * (1f - normalizedY)
            Offset(x, y)
        }

        for (i in 0 until points.size - 1) {
            drawLine(
                color = lineColor,
                start = points[i],
                end = points[i + 1],
                strokeWidth = STROKE_WIDTH_DP.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
            )
        }

        points.forEach { point ->
            drawCircle(color = lineColor, radius = POINT_RADIUS_DP.dp.toPx(), center = point)
        }
    }
}
