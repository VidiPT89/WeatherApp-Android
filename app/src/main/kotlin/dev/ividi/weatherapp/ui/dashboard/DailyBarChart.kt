package dev.ividi.weatherapp.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.ividi.weatherapp.R
import dev.ividi.weatherapp.data.model.DailyForecastEntry
import dev.ividi.weatherapp.data.model.Units
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.math.roundToInt

private const val CHART_HEIGHT_DP = 200
private const val VERTICAL_PADDING_DP = 16
private const val DAY_SLOT_WIDTH_DP = 60
private const val BAR_WIDTH_FRACTION = 0.4f
private const val DAYS_PER_PAGE = 6
private const val GRIDLINE_COUNT = 3
private const val PLACEHOLDER_PAD = 2

/**
 * Hand-rolled Canvas bar chart showing min+max temperature for every daily entry (up to 16,
 * per the backend's extended forecast). Each day gets a fixed-width slot so the chart can grow
 * past one screen width; it scrolls horizontally rather than truncating or squeezing the data.
 *
 * Design intent: this isn't just for a casual glance -- shore fishermen and surfers rely on it
 * too, so both the max AND min are labeled directly on every bar (not just the overall range),
 * bars are colored on a cool-to-warm scale for an at-a-glance read, and today's column is called
 * out explicitly rather than blending in as just another weekday abbreviation.
 */
@Composable
fun DailyBarChart(entries: List<DailyForecastEntry>, units: Units, modifier: Modifier = Modifier) {
    if (entries.isEmpty()) return

    val minBarColor = MaterialTheme.colorScheme.onSurfaceVariant
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val dayLabels = stringArrayResource(R.array.weekday_labels_short)
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }

    val overallMin = entries.minOf { it.temperatureMin }
    val overallMax = entries.maxOf { it.temperatureMax }
    val tempRange = (overallMax - overallMin).takeIf { it > 0.0 } ?: 1.0

    val chartWidth = remember(entries.size) { (DAY_SLOT_WIDTH_DP * entries.size).dp }
    val dayWidthPx = with(density) { DAY_SLOT_WIDTH_DP.dp.toPx() }
    val pageWidthPx = (dayWidthPx * DAYS_PER_PAGE).roundToInt()

    val visibleStartIndex = (scrollState.value / dayWidthPx).roundToInt().coerceIn(0, entries.size - 1)
    val visibleEndIndex = (visibleStartIndex + DAYS_PER_PAGE - 1).coerceIn(0, entries.size - 1)

    Column(modifier = modifier.fillMaxWidth()) {
        ForecastChartHeader(
            rangeLabel = "${entries[visibleStartIndex].date.toShortLabel()} – ${entries[visibleEndIndex].date.toShortLabel()}",
            canPageBackward = scrollState.value > 0,
            canPageForward = scrollState.value < scrollState.maxValue,
            onPageBackward = {
                scope.launch { scrollState.animateScrollTo((scrollState.value - pageWidthPx).coerceAtLeast(0)) }
            },
            onPageForward = {
                scope.launch { scrollState.animateScrollTo((scrollState.value + pageWidthPx).coerceAtMost(scrollState.maxValue)) }
            },
        )

        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Column(
                modifier = Modifier.height(CHART_HEIGHT_DP.dp).padding(end = 4.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = "${overallMax.roundToInt()}${units.temperatureSymbol}", style = MaterialTheme.typography.labelSmall)
                Text(text = "${overallMin.roundToInt()}${units.temperatureSymbol}", style = MaterialTheme.typography.labelSmall)
            }

            Column(modifier = Modifier.horizontalScroll(scrollState)) {
                Row(modifier = Modifier.width(chartWidth)) {
                    entries.forEach { entry ->
                        Box(modifier = Modifier.width(DAY_SLOT_WIDTH_DP.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = "${entry.temperatureMax.roundToInt()}°",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            )
                        }
                    }
                }

                Canvas(
                    modifier = Modifier
                        .width(chartWidth)
                        .height(CHART_HEIGHT_DP.dp),
                ) {
                    val verticalPadding = VERTICAL_PADDING_DP.dp.toPx()
                    val slotWidth = size.width / entries.size
                    val chartHeight = size.height - verticalPadding * 2
                    val barWidth = slotWidth * BAR_WIDTH_FRACTION

                    for (i in 1 until GRIDLINE_COUNT) {
                        val y = verticalPadding + chartHeight * i / GRIDLINE_COUNT
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx(),
                        )
                    }

                    entries.forEachIndexed { index, entry ->
                        val slotCenterX = slotWidth * index + slotWidth / 2

                        val topNormalized = ((entry.temperatureMax - overallMin) / tempRange).toFloat()
                        val bottomNormalized = ((entry.temperatureMin - overallMin) / tempRange).toFloat()

                        val topY = verticalPadding + chartHeight * (1f - topNormalized)
                        val bottomY = verticalPadding + chartHeight * (1f - bottomNormalized)

                        drawRoundRect(
                            color = temperatureColor(entry.temperatureMax, overallMin, overallMax),
                            topLeft = Offset(slotCenterX - barWidth / 2, topY),
                            size = Size(barWidth, (bottomY - topY).coerceAtLeast(4f)),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth * 0.3f),
                        )
                        drawCircle(
                            color = minBarColor,
                            radius = 3.dp.toPx(),
                            center = Offset(slotCenterX, bottomY),
                        )
                    }
                }

                Row(modifier = Modifier.width(chartWidth)) {
                    entries.forEach { entry ->
                        Box(modifier = Modifier.width(DAY_SLOT_WIDTH_DP.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = "${entry.temperatureMin.roundToInt()}${units.temperatureSymbol}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                Row(modifier = Modifier.width(chartWidth)) {
                    entries.forEach { entry ->
                        val isToday = entry.date == today
                        Box(modifier = Modifier.width(DAY_SLOT_WIDTH_DP.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = if (isToday) stringResource(R.string.forecast_today_label) else dayLabel(entry, dayLabels),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isToday) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal,
                                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Interpolates cool->warm across this chart's own data range, so the hottest day in the
 * current forecast always reads as "warm" and the coolest as "cool", regardless of units. */
private fun temperatureColor(value: Double, min: Double, max: Double): Color {
    if (max <= min) return Color(0xFF3B82F6)
    val t = ((value - min) / (max - min)).coerceIn(0.0, 1.0)
    val cool = Triple(0.20, 0.55, 0.95)
    val warm = Triple(0.95, 0.35, 0.20)
    return Color(
        red = (cool.first + (warm.first - cool.first) * t).toFloat(),
        green = (cool.second + (warm.second - cool.second) * t).toFloat(),
        blue = (cool.third + (warm.third - cool.third) * t).toFloat(),
    )
}

internal fun dayLabel(entry: DailyForecastEntry, labels: Array<String>): String {
    val mondayFirstIndex = entry.date.dayOfWeek.value - 1
    return labels.getOrElse(mondayFirstIndex) { "" }
}

private fun LocalDate.toShortLabel(): String {
    val day = dayOfMonth.toString().padStart(PLACEHOLDER_PAD, '0')
    val month = monthNumber.toString().padStart(PLACEHOLDER_PAD, '0')
    return "$day/$month"
}
