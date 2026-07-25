# LifeOS Mobile

Kotlin Multiplatform + Compose Multiplatform client. See
[docs/12-project-architecture.md](../docs/12-project-architecture.md#mobile-architecture)
for the architecture this module implements and
[docs/13-folder-structure.md](../docs/13-folder-structure.md#mobile) for the
package layout.

## Status

This is an infrastructure bootstrap: DI (Koin), networking (Ktor), local
storage (DataStore), the design system (Material 3 theme + reusable
components), and navigation (Compose Navigation) are wired and compilable.
No feature screens, ViewModels, or repositories exist yet — `App.kt` renders
a single placeholder destination to prove the wiring works end to end.

## Getting Started

1. Install a recent stable Android Studio (Koala or newer) with the Kotlin
   Multiplatform plugin, and Xcode if building the iOS target.
2. This project does not include a checked-in Gradle wrapper JAR. Generate
   one before the first build:
   ```
   gradle wrapper --gradle-version 8.10
   ```
3. Open `mobile/` in Android Studio, let it sync, and run the `composeApp`
   Android configuration.

## Adding a Feature

Each feature (Authentication, Home Dashboard, AI Assistant, Travel, Planner,
Profile) has reserved, empty `presentation/domain/data` folders under
`composeApp/src/commonMain/kotlin/com/lifeos/app/features/<feature>/`. When
implementing one:

1. Add its Koin module under `features/<feature>/di/` and register it in
   `core/di/AppModule.kt#coreModules()`.
2. Build domain use cases and a repository interface in `domain/`, and the
   implementation (using the shared `HttpClient` and `PreferencesStorage`)
   in `data/`.
3. Build ViewModels and Composables in `presentation/`, registering routes
   from `core/navigation/Destination.kt` into `LifeOSNavHost.kt`.

See [docs/12-project-architecture.md#mobile-architecture](../docs/12-project-architecture.md#mobile-architecture)
for the full layering rules.
