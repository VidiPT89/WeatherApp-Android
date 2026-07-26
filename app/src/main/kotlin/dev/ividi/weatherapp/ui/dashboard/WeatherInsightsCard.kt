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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.ividi.weatherapp.R
import dev.ividi.weatherapp.data.model.WeatherInsightsResponse
import dev.ividi.weatherapp.ui.common.ConditionLabels
import dev.ividi.weatherapp.ui.common.ConditionTone
import dev.ividi.weatherapp.ui.common.toColor

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
                    value = "${ConditionLabels.moonPhase(insights.moonPhase.phase)} · ${insights.moonPhase.illuminationPercent}%",
                    tone = ConditionTone.NEUTRAL,
                )
                InsightBadge(
                    label = stringResource(R.string.insights_uv_risk),
                    value = ConditionLabels.uvRisk(insights.uvRiskLabel),
                    tone = ConditionLabels.uvRiskTone(insights.uvRiskLabel),
                )
                InsightBadge(
                    label = stringResource(R.string.insights_outdoor_activity),
                    value = "${ConditionLabels.outdoorActivity(insights.outdoorActivityLabel)} · ${insights.outdoorActivityScore}",
                    tone = ConditionLabels.outdoorActivityTone(insights.outdoorActivityLabel),
                )
                insights.fishingConditionLabel?.let { fishingLabel ->
                    InsightBadge(
                        label = stringResource(R.string.insights_fishing_conditions),
                        value = ConditionLabels.fishingCondition(fishingLabel),
                        tone = ConditionLabels.conditionTone(fishingLabel),
                    )
                }
            }
        }
    }
}

@Composable
private fun InsightBadge(label: String, value: String, tone: ConditionTone) {
    val color = tone.toColor()
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
