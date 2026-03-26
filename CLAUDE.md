# CLAUDE.md

Этот файл содержит инструкции для Claude Code (claude.ai/code) при работе с кодом в данном репозитории.

## Команды сборки

```bash
# Сборка Android
./gradlew :composeApp:assembleDebug

# Сборка iOS фреймворка
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64

# Очистка и пересборка
./gradlew clean build

# Запуск тестов
./gradlew test

# Запуск тестов конкретного модуля
./gradlew :features:card-feature:test
./gradlew :core:network:test
```

Для разработки iOS-приложения откройте `iosApp/` в Xcode после сборки фреймворка.

## Обзор архитектуры

Это проект на **Kotlin Multiplatform** (KMP), нацеленный на Android и iOS, с использованием **Compose Multiplatform** для общего UI.

### Структура модулей

```
├── composeApp/          # Основная точка входа приложения
├── core/                # Общие инфраструктурные модули
│   ├── database/        # Room база данных (KSP для кодогенерации)
│   ├── network/         # Ktor HTTP клиент
│   └── storage/         # Хранилище токенов (multiplatform-settings)
├── features/            # Модули фич по принципам Clean Architecture
│   ├── card-feature/    # Корневая навигация и оркестрация главного экрана
│   ├── wardrobe-feature/# Управление гардеробом/одеждой (домен pincard)
│   ├── authorization/   # Флоу авторизации
│   ├── profile/         # Профиль пользователя
│   ├── recommendations-page-screen/
│   └── tiktok-feed/
└── iosApp/              # Точка входа iOS-приложения (SwiftUI)
```

### Паттерн модуля фичи

Каждая фича следует **Clean Architecture** с разделением на internal/external:

```
feature/
├── di/              # Определение Koin-модуля
├── internal/
│   ├── data/        # Реализации репозиториев, DTO
│   ├── domain/      # Интерфейсы репозиториев, use cases
│   └── presentation/# ViewModels, экраны, composables
└── external/        # Публичные API, доступные другим модулям
```

### Внедрение зависимостей

Используется **Koin** для DI. Каждая фича предоставляет Koin-модуль:
- `cardModule` (card-feature) — корневой модуль, включающий остальные
- `pinCardModule` (wardrobe-feature)
- `authorizationModule()` (authorization)
- `profileModule` (profile)

Инициализация Koin происходит в `App.kt` через `initKoin()`.

### Навигация

Используется **Jetpack Navigation Compose** (multiplatform):
- `AppNavHost` управляет корневой навигацией (Authorization → Main)
- `BottomNavigationNavHost` управляет навигацией по вкладкам внутри Main
- Deep linking поддерживается через `DeepLinkManager`

### Основной технологический стек

- **UI**: Compose Multiplatform
- **DI**: Koin 4.x
- **Сеть**: Ktor 3.x с kotlinx.serialization
- **Локальное хранилище**: Room (база данных), multiplatform-settings (настройки)
- **Загрузка изображений**: Coil 3.x
- **Асинхронность**: Kotlin Coroutines

### Платформенные наборы исходников

- `commonMain/` — общий код (UI, бизнес-логика)
- `androidMain/` — Android-специфичные реализации
- `iosMain/` — iOS-специфичные реализации (Ktor Darwin engine)

## Правила написания кода

**ВАЖНО:** При написании кода обязательно следуй правилам и паттернам, описанным в документации:

- **`docs/CODE_STYLE.md`** - правила стиля кода:
  - Запрет вызова функций через полный путь (fully qualified name)
  - Использование `import as` при конфликтах имён

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

- **`docs/RESOURCES.md`** - работа с ресурсами (moko-resources):
  - Структура модуля `core/resources` (images, strings, fonts)
  - Форматы файлов: SVG для векторов, PNG@1x/2x/3x для растра
  - Использование `MR.images.*` вместо Material Icons
  - Подключение модуля к фичам

- **`docs/API.md`** - работа с сетевыми запросами и API

### Ключевые правила

1. **UI код**: Всегда используй паттерны из `docs/COMPOSE.md`
2. **Навигация**: Следуй структуре из `docs/NAVIGATION.md`
3. **Новые фичи**: Создавай по шаблону из Feature Module Pattern выше
4. **Состояние**: ViewModel + MutableStateFlow + collectAsState()
5. **DI**: Регистрируй зависимости в Koin модулях
6. **ЗАПРЕЩЕНО использовать Material Icons** (`androidx.compose.material.icons`, `Icons.Default.*`, `Icons.Filled.*` и т.д.). Все иконки берутся **только** из `MR.images.*` через moko-resources (модуль `core:resources`). См. `docs/RESOURCES.md`
7. **Исследование зависимостей**: Используй `./ksrc.exe` (в корне проекта) для изучения исходников Kotlin/Gradle библиотек:
   - `./ksrc.exe deps --offline` — список доступных зависимостей с исходниками
   - `./ksrc.exe cat "group:artifact:version!/commonMain/path/File.kt" --lines 1,100` — чтение файла с исходниками
