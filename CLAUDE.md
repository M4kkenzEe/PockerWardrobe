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

## UI-тесты (Compose + Robolectric, без эмулятора)

Есть в `features/authorization` (`src/androidUnitTest/.../ui/`). Бегут вместе с unit-тестами модуля:

```bash
./gradlew :features:authorization:testDebugUnitTest
./gradlew :features:authorization:testDebugUnitTest --tests '*.ui.*'   # только UI-тесты
```

- **Первый запуск требует сети:** Robolectric скачивает `android-all-instrumented` (~150 МБ) в `~/.m2/repository/org/robolectric/`. Дальше — из кэша, офлайн.
- Конфиг: `src/androidUnitTest/resources/robolectric.properties` (SDK 34, экран 412×915dp — как у Samsung A53; на дефолтном 320×470 кнопки сплющиваются и клики мимо), `testOptions.unitTests.isIncludeAndroidResources = true`. Зависимости (`robolectric`, `ui-test-junit4`, `ui-test-manifest`) — только в `androidUnitTest`, версия `ui-test` = версия androidx `compose.ui`, которую тянет Compose Multiplatform.
- **Как добавить тест:** `@RunWith(RobolectricTestRunner::class) internal class ...`, `@get:Rule val rule = AuthScreenRule(ControllableAuthorizationRepository())`. Правило поднимает реальный `AuthorizationScreen`, VM получает через изолированный Koin-контекст (`KoinIsolatedContext`; глобальный `startKoin/stopKoin` в тестах не работает — Compose кэширует root scope между тестами) и переданный репозиторий: обычно управляемый fake из `AuthTestDoubles.kt`, для сквозной проверки текста — настоящий `AuthorizationRepositoryImpl` поверх `networkRepositoryOver(MockEngine)` (см. `LoginWithRealRepositoryUiTest`; ответ MockEngine приходит с потоков Ktor — ждать через `waitUntilExactlyOneExists`).
- Прод-проводку `authorizationModule()` проверяет `AuthorizationModuleTest` (резолвит репозиторий, use case, VM); UI-тесты её не видят.
- Элементы ищем по тегам из `internal object AuthTestTags` (`commonMain`); новый элемент экрана → новая константа + `Modifier.testTag(...)`. Тег поля висит на обёртке: вводить через `typeInto(tag, text)`, ошибку поля искать через `textInside(tag, text)`.
- Тексты в ассертах — из кода (`PASSWORD_RULE_ERROR`, `emailErrorMessage(...)`, `formatResendText(...)`, коды из `AuthService.kt`), не копипастой.
- Время виртуальное: корутины VM на `UnconfinedTestDispatcher`, таймеры двигает `rule.runPendingTimers()`. `Thread.sleep` и реальные задержки запрещены. После действия, за которым идёт проверка не-Compose состояния (`rule.isSessionOpened`, вызовы fake), — `compose.waitForIdle()`.

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

## Code Guidelines

**IMPORTANT:** При написании кода обязательно следуй правилам и паттернам, описанным в документации:

- **`docs/COMPOSE.md`** - правила верстки UI на Compose:
  - Использование AsyncImage из Coil для загрузки изображений
  - Паттерн ViewModel + StateFlow для управления состоянием
  - Передача событий через callback параметры
  - Запрет маппинга данных в Composable функциях
  - Создание Preview функций

- **`docs/NAVIGATION.md`** - архитектура навигации:
  - Трёхуровневая структура навигации (App → BottomNav → Feature)
  - Typed routes с @Serializable
  - Создание Navigation Graph для новых фич
  - Обработка Deep Links

- **`docs/API.md`** - работа с сетевыми запросами и API

### Ключевые правила

1. **UI код**: Всегда используй паттерны из `docs/COMPOSE.md`
2. **Навигация**: Следуй структуре из `docs/NAVIGATION.md`
3. **Новые фичи**: Создавай по шаблону из Feature Module Pattern выше
4. **Состояние**: ViewModel + MutableStateFlow + collectAsState()
5. **DI**: Регистрируй зависимости в Koin модулях
6. **Skeleton / Shimmer** — `SkeletonCard` + `rememberShimmerTranslation()` live in `design-system/.../components/SkeletonCard.kt`.
   - Base: `#26231F` (`surfaceElevated`), Highlight: `#322E2A` (OnCanvasMuted @9% alpha over base)
   - Duration: 1200 ms, LinearEasing, RepeatMode.Restart
   - Call `rememberShimmerTranslation()` once per grid/container, pass `shimmerTranslation: Float` to each `SkeletonCard` for a synchronized sweep.

8. **Floating bottom controls pattern** — `GlassIconButton` (`design-system/.../components/GlassIconButton.kt`):
   - Circular, diameter = `dimens.navIslandHeight` (64dp)
   - Material: `glassTint` background + 1dp `glassStroke` border + `CircleShape`
   - Icon: `navIslandIconSize` (28dp), color `onCanvasMuted`, `indication = null`
   - In `MainScreen.kt`, placed as siblings in a `Row` with `ClothisNavIsland(applyInsets = false)` between them; the Row owns `navigationBarsPadding` + `padding(bottom = navIslandBottomGap)`.

9. **Auto-hiding tab pill** (`WardrobeMainScreen.kt`):
   - `WardrobeSegmentedControl` (public, from `design-system`) floats at `Alignment.TopCenter` below status bar.
   - Hidden by default; shown via `AnimatedVisibility` (fadeIn/fadeOut 200ms).
   - Reveal/hide logic: `LaunchedEffect(pagerState)` + `snapshotFlow { isScrollInProgress to currentPage }.collectLatest { pillVisible = true; delay(3000); pillVisible = false }`.
   - 3s timer restarts on every swipe frame or page change; tapping a pill segment triggers `pagerState.animateScrollToPage()` which restarts it too.

10. **Исследование зависимостей**: Use `./ksrc.exe` (in project root) to explore Kotlin/Gradle library sources:
   - `./ksrc.exe deps --offline` — list available dependencies with sources
   - `./ksrc.exe cat "group:artifact:version!/commonMain/path/File.kt" --lines 1,100` — read source file
