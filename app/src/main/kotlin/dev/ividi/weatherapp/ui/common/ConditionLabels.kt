package dev.ividi.weatherapp.ui.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import dev.ividi.weatherapp.R

/**
 * Maps the backend's fixed English enum-like condition labels (uv risk, outdoor activity,
 * fishing, surf) to a localized string and a color tone. Shared between [WeatherInsightsCard]
 * (today only) and the daily forecast's per-day insight rows (all 16 days), since both surface
 * the same vocabulary.
 */
enum class ConditionTone { NEUTRAL, GOOD, WARN, BAD }

@Composable
fun ConditionTone.toColor(): Color = when (this) {
    ConditionTone.NEUTRAL -> MaterialTheme.colorScheme.onSurfaceVariant
    ConditionTone.GOOD -> MaterialTheme.colorScheme.primary
    ConditionTone.WARN -> MaterialTheme.colorScheme.tertiary
    ConditionTone.BAD -> MaterialTheme.colorScheme.error
}

object ConditionLabels {
    @Composable
    fun moonPhase(phase: String): String = stringResource(
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
    fun uvRisk(label: String): String = stringResource(
        when (label) {
            "Low" -> R.string.uv_risk_low
            "Moderate" -> R.string.uv_risk_moderate
            "High" -> R.string.uv_risk_high
            "Very High" -> R.string.uv_risk_very_high
            "Extreme" -> R.string.uv_risk_extreme
            else -> R.string.uv_risk_low
        },
    )

    fun uvRiskTone(label: String): ConditionTone = when (label) {
        "Low" -> ConditionTone.GOOD
        "Moderate" -> ConditionTone.NEUTRAL
        "High", "Very High" -> ConditionTone.WARN
        "Extreme" -> ConditionTone.BAD
        else -> ConditionTone.NEUTRAL
    }

    @Composable
    fun outdoorActivity(label: String): String = stringResource(
        when (label) {
            "Great" -> R.string.activity_great
            "Good" -> R.string.activity_good
            "Fair" -> R.string.activity_fair
            "Poor" -> R.string.activity_poor
            else -> R.string.activity_fair
        },
    )

    fun outdoorActivityTone(label: String): ConditionTone = when (label) {
        "Great", "Good" -> ConditionTone.GOOD
        "Fair" -> ConditionTone.NEUTRAL
        "Poor" -> ConditionTone.BAD
        else -> ConditionTone.NEUTRAL
    }

    @Composable
    fun fishingCondition(label: String): String = stringResource(
        when (label) {
            "Good" -> R.string.fishing_good
            "Fair" -> R.string.fishing_fair
            "Poor" -> R.string.fishing_poor
            else -> R.string.fishing_fair
        },
    )

    @Composable
    fun surfCondition(label: String): String = stringResource(
        when (label) {
            "Good" -> R.string.surf_good
            "Fair" -> R.string.surf_fair
            "Poor" -> R.string.surf_poor
            else -> R.string.surf_fair
        },
    )

    /** Shared tone mapping for fishing/surf, which use the same Good/Fair/Poor vocabulary. */
    fun conditionTone(label: String): ConditionTone = when (label) {
        "Good" -> ConditionTone.GOOD
        "Fair" -> ConditionTone.NEUTRAL
        "Poor" -> ConditionTone.BAD
        else -> ConditionTone.NEUTRAL
    }
}
