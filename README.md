# 🤖 WeatherApp — Android Client

> Native Kotlin/Jetpack Compose client for the [Weather API Aggregator](https://github.com/VidiPT89/WeatherAPI) — proves the same backend contract that powers the [web](https://github.com/VidiPT89/WeatherApp) and [iOS](https://github.com/VidiPT89/WeatherApp-iOS) clients serves Android too.

**Live demo:** not published (no Play Store account for this project) — can also point at the live backend directly, see *How to Run*.

One of three clients (Web / iOS / Android) built on top of the same backend. This app talks directly to the Weather API — it never talks to Open-Meteo/OpenWeatherMap directly.

## 📦 What's Inside

- 🔎 City search with debounced autocomplete (backend geocoding endpoint) — also used by "add favorite," so a favorite can only ever be a real geocoded place, never unvalidated free text
- 🌡️ Current weather + hourly/daily forecast chart (hand-rolled Canvas line/bar charts), with a °C/°F toggle
- 📇 **Tap a Dashboard card for more** — the weather, sea-conditions and "more about today" cards each open a bottom sheet with more detail than fits on the compact card (full stat breakdown, tide list with today's swell/fishing/surf context, next-7-days UV/outdoor/fishing/surf outlook)
- 🏠 **Home-screen widget** (Jetpack Glance) — shows the last weather the app itself loaded; it never fetches on its own, it's refreshed only when the Dashboard successfully loads weather
- ⚡ **Cache badge** — "dados frescos" vs "servido da cache há Xs", ticking live from the response's `fromCache` flag and timestamp
- 🔁 **Fallback banner** — appears when the response was served by the secondary provider
- 🔐 Auth (register/login, JWT in `EncryptedSharedPreferences`), favorite cities (add/remove), search history (delete one entry or clear all), saved unit preference
- 🛡️ **Admin dashboard** — admin accounts get a "Administração" entry in Settings listing every registered account (email, role, joined date) with a per-row delete action (self-delete hidden client-side, refused server-side too)
- ✅ Loading, error and empty states throughout

## 🛠️ Tech Stack

![Kotlin](https://img.shields.io/badge/Kotlin%202-7F52FF?style=flat&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=flat&logo=jetpackcompose&logoColor=white)
![Material 3](https://img.shields.io/badge/Material%203-757575?style=flat&logo=materialdesign&logoColor=white)
![Glance](https://img.shields.io/badge/Glance%20Widgets-4285F4?style=flat)
![Hilt](https://img.shields.io/badge/Hilt-4285F4?style=flat)
![Retrofit](https://img.shields.io/badge/Retrofit-48B983?style=flat)
![JUnit](https://img.shields.io/badge/JUnit-25A162?style=flat&logo=junit5&logoColor=white)

## 🏗️ Architecture

```
WeatherApp-Android (Jetpack Compose)
   │  Retrofit + OkHttp, Bearer token from EncryptedSharedPreferences — no BFF, talks to the API directly
   ▼
WeatherAPI (Spring Boot, sibling repo, host machine, reached via 10.0.2.2:8080 from the emulator)
   │  cache (Caffeine) → circuit breaker + retry → provider adapters
   ▼
Open-Meteo / OpenWeatherMap (external providers)
```

```
app/src/main/kotlin/dev/ividi/weatherapp/
├── data/
│   ├── model/       # kotlinx.serialization data classes mirroring the backend DTOs exactly
│   ├── network/      # Retrofit service, AuthInterceptor, error-body parsing → typed ApiException
│   ├── auth/          # EncryptedSharedPreferences-backed token storage
│   └── repository/    # incl. WeatherWidgetRepository — DataStore snapshot for the home-screen widget
├── ui/                # one ViewModel (StateFlow) + Composable screen per feature:
│                       # auth, dashboard (search, weather card, cache badge, fallback banner, forecast chart,
│                       # tap-for-detail bottom sheets), favorites, history, settings, admin (admin-only)
├── widget/             # Jetpack Glance home-screen widget (app-driven only, no independent fetching)
└── di/                 # Hilt modules
```

### Why these choices

- **Direct-to-API, no BFF**: like the iOS client (and unlike the web client, which proxies through Next.js to keep the JWT out of browser JS), `EncryptedSharedPreferences` is already a secure, sandboxed place to hold a token on-device — no need for a server-side proxy layer.
- **`10.0.2.2` instead of `localhost`**: the Android emulator runs in its own network namespace: `10.0.2.2` is Google's documented alias back to the host machine's `localhost`. A `network_security_config.xml` cleartext exception is needed for it too, since Android blocks plaintext HTTP by default since API 28.
- **Hand-rolled Canvas charts over a charting library**: there's no Compose charting library as mature as Swift Charts/Recharts; for a simple hourly-line/daily-bar chart, a small custom `Canvas` composable is less risk than pulling in and learning a third-party dependency (KISS/YAGNI).
- **Local-datetime forecast parsing**: `hourly[].time`/`daily[].date` come back from the API without a timezone offset (Open-Meteo's `timezone=auto` already localizes them), so they're parsed as `kotlinx.datetime.LocalDateTime`/`LocalDate`, not `Instant`.
- **Widget is app-driven only, by design**: the home-screen widget (Jetpack Glance) never makes its own network call — it renders whatever `WeatherWidgetRepository` last persisted, written by `DashboardViewModel` after a successful load and pushed to any placed widgets immediately via `GlanceAppWidget.updateAll`. No `WorkManager`/periodic fetch, and `updatePeriodMillis="0"` in the widget's provider XML reflects that on purpose.
- **`ModalBottomSheet` for card detail views**: the Dashboard's weather/sea-conditions/insights cards each open a `ModalBottomSheet` on tap rather than a new destination — consistent with this app already using in-place dialogs (e.g. Admin's delete confirmation) instead of extra nav-graph routes for transient, dismissible content.

## 🚀 How to Run

Prerequisites: JDK 17, Android SDK (API 34+ platform + an emulator system image), and the [Weather API](https://github.com/VidiPT89/WeatherAPI) running locally on `http://localhost:8080` (see that repo's README) — or point `NetworkModule`'s `BASE_URL` at the live deployment: `https://weather-api-production-68ff.up.railway.app/`.

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17   # or your JDK 17 install
export ANDROID_HOME=$HOME/Library/Android/sdk    # or your SDK location

./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n dev.ividi.weatherapp/.MainActivity
```

The app's base URL is `http://10.0.2.2:8080` (the emulator's alias for the host machine) — no configuration needed for the default local setup.

## ✅ Tests

```bash
./gradlew test
```

- JSON parsing fixtures for `WeatherResponse`/`ForecastResponse` (pinning the local-datetime hourly/daily parsing), `WeatherInsightsResponse`/`MarineResponse`, `UserAccount`, `Units`.
- `MockWebServer`-backed test of the API client's error-body parsing (non-2xx → typed `ApiException` carrying the backend's `message`) and token-refresh/retry behavior.
- ViewModel tests for the admin user list/delete/error paths, search history, and favorites (incl. the debounced geocoding-suggestion pipeline behind "add favorite").
- Pure-function unit tests for cache-age formatting and fallback-provider detection.

Given the project's scope (three client apps on one backend), test effort is weighted toward parsing/business-logic rather than Compose UI layout.

## 📝 Notes

- Requires the backend reachable at `http://10.0.2.2:8080` from the emulator; on a physical device, point it at the host machine's LAN IP instead.
- The home-screen widget shows a placeholder until the app has been opened and successfully loaded weather at least once — it has no data of its own before that.

## 📄 License

MIT — see [LICENSE](LICENSE).

---

Developed by **David Arsénio Martins**
🌐 [ividi.dev](https://ividi.dev/) · 💻 [github.com/VidiPT89](https://github.com/VidiPT89/)
