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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.ividi.weatherapp.R
import dev.ividi.weatherapp.data.model.HourlyForecastEntry
import dev.ividi.weatherapp.data.model.Units
import dev.ividi.weatherapp.util.toDisplayTime
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.math.roundToInt

private const val CHART_HEIGHT_DP = 200
private const val VERTICAL_PADDING_DP = 16
private const val STROKE_WIDTH_DP = 3
private const val POINT_RADIUS_DP = 3
private const val HOUR_SLOT_WIDTH_DP = 32
private const val PRECIPITATION_BAR_MAX_HEIGHT_DP = 40
private const val HOUR_LABEL_STEP = 3
// A full day per page tap: was 7h, which took ~7 taps to page through the 48h forecast and made
// the header's "visible range" caption understate how much was actually on screen on most phones
// (the chart itself scrolls freely by drag already; this constant only governs the discrete
// paging-button jump and that caption). 24h mirrors iOS's widened hourly window for the same
// "too much paging to see the day's temperatures" complaint.
private const val HOURS_PER_PAGE = 24

/**
 * Hand-rolled Canvas line chart for the full hourly forecast (up to 48 points, per the
 * backend's extended forecast). No third-party charting dependency, per the project's KISS/
 * YAGNI stance. Each hour gets a fixed-width slot so the chart can grow past one screen width;
 * it scrolls horizontally rather than truncating or squeezing the data. A light precipitation-
 * probability bar is drawn beneath the temperature line for each point.
 *
 * Design intent: every labeled tick gets a point + its exact temperature (not just an
 * inferred line height), and a dashed "now" marker anchors the reader to the current hour --
 * both matter as much to shore fishermen/surfers planning their day as to a casual glance.
 */
@Composable
fun HourlyLineChart(entries: List<HourlyForecastEntry>, units: Units, modifier: Modifier = Modifier) {
    if (entries.isEmpty()) return

    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val precipitationColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f)
    val nowColor = MaterialTheme.colorScheme.error
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    val minTemp = entries.minOf { it.temperature }
    val maxTemp = entries.maxOf { it.temperature }
    val tempRange = (maxTemp - minTemp).takeIf { it > 0.0 } ?: 1.0

    val chartWidth = remember(entries.size) { (HOUR_SLOT_WIDTH_DP * entries.size).dp }
    val hourWidthPx = with(density) { HOUR_SLOT_WIDTH_DP.dp.toPx() }
    val pageWidthPx = (hourWidthPx * HOURS_PER_PAGE).roundToInt()

    val zone = remember { TimeZone.currentSystemDefault() }
    val now = remember { Clock.System.now().toLocalDateTime(zone) }
    // Assumes ~1-hour spacing between entries (true of this backend's hourly forecast),
    // so "hours since the first entry" maps directly to an x position in slot-widths.
    val nowOffsetHours = remember {
        (now.toInstant(zone) - entries.first().time.toInstant(zone)).inWholeMinutes / 60.0
    }

    val visibleStartIndex = (scrollState.value / hourWidthPx).roundToInt().coerceIn(0, entries.size - 1)
    val visibleEndIndex = (visibleStartIndex + HOURS_PER_PAGE - 1).coerceIn(0, entries.size - 1)

    Column(modifier = modifier.fillMaxWidth()) {
        ForecastChartHeader(
            rangeLabel = "${entries[visibleStartIndex].time.toDisplayTime()} – ${entries[visibleEndIndex].time.toDisplayTime()}",
            canPageBackward = scrollState.value > 0,
            canPageForward = scrollState.value < scrollState.maxValue,
            onPageBackward = {
                scope.launch { scrollState.animateScrollTo((scrollState.value - pageWidthPx).coerceAtLeast(0)) }
            },
            onPageForward = {
                scope.launch { scrollState.animateScrollTo((scrollState.value + pageWidthPx).coerceAtMost(scrollState.maxValue)) }
            },
        )
        Text(
            text = stringResource(R.string.forecast_now_label, now.toDisplayTime()),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = nowColor,
            modifier = Modifier.padding(top = 2.dp),
        )

        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Column(
                modifier = Modifier.height(CHART_HEIGHT_DP.dp).padding(end = 4.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = "${maxTemp.roundToInt()}${units.temperatureSymbol}", style = MaterialTheme.typography.labelSmall)
                Text(text = "${minTemp.roundToInt()}${units.temperatureSymbol}", style = MaterialTheme.typography.labelSmall)
            }

            Column(modifier = Modifier.horizontalScroll(scrollState)) {
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

                    points.forEachIndexed { index, point ->
                        if (index % HOUR_LABEL_STEP == 0) {
                            drawCircle(color = lineColor, radius = POINT_RADIUS_DP.dp.toPx() * 1.6f, center = point)
                        }
                    }

                    // "Now" marker: a dashed vertical rule at the current moment's x position,
                    // clamped so it doesn't render outside this chart's own data range.
                    val nowX = (nowOffsetHours * slotWidth).toFloat().coerceIn(0f, size.width)
                    drawLine(
                        color = nowColor,
                        start = Offset(nowX, 0f),
                        end = Offset(nowX, size.height),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)),
                    )
                }

                // Temperature labels for the same labeled points drawn in the Canvas above,
                // as real Text composables laid out in their own row (Canvas can't measure text).
                Row(modifier = Modifier.width(chartWidth)) {
                    entries.forEachIndexed { index, entry ->
                        Box(modifier = Modifier.width(HOUR_SLOT_WIDTH_DP.dp), contentAlignment = Alignment.Center) {
                            if (index % HOUR_LABEL_STEP == 0) {
                                Text(
                                    text = "${entry.temperature.roundToInt()}°",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
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
    }
}

private fun hourLabel(entry: HourlyForecastEntry): String =
    entry.time.hour.toString().padStart(2, '0')
