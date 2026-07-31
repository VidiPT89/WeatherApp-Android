package dev.ividi.weatherapp.util

import java.util.Locale

/**
 * Backend `description` strings are always raw English: Open-Meteo's side comes from the
 * backend's own fixed WMO-code-to-English map, OpenWeatherMap's side is whatever English phrase
 * their API returns. That same raw string is also keyword-matched by
 * [dev.ividi.weatherapp.ui.common.weatherConditionGradient] /
 * [dev.ividi.weatherapp.ui.common.weatherConditionNeedsLightText] and the widget's
 * `widgetConditionEmoji` to pick styling, so it can't simply be translated at the source -- this
 * table is a *display-only* translation layer, applied only where the description is shown as
 * text to the user.
 *
 * Kept byte-for-byte identical to the iOS client's translation table so all clients show the same
 * Portuguese wording for the same backend phrase. Keys are stored lowercase; lookup is
 * case-insensitive. OpenWeatherMap's vocabulary is open-ended, so this is best-effort -- anything
 * missing falls back to the original English in [localizedWeatherDescription].
 */
private val PT_TRANSLATIONS: Map<String, String> = mapOf(
    "clear sky" to "Céu limpo",
    "mainly clear" to "Praticamente limpo",
    "partly cloudy" to "Parcialmente nublado",
    "overcast" to "Nublado",
    "fog" to "Nevoeiro",
    "depositing rime fog" to "Nevoeiro gelado",
    "light drizzle" to "Chuvisco fraco",
    "moderate drizzle" to "Chuvisco moderado",
    "dense drizzle" to "Chuvisco intenso",
    "light freezing drizzle" to "Chuvisco gelado fraco",
    "dense freezing drizzle" to "Chuvisco gelado intenso",
    "slight rain" to "Chuva fraca",
    "moderate rain" to "Chuva moderada",
    "heavy rain" to "Chuva forte",
    "light freezing rain" to "Chuva gelada fraca",
    "heavy freezing rain" to "Chuva gelada forte",
    "slight snow fall" to "Neve fraca",
    "moderate snow fall" to "Neve moderada",
    "heavy snow fall" to "Neve forte",
    "snow grains" to "Grãos de neve",
    "slight rain showers" to "Aguaceiros fracos",
    "moderate rain showers" to "Aguaceiros moderados",
    "violent rain showers" to "Aguaceiros fortes",
    "slight snow showers" to "Aguaceiros de neve fracos",
    "heavy snow showers" to "Aguaceiros de neve fortes",
    "thunderstorm" to "Trovoada",
    "thunderstorm with slight hail" to "Trovoada com granizo fraco",
    "thunderstorm with heavy hail" to "Trovoada com granizo forte",
    "unknown" to "Desconhecido",
    "clear" to "Céu limpo",
    "few clouds" to "Poucas nuvens",
    "scattered clouds" to "Nuvens dispersas",
    "broken clouds" to "Céu muito nublado",
    "overcast clouds" to "Céu encoberto",
    "light rain" to "Chuva fraca",
    "heavy intensity rain" to "Chuva intensa",
    "shower rain" to "Aguaceiros",
    "light intensity shower rain" to "Aguaceiros fracos",
    "heavy intensity shower rain" to "Aguaceiros fortes",
    "ragged shower rain" to "Aguaceiros irregulares",
    "snow" to "Neve",
    "light snow" to "Neve fraca",
    "heavy snow" to "Neve forte",
    "sleet" to "Água-neve",
    "mist" to "Neblina",
    "smoke" to "Fumo",
    "haze" to "Neblina seca",
    "sand/dust whirls" to "Redemoinhos de areia/poeira",
    "dust" to "Poeira",
    "sand" to "Areia",
    "volcanic ash" to "Cinza vulcânica",
    "squalls" to "Rajadas de vento",
    "tornado" to "Tornado",
)

/**
 * Translates a raw backend weather [description] for display, without touching the string used
 * for condition-based styling elsewhere (callers must keep passing the raw [description] to
 * [dev.ividi.weatherapp.ui.common.weatherConditionGradient] and friends, not this function's
 * result). Portuguese when [locale]'s language is Portuguese and the phrase is in
 * [PT_TRANSLATIONS]; otherwise the original English, capitalized -- matching the app's prior
 * display convention and never leaving an unmapped phrase blank.
 *
 * [locale] defaults to [Locale.getDefault], which [MainActivity][dev.ividi.weatherapp.MainActivity]
 * keeps in sync with the user's chosen in-app language (see its `attachBaseContext` override) --
 * the same signal the rest of the app relies on for the active locale.
 */
fun localizedWeatherDescription(description: String, locale: Locale = Locale.getDefault()): String {
    val capitalizedEnglish = description.replaceFirstChar { it.uppercase() }
    if (locale.language != "pt") return capitalizedEnglish
    return PT_TRANSLATIONS[description.lowercase()] ?: capitalizedEnglish
}
