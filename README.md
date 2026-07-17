# 🤖 WeatherApp — Android Client

> Native Kotlin/Jetpack Compose client for the [Weather API Aggregator](https://github.com/VidiPT89/WeatherAPI) — proves the same backend contract that powers the [web](https://github.com/VidiPT89/WeatherApp) and [iOS](https://github.com/VidiPT89/WeatherApp-iOS) clients serves Android too.

**Live demo:** not published (no Play Store account for this project) — can also point at the live backend directly, see *How to Run*.

One of three clients (Web / iOS / Android) built on top of the same backend. This app talks directly to the Weather API — it never talks to Open-Meteo/OpenWeatherMap directly.

## 📦 What's Inside

- 🔎 City search with debounced autocomplete (backend geocoding endpoint)
- 🌡️ Current weather + hourly/daily forecast chart (hand-rolled Canvas line/bar charts), with a °C/°F toggle
- ⚡ **Cache badge** — "dados frescos" vs "servido da cache há Xs", ticking live from the response's `fromCache` flag and timestamp
- 🔁 **Fallback banner** — appears when the response was served by the secondary provider
- ⚖️ **Provider comparison screen** — the same city, side by side, across every configured provider, with a computed average
- 🔐 Auth (register/login, JWT in `EncryptedSharedPreferences`), favorite cities, search history, saved unit preference
- ✅ Loading, error and empty states throughout

## 🛠️ Tech Stack

![Kotlin](https://img.shields.io/badge/Kotlin%202-7F52FF?style=flat&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=flat&logo=jetpackcompose&logoColor=white)
![Material 3](https://img.shields.io/badge/Material%203-757575?style=flat&logo=materialdesign&logoColor=white)
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
│   └── auth/          # EncryptedSharedPreferences-backed token storage
├── ui/                # one ViewModel (StateFlow) + Composable screen per feature:
│                       # auth, dashboard (search, weather card, cache badge, fallback banner, forecast chart),
│                       # favorites, history, compare, settings
└── di/                 # Hilt modules
```

### Why these choices

- **Direct-to-API, no BFF**: like the iOS client (and unlike the web client, which proxies through Next.js to keep the JWT out of browser JS), `EncryptedSharedPreferences` is already a secure, sandboxed place to hold a token on-device — no need for a server-side proxy layer.
- **`10.0.2.2` instead of `localhost`**: the Android emulator runs in its own network namespace: `10.0.2.2` is Google's documented alias back to the host machine's `localhost`. A `network_security_config.xml` cleartext exception is needed for it too, since Android blocks plaintext HTTP by default since API 28.
- **Hand-rolled Canvas charts over a charting library**: there's no Compose charting library as mature as Swift Charts/Recharts; for a simple hourly-line/daily-bar chart, a small custom `Canvas` composable is less risk than pulling in and learning a third-party dependency (KISS/YAGNI).
- **Local-datetime forecast parsing**: `hourly[].time`/`daily[].date` come back from the API without a timezone offset (Open-Meteo's `timezone=auto` already localizes them), so they're parsed as `kotlinx.datetime.LocalDateTime`/`LocalDate`, not `Instant`.

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

- JSON parsing fixtures for `WeatherResponse`/`ForecastResponse` (pinning the local-datetime hourly/daily parsing), `CompareResponse`, `Units`.
- `MockWebServer`-backed test of the API client's error-body parsing (non-2xx → typed `ApiException` carrying the backend's `message`).
- Pure-function unit tests for cache-age formatting, fallback-provider detection, and the cross-provider average-temperature calculation.

Given the project's scope (three client apps on one backend), test effort is weighted toward parsing/business-logic rather than Compose UI layout.

## 📝 Notes

- No delete-favorite/clear-history UI — matches the backend's intentional v1 scope (no delete endpoints exist yet).
- Requires the backend reachable at `http://10.0.2.2:8080` from the emulator; on a physical device, point it at the host machine's LAN IP instead.

## 📄 License

MIT.

---

Developed by **David Arsénio Martins**
🌐 [ividi.dev](https://ividi.dev/) · 💻 [github.com/VidiPT89](https://github.com/VidiPT89/)
