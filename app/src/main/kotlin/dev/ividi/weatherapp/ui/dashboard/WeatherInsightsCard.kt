package dev.ividi.weatherapp.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.ividi.weatherapp.R
import dev.ividi.weatherapp.data.model.WeatherInsightsResponse

/**
 * "More about today" card: moon phase, UV risk, outdoor-activity score and (when available)
 * fishing conditions -- derived indicators computed server-side from data already fetched for
 * the dashboard, from the `/insights` endpoint.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WeatherInsightsCard(insights: WeatherInsightsResponse, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.insights_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.insights_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp),
            )

            FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                InsightBadge(
                    label = stringResource(R.string.insights_moon_phase),
                    value = "${moonPhaseLabel(insights.moonPhase.phase)} · ${insights.moonPhase.illuminationPercent}%",
                    tone = InsightTone.NEUTRAL,
                )
                InsightBadge(
                    label = stringResource(R.string.insights_uv_risk),
                    value = uvRiskLabel(insights.uvRiskLabel),
                    tone = uvRiskTone(insights.uvRiskLabel),
                )
                InsightBadge(
                    label = stringResource(R.string.insights_outdoor_activity),
                    value = "${activityLabel(insights.outdoorActivityLabel)} · ${insights.outdoorActivityScore}",
                    tone = activityTone(insights.outdoorActivityLabel),
                )
                insights.fishingConditionLabel?.let { fishingLabel ->
                    InsightBadge(
                        label = stringResource(R.string.insights_fishing_conditions),
                        value = fishingConditionLabel(fishingLabel),
                        tone = fishingTone(fishingLabel),
                    )
                }
            }
        }
    }
}

private enum class InsightTone { NEUTRAL, GOOD, WARN, BAD }

@Composable
private fun toneColor(tone: InsightTone): Color = when (tone) {
    InsightTone.NEUTRAL -> MaterialTheme.colorScheme.onSurfaceVariant
    InsightTone.GOOD -> MaterialTheme.colorScheme.primary
    InsightTone.WARN -> MaterialTheme.colorScheme.tertiary
    InsightTone.BAD -> MaterialTheme.colorScheme.error
}

@Composable
private fun InsightBadge(label: String, value: String, tone: InsightTone) {
    val color = toneColor(tone)
    Column {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = color,
            modifier = Modifier
                .padding(top = 4.dp)
                .background(color.copy(alpha = 0.12f), RoundedCornerShape(50))
                .padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

/** The backend returns these as fixed English enum-like strings (not localized). */
@Composable
private fun moonPhaseLabel(phase: String): String = stringResource(
    when (phase) {
        "New Moon" -> R.string.moon_phase_new_moon
        "Waxing Crescent" -> R.string.moon_phase_waxing_crescent
        "First Quarter" -> R.string.moon_phase_first_quarter
        "Waxing Gibbous" -> R.string.moon_phase_waxing_gibbous
        "Full Moon" -> R.string.moon_phase_full_moon
        "Waning Gibbous" -> R.string.moon_phase_waning_gibbous
        "Last Quarter" -> R.string.moon_phase_last_quarter
        "Waning Crescent" -> R.string.moon_phase_waning_crescent
        else -> R.string.moon_phase_new_moon
    },
)

@Composable
private fun uvRiskLabel(label: String): String = stringResource(
    when (label) {
        "Low" -> R.string.uv_risk_low
        "Moderate" -> R.string.uv_risk_moderate
        "High" -> R.string.uv_risk_high
        "Very High" -> R.string.uv_risk_very_high
        "Extreme" -> R.string.uv_risk_extreme
        else -> R.string.uv_risk_low
    },
)

private fun uvRiskTone(label: String): InsightTone = when (label) {
    "Low" -> InsightTone.GOOD
    "Moderate" -> InsightTone.NEUTRAL
    "High", "Very High" -> InsightTone.WARN
    "Extreme" -> InsightTone.BAD
    else -> InsightTone.NEUTRAL
}

@Composable
private fun activityLabel(label: String): String = stringResource(
    when (label) {
        "Great" -> R.string.activity_great
        "Good" -> R.string.activity_good
        "Fair" -> R.string.activity_fair
        "Poor" -> R.string.activity_poor
        else -> R.string.activity_fair
    },
)

private fun activityTone(label: String): InsightTone = when (label) {
    "Great", "Good" -> InsightTone.GOOD
    "Fair" -> InsightTone.NEUTRAL
    "Poor" -> InsightTone.BAD
    else -> InsightTone.NEUTRAL
}

@Composable
private fun fishingConditionLabel(label: String): String = stringResource(
    when (label) {
        "Good" -> R.string.fishing_good
        "Fair" -> R.string.fishing_fair
        "Poor" -> R.string.fishing_poor
        else -> R.string.fishing_fair
    },
)

private fun fishingTone(label: String): InsightTone = when (label) {
    "Good" -> InsightTone.GOOD
    "Fair" -> InsightTone.NEUTRAL
    "Poor" -> InsightTone.BAD
    else -> InsightTone.NEUTRAL
}
