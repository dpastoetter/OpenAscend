# OpenAscend

OpenAscend is an Android app that frames daily habits and check-ins as a light RPG-style progression loop: onboarding, morning and evening flows, a character sheet with stats and XP, habit tracking, and shareable recap cards. It is built with Kotlin, Jetpack Compose, Room, DataStore, and Hilt.

**Repository:** [github.com/dpastoetter/OpenAscend](https://github.com/dpastoetter/OpenAscend)

## Features

- **Onboarding** — Set a hero name and initial quest goals to start your run.
- **Daily flows** — Morning overview and evening check-in style surfaces to anchor the day.
- **Character & progression** — Level, XP, and stat-style metrics tied to your activity.
- **Habits** — Create and manage habits; edit flows are integrated in the app shell.
- **Profile** — Optional profile image (camera/gallery) stored on device.
- **Appearance** — Light/dark (or system) theme preference persisted locally.
- **Share** — Generate bitmap recap cards for sharing (via Android share sheet where supported).

Data is stored on the device (Room, DataStore). There is no bundled cloud sync in this MVP.

## Screenshots

Representative UI (documentation mockups):

| Onboarding | Home overview | Character sheet |
|------------|---------------|------------------|
| ![Onboarding](docs/screenshots/onboarding.png) | ![Home](docs/screenshots/home.png) | ![Character](docs/screenshots/character.png) |

For pixel-accurate captures from a running build, use an emulator or device and:

```bash
adb exec-out screencap -p > shot.png
```

## Project structure

| Module | Role |
|--------|------|
| `:app` | Android application, Compose UI, navigation, Hilt wiring |
| `:core:domain` | Domain models and use-case style logic (pure Kotlin) |
| `:core:data` | Persistence (Room), repositories, DataStore preferences |

Versioning (debug build): see `versionName` / `versionCode` in `app/build.gradle.kts`. Package id: `com.openascend.app`. **minSdk 26**, **targetSdk / compileSdk 35**.

## Tech stack

- Kotlin, Coroutines
- Jetpack Compose, Material 3
- Room, DataStore
- Hilt (dependency injection)
- Coil (image loading)
- Gradle with Kotlin DSL, version catalogs (`gradle/libs.versions.toml`)

## Requirements

- **JDK 17** (Gradle uses the toolchain declared in the build scripts)
- **Android SDK** with API 35 for builds; **platform tools** (`adb`) for installing APKs on hardware or emulators

## Build

```bash
./gradlew :app:assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

Install on a connected device or emulator:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Tests

```bash
./gradlew :core:domain:test
./gradlew :core:data:testDebugUnitTest
./gradlew :app:testDebugUnitTest
```

Or run the same set the CI job uses:

```bash
./gradlew :core:domain:test :core:data:testDebugUnitTest :app:testDebugUnitTest :app:assembleDebug
```

### Continuous integration

[`.github/workflows/ci.yml`](.github/workflows/ci.yml) runs on pushes to `main`/`master` and on pull requests:

- **build** — Domain, data, and app unit tests (including Compose/Robolectric where configured) plus `assembleDebug`; uploads the debug APK as a workflow artifact.
- **instrumented** — `connectedDebugAndroidTest` on an API 29 x86_64 emulator (smoke / integration coverage).

## Emulator (optional)

```bash
./scripts/run-emulator.sh
```

The script documents AVD locations (including Flatpak Android Studio on Linux), lock-file cleanup, and GPU options when the default emulator path misbehaves on some distros.

## Contributing

Issues and pull requests are welcome. Please keep changes focused and match existing Kotlin/Compose style. Run the unit test tasks above before opening a PR; CI will run the full matrix.

## License

OpenAscend is released under the [MIT License](LICENSE).
