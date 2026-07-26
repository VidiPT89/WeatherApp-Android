package dev.ividi.weatherapp.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phishing
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.ividi.weatherapp.R
import dev.ividi.weatherapp.data.model.DailyForecastEntry
import dev.ividi.weatherapp.ui.common.ConditionLabels
import dev.ividi.weatherapp.ui.common.ConditionTone
import dev.ividi.weatherapp.ui.common.toColor
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * Full list of [DailyInsightRow]s for every entry in the daily forecast, with dividers between
 * days. Rendered below [DailyBarChart] when the daily tab is selected.
 */
@Composable
fun DailyInsightList(entries: List<DailyForecastEntry>, modifier: Modifier = Modifier) {
    val dayLabels = stringArrayResource(R.array.weekday_labels_short)
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        entries.forEachIndexed { index, entry ->
            DailyInsightRow(entry = entry, isToday = entry.date == today, dayLabel = dayLabel(entry, dayLabels))
            if (index != entries.lastIndex) HorizontalDivider()
        }
    }
}

/**
 * One row of the daily forecast's expanded view: rain chance plus the wind/wave-derived
 * condition badges (UV risk, fishing, surf) the backend already computes per day. Rendered
 * below [DailyBarChart] for every entry, mirroring the "more about today" vocabulary used by
 * [WeatherInsightsCard] but per-day instead of today-only.
 */
@Composable
fun DailyInsightRow(entry: DailyForecastEntry, isToday: Boolean, dayLabel: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = if (isToday) stringResource(R.string.forecast_today_label) else dayLabel,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.width(48.dp),
            )
            ConditionChip(
                icon = Icons.Filled.WaterDrop,
                text = stringResource(R.string.daily_insight_rain_chance, entry.precipitationProbabilityMax),
                tone = if (entry.rainLikely) ConditionTone.WARN else ConditionTone.NEUTRAL,
            )
        }

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ConditionChip(
                icon = Icons.Filled.WbSunny,
                text = ConditionLabels.uvRisk(entry.uvRiskLabel),
                tone = ConditionLabels.uvRiskTone(entry.uvRiskLabel),
            )
            entry.fishingConditionLabel?.let { label ->
                ConditionChip(
                    icon = Icons.Filled.Phishing,
                    text = ConditionLabels.fishingCondition(label),
                    tone = ConditionLabels.conditionTone(label),
                )
            }
            entry.surfConditionLabel?.let { label ->
                ConditionChip(
                    icon = Icons.Filled.Waves,
                    text = ConditionLabels.surfCondition(label),
                    tone = ConditionLabels.conditionTone(label),
                )
            }
        }
    }
}

@Composable
private fun ConditionChip(icon: ImageVector, text: String, tone: ConditionTone) {
    val color = tone.toColor()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.width(14.dp))
        Text(text = text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = color)
    }
}
