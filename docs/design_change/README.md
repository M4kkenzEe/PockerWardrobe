# Design Change Documentation

Документация по изменениям дизайна и архитектуры на основе референса `F:\Work\startup\clothis_design`.

## Источники

- Дизайн: `F:\Work\startup\clothis_design` (React/TypeScript прототип)
- Основные заметки по продукту: `keynotes.md` (в папке дизайна)

## Архитектурные изменения

Ключевые решения принятые для нового цикла разработки:

- **FlowMVI**: добавляется как основной MVI-фреймворк для всех фич
- **Модульность**: каждая фича в своём Gradle-модуле по Clean Architecture
- **Navigation 3**: замена текущей навигации на `androidx.navigation3` (multiplatform-ready)
- **Точка входа**: единый `AppNavHost` (для поддержки deeplinks), `BottomSheetNavigation` удаляется
- **Deeplinks**: выносятся в `core/deeplink` модуль
- **Platform.kt**: выносится в `core` модуль
- **App.kt**: выносится в фичу `main` (главный модуль-оркестратор)
- **Переименование**: `composeApp` → `androidApp` (точка входа Android-платформы)
- **Удаляется**: `tiktok-feed` модуль полностью; `card-feature` модуль (переносится по назначению)
- **Не используется**: `recommendations-page-screen` — оставить, не трогать

## Новая структура модулей

```
├── androidApp/              # (бывший composeApp) — точка входа Android
├── iosApp/                  # Точка входа iOS (SwiftUI)
├── core/
│   ├── compose/             # Theme, системные компоненты (components/)
│   ├── database/            # Room база данных
│   ├── deeplink/            # DeepLinkManager (перенесён из card-feature)
│   ├── network/             # Ktor HTTP клиент
│   ├── resources/           # moko-resources (images, colors, strings, fonts)
│   └── storage/             # multiplatform-settings
└── features/
    ├── main/                # App.kt, RootScreen, BottomNavigation (перенесено из card-feature)
    │   └── navigation/      # BottomNavigationNavHost
    ├── authorization/       # Флоу авторизации (без изменений)
    ├── home/                # (бывший Home в card-feature) — лента/рекомендации
    ├── outfit/              # Образы + деталка образа (бывший wardrobe-feature/Looks)
    ├── outfit_constructor/  # Конструктор образа (бывший wardrobe-feature/LookConstructor)
    ├── wardrobe/            # Гардероб + деталка вещи (бывший wardrobe-feature/Wardrobe)
    ├── profile/             # Профиль + Размеры + Редактирование профиля + Настройки
    └── recommendations-page-screen/  # Оставить как есть, не используется
```

## Статус экранов

| Экран | Модуль | Файл | Статус |
|---|---|---|---|
| Гардероб (Одежда) | `features/wardrobe` | `WardrobeScreen.kt` | Перенос + доработка |
| Деталка вещи | `features/wardrobe` | `ItemDetailScreen.kt` | Новый экран |
| Редактирование вещи | `features/wardrobe` | `ItemEditScreen.kt` | Новый экран |
| Образы (Looks) | `features/outfit` | `OutfitScreen.kt` | Перенос + доработка |
| Деталка образа | `features/outfit` | `OutfitDetailScreen.kt` | Перенос + доработка |
| Конструктор образа | `features/outfit_constructor` | `OutfitConstructorScreen.kt` | Перенос + доработка |
| Профиль | `features/profile` | `ProfileScreen.kt` | Доработка |
| Редактирование профиля | `features/profile` | `EditProfileScreen.kt` | Новый экран |
| Размеры | `features/profile` | `SizesScreen.kt` | Новый экран |

## Содержание

1. [Экран Гардероб](./01_wardrobe_screen.md) — вкладка "Одежда"
2. [Деталка и редактирование вещи](./02_item_screens.md) — просмотр и редактирование
3. [Экран Образы](./03_outfits_screen.md) — вкладка "Образы"
4. [Деталка образа](./04_outfit_detail_screen.md)
5. [Конструктор образа](./05_outfit_constructor.md)
6. [Профиль](./06_profile_screen.md)
7. [Размеры](./07_sizes_screen.md)
8. [Изменения Backend](./08_backend_changes.md)
9. [Изменения навигации](./09_navigation.md)
10. [Системные компоненты](./10_system_components.md)
11. [Clean Architecture + FlowMVI](./11_architecture_flowmvi.md)
