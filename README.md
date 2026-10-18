# A-RAICE Android App

**A-RAICE** (Autonomous Rice Disease Identification and Outbreak Tracking) is a native Android application designed for farmers and agricultural/LGU staff in Bataan, Philippines. It provides AI-assisted rice disease detection, outbreak monitoring, disease encyclopedia with actionable treatment protocols, alerts broadcasting, and localized farm mapping.

---

## Technical Stack & Architecture

- **Language:** Kotlin
- **Architecture:** Fragments + Jetpack Navigation component, MVVM / Repository pattern, ViewBinding
- **Networking:** Retrofit 2 + OkHttp 4 + Gson converter
- **Camera & Imagery:** CameraX (Camera2 / View / Lifecycle), Glide 4
- **Location:** Google Play Services Location (`FusedLocationProviderClient`)
- **Mapping:** Custom lightweight Canvas-based `BataanMapView` parsing bundled GeoJSON polygon data (`app/src/main/assets/bataan_municipalities.geojson`) with pinch-zoom, pan, point-in-polygon selection, and dark mode palette support (no external Google Maps API key required).
- **Target Platforms:** `minSdk = 26` (Android 8.0 Oreo), `targetSdk = 36`

---

## Roles & Access Control

- **FARMER:**
  - Registration with municipality selection, barangay, contact number, and geolocated farm coordinates.
  - Image capture / upload scan flow with disease prediction, confidence score, and top-3 differential diagnoses.
  - Scan history with infinite scrolling pagination.
  - Interactive municipality disease map showing farm position and local active outbreaks.
  - Disease encyclopedia with offline caching and bundled fallback.
  - Outbreak alerts with read/unread tracking.
  - Profile management and password updates.
- **ADMIN / LGU STAFF:**
  - Province-wide dashboard tracking active outbreaks, scan metrics, and recent activities.
  - Filterable scan inspection across all farmers.
  - Outbreak resolution and management (`PATCH /outbreaks/{id}/close`).
  - Broadcast notifications targeting all farmers or specific municipalities.
  - Consolidated report generation and native Android sharing sheet integration.
  - Farmer records directory with tap-to-call direct contact support.

---

## Setup & Prerequisites

1. **Java Development Kit (JDK):** JDK 17 or higher (tested with JDK 26 / 21 / 17).
2. **Android SDK:** Platforms up to API 36, Build-Tools 36.x.
3. **Android Studio:** Ladybug / Meerkat or modern CLI build environment.

---

## Configuration

The backend API base URL is defined in `app/build.gradle.kts`:

```kotlin
buildConfigField("String", "API_BASE_URL", "\"https://a-raice-web-production-be92.up.railway.app/api/\"")
```

The app handles automatic authentication tokens, session restoration, and a global 401 interceptor that safely navigates back to the login screen when sessions expire.

---

## Building the Project

### 1. Build Debug APK
```bash
./gradlew assembleDebug
```
Output APK location: `app/build/outputs/apk/debug/app-debug.apk`

### 2. Run JVM Unit Tests
```bash
./gradlew testDebugUnitTest
```
Tests verify API contract deserialization for:
- Authentication & user profile responses
- Paginated scan results & scan detail with top-3 predictions
- Paginated alerts and unread filters
- GeoJSON and map endpoints (municipalities, outbreaks, farms)
- Outbreaks and resolution responses
- Reports generation and disease breakdown models
- Diseases encyclopedia listings and detailed treatments

### 3. Run Android Lint
```bash
./gradlew lintDebug
```
Generates HTML and SARIF reports under `app/build/reports/`.

---

## Release Build & Signing Configuration

Release builds enforce code minification (`isMinifyEnabled = true`) and resource shrinking (`isShrinkResources = true`) with tailored ProGuard rules preserving Gson serialization models and Retrofit interfaces.

To sign release builds, create a `keystore.properties` file in the root directory (ignored by git):

```properties
storeFile=path/to/your-release-key.jks
storePassword=your_keystore_password
keyAlias=your_key_alias
keyPassword=your_key_password
```

Build the release APK:
```bash
./gradlew assembleRelease
```
*Note: If `keystore.properties` is absent, the build configuration falls back to the debug signing configuration so release assembly completes successfully in CI/testing environments.*

Output APK location: `app/build/outputs/apk/release/app-release.apk`
