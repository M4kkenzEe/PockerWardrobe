# Проблемы в проекте

## 1. Проблемы с State Management в ViewModels

### AuthorizationViewModel
**Файл:** `features/authorization/src/commonMain/kotlin/.../AuthorizationViewModel.kt`

| Проблема | Описание |
|----------|----------|
| `errorState` не сбрасывается | После ошибки `errorState` остаётся `true` навсегда, UI будет показывать ошибку даже после повторной попытки |
| Race condition | При повторных вызовах `loginUser`/`registerUser` предыдущие запросы не отменяются, могут перезаписать результат |
| Неконсистентные состояния | Три отдельных `StateFlow` (`viewState`, `errorState`, `isSessionOpen`) вместо единого sealed class - может привести к невозможным комбинациям состояний |

### ProfileViewModel
**Файл:** `features/profile/src/commonMain/kotlin/.../ProfileViewModel.kt`

| Проблема | Описание |
|----------|----------|
| Нет loading state | UI не знает, когда данные загружаются |
| Нет error state | Ошибки загрузки профиля игнорируются |
| Неоднозначный null | `profileState: User?` - null может означать "загрузка", "ошибка" или "нет данных" |

### FeedViewModel
**Файл:** `features/tiktok-feed/src/commonMain/kotlin/.../FeedViewModel.kt`

| Проблема | Описание |
|----------|----------|
| Пустой класс | Полностью пустая реализация - возможно неиспользуемый класс |

### ConstructorViewModel
**Файл:** `features/wardrobe-feature/src/commonMain/kotlin/.../ConstructorViewModel.kt`

| Проблема | Строка | Описание |
|----------|--------|----------|
| Не потокобезопасные переменные | 30-31 | `maxZIndex` и `lastInteractedPhotoId` - примитивы без синхронизации, используемые из корутин |
| Ошибки не обрабатываются | 94-105 | В методе `save()` ошибки обрабатываются только комментариями `// Показать ошибку пользователю` |
| Нет loading state | - | Нет индикации процесса сохранения |
| Force unwrap | 49 | `clothe.id!!` может вызвать NPE |

### LookDetailsViewModel
**Файл:** `features/wardrobe-feature/src/commonMain/kotlin/.../LookDetailsViewModel.kt`

| Проблема | Описание |
|----------|----------|
| Нет error state | Ошибки только логируются через `println`, UI не информируется |
| `addToWardrobe` без состояний | Нет loading/success/error state для операции добавления |

### LooksViewModel
**Файл:** `features/wardrobe-feature/src/commonMain/kotlin/.../LooksViewModel.kt`

| Проблема | Строка | Описание |
|----------|--------|----------|
| Нет loading/error states | - | UI не знает о состоянии загрузки и ошибках |
| Debug код в продакшене | 16, 21-23 | `println` с emoji логами `🎯` остались в коде |
| Callback на IO диспетчере | 44 | `onSuccess` в `shareLook` вызывается на IO диспетчере - проблема если callback обращается к UI |

### WardrobeViewModel
**Файл:** `features/wardrobe-feature/src/commonMain/kotlin/.../WardrobeViewModel.kt`

| Проблема | Строка | Описание |
|----------|--------|----------|
| Нет loading/error states | - | UI не информируется о состоянии |
| Debug код в продакшене | 25-27, 29-30 | `println` с emoji логами остались |
| Race condition | 35-39, 42-53 | `loadClothe` и `uploadFromUrl` могут выполняться одновременно и перезаписывать данные |

---

## 2. Магические числа и строки

### Захардкоженные API endpoints и URLs

| Файл | Строка | Проблема |
|------|--------|----------|
| `core/network/.../ServerConfig.kt` | 21 | `SERVER_HOST = "194.87.190.248:80"` - IP сервера захардкожен |
| `features/authorization/.../AuthServiceImpl.kt` | 71-72 | Endpoint пути `"register"`, `"login"` |
| `features/wardrobe-feature/.../LookRepositoryImpl.kt` | 168-173 | Endpoints: `"looks"`, `"/uploadImage"`, etc. |
| `features/wardrobe-feature/.../WardrobeRepositoryImpl.kt` | 115-116 | Endpoints: `"clothes"`, `"/from_url"` |

### Захардкоженные имена баз данных

| Файл | Проблема |
|------|----------|
| `core/database/src/iosMain/.../Database.kt:8` | `"my_room.db"` |
| `core/database/src/androidMain/.../Database.kt:9` | `"my_room.db"` |

### Дублирование цветов (нет Design System)

Цвета дублируются вместо использования общих констант:

| Файл | Строки | Цвета |
|------|--------|-------|
| `features/wardrobe-feature/.../LookConstructor.kt` | 64-67 | `BG_GREY_COLOR`, `BLUE_COLOR`, `GREY_COLOR`, `CAPTURE_ZONE_BG` |
| `features/authorization/.../Colors.kt` | 5-8 | Те же цвета продублированы |
| `features/tiktok-feed/.../FeedScreen.kt` | 77 | `Color(0xFFfdf3df)` inline |
| `features/wardrobe-feature/.../LookConstructor.kt` | 439 | `Color(0xFFF5F5F5)` inline |

### Захардкоженные размеры и отступы

