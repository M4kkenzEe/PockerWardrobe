# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build Android
./gradlew :composeApp:assembleDebug

# Build iOS framework
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64

# Clean and rebuild
./gradlew clean build

# Run tests
./gradlew test

# Run specific module tests
./gradlew :features:card-feature:test
./gradlew :core:network:test
```

For iOS app development, open `iosApp/` in Xcode after building the framework.

## Architecture Overview

This is a **Kotlin Multiplatform** (KMP) project targeting Android and iOS, using **Compose Multiplatform** for shared UI.

### Module Structure

```
├── composeApp/          # Main application entry point
├── core/                # Shared infrastructure modules
│   ├── database/        # Room database (KSP for codegen)
│   ├── network/         # Ktor HTTP client
│   └── storage/         # Token storage (multiplatform-settings)
├── features/            # Feature modules following Clean Architecture
│   ├── card-feature/    # Root navigation and main screen orchestration
│   ├── wardrobe-feature/# Wardrobe/clothing management (pincard domain)
│   ├── authorization/   # Auth flow
│   ├── profile/         # User profile
│   ├── recommendations-page-screen/
│   └── tiktok-feed/
└── iosApp/              # iOS application entry point (SwiftUI)
```

### Feature Module Pattern

Each feature follows **Clean Architecture** with internal/external visibility:

```
feature/
├── di/              # Koin module definition
├── internal/
│   ├── data/        # Repository implementations, DTOs
│   ├── domain/      # Repository interfaces, use cases
│   └── presentation/# ViewModels, screens, composables
└── external/        # Public APIs exposed to other modules
```

### Dependency Injection

Uses **Koin** for DI. Each feature exposes a Koin module:
- `cardModule` (card-feature) - root module that includes others
- `pinCardModule` (wardrobe-feature)
- `authorizationModule()` (authorization)
- `profileModule` (profile)

Koin initialization happens in `App.kt` via `initKoin()`.

### Navigation

Uses **Jetpack Navigation Compose** (multiplatform):
- `AppNavHost` handles root navigation (Authorization → Main)
- `BottomNavigationNavHost` handles tab navigation within Main
- Deep linking supported via `DeepLinkManager`

### Key Technical Stack

- **UI**: Compose Multiplatform
- **DI**: Koin 4.x
- **Networking**: Ktor 3.x with kotlinx.serialization
- **Local Storage**: Room (database), multiplatform-settings (preferences)
- **Image Loading**: Coil 3.x
- **Async**: Kotlin Coroutines

### Platform Source Sets

- `commonMain/` - Shared code (UI, business logic)
- `androidMain/` - Android-specific implementations
- `iosMain/` - iOS-specific implementations (Ktor Darwin engine)