| Файл | Строка | Значение |
|------|--------|----------|
| `features/recommendations-page-screen/.../Stories.kt` | 41 | `.size(72.dp)` |
| `features/recommendations-page-screen/.../OutfitsGrid.kt` | 24-25, 32 | `.width(160.dp)`, `RoundedCornerShape(20)`, `.padding(vertical = 100.dp)` |
| `features/recommendations-page-screen/.../CategoriesCards.kt` | 34, 44-45 | `spacedBy(16.dp)`, `.size(170.dp)`, `RoundedCornerShape(16.dp)` |
| `features/wardrobe-feature/.../LookDetailsScreen.kt` | 277 | `200.dp` magic number в расчёте высоты |

### Захардкоженные размеры шрифтов

Размеры шрифтов разбросаны по файлам без системы:
- `AuthorizationScreen.kt`: 28sp, 20sp, 14sp, 16sp
- `LookConstructor.kt`: 24sp, 28sp, 20sp, 16sp, 32sp
- `CategoriesCards.kt`: 16sp
- `Wardrobe.kt`: 32sp
- `TiktokScreen.kt`: 18sp, 14sp, 12sp
- `LookDetailsScreen.kt`: 18sp, 14sp, 20sp

### Захардкоженные UI строки

| Файл | Строка | Текст |
|------|--------|-------|
| `features/wardrobe-feature/.../Wardrobe.kt` | 81 | `"WB"` - логотип |
| `features/wardrobe-feature/.../Dialogs.kt` | 51, 69, 84 | `"Select an Image Source"`, `"Camera"`, `"Gallery"` |
| `features/wardrobe-feature/.../AddClotheFloatButton.kt` | 81-82 | `"Settings"`, `"Cancel"` |
| `features/wardrobe-feature/.../Wardrobe.kt` | 103 | `"Import"` |

### Другие магические значения

| Файл | Строка | Проблема |
|------|--------|----------|
| `features/tiktok-feed/.../FeedScreen.kt` | 43-70 | Random ranges `-300..500`, size `200` |
| `core/storage/.../TokenStorageImpl.kt` | 11 | `"jwt_token"` key |
| `features/wardrobe-feature/.../Utils.kt` | 11-14 | Захардкоженные `"0.0.0.0:8080"`, `"localhost:8080"` |
| `features/wardrobe-feature/.../SharedImage.kt` | 12-13 | Compression quality `100` |
| `features/wardrobe-feature/.../LookRepositoryImpl.kt` | 70-71 | `"image/jpeg"` content type |
| `features/wardrobe-feature/.../WardrobeRepositoryImpl.kt` | 76 | `"clothing_image.png"` filename |
| `features/wardrobe-feature/.../LookRepositoryImpl.kt` | 38 | `.take(20)` token preview length |

---

## 3. Потенциальные утечки памяти

### Критические (исправить немедленно)

#### HTTP Client не кэшируется
**Файл:** `core/network/src/commonMain/kotlin/.../NetworkClient.kt:12-27`

`HttpClient` создаётся при каждом вызове `getClient()` без кэширования. HttpClient держит ресурсы и пулы соединений.

**Также:** `features/wardrobe-feature/.../WardrobeRepositoryImpl.kt:29-30` создаёт новую ссылку на клиент при каждом создании репозитория.

#### Callbacks в корутинах ViewModel
**Файлы:**
- `LooksViewModel.kt:38-49` - `shareLook` принимает callback `onSuccess: (String) -> Unit`
- `ConstructorViewModel.kt:82-115` - `save()` принимает callback `onSuccess: () -> Unit`

Callbacks из корутин могут держать ссылки на уничтоженные composables.

### Высокий приоритет

#### Permission callbacks без lifecycle cleanup
**Файлы:**
- `features/wardrobe-feature/.../ImagePickerApp.kt:39-59, 70-77`
- `features/wardrobe-feature/.../AddClotheFloatButton.kt:37-57, 59-66`

`PermissionCallback` создаётся inline без proper lifecycle management.

#### DisposableEffect с пустым onDispose
**Файл:** `features/wardrobe-feature/.../Looks.kt:32-35`

```kotlin
DisposableEffect(Unit) {
    viewModel.getLooks()
    onDispose { } // Пустой cleanup!
}
```

### Средний приоритет

#### Context references
**Файлы:**
- `features/wardrobe-feature/.../PermissionManager.kt:29-62` - `lifecycleScope.launch()` может держать ссылки
- `features/wardrobe-feature/.../ShareManager.kt:12, 32-34` - прямая ссылка на Context

#### Singleton DeepLinkManager
**Файлы:**
- `features/card-feature/src/androidMain/.../DeepLinkManager.kt:25-38`
- `features/card-feature/src/iosMain/.../DeepLinkManager.kt:23-26`

Статический singleton с `MutableStateFlow` - может накапливать ссылки.

---

## 4. Неиспользуемый код

| Файл | Проблема |
|------|----------|
| `features/tiktok-feed/.../FeedViewModel.kt` | Полностью пустой класс |

---

## Рекомендации

### State Management
1. Использовать sealed class для состояний вместо множества отдельных StateFlow
2. Добавить loading/error states во все ViewModels
3. Сбрасывать errorState после обработки ошибки
4. Отменять предыдущие запросы при новых (Job cancellation)

### Design System
1. Создать централизованный модуль с цветами, размерами, шрифтами
2. Вынести UI строки в ресурсы для i18n
3. Создать константы для всех magic numbers

### Memory Management
1. Сделать HttpClient singleton
2. Заменить callbacks на StateFlow для результатов операций
3. Добавить proper cleanup в DisposableEffect
4. Использовать WeakReference или proper scoping для callbacks
