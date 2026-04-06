# Деталка образа (OutfitDetailScreen)

## Модуль

`features/outfit` — входит в тот же модуль что и экран Образы.

## Файлы

```
features/outfit/src/commonMain/.../
├── internal/presentation/
│   └── detail/
│       ├── interactionModel/
│       │   ├── OutfitDetailState.kt
│       │   ├── OutfitDetailIntent.kt
│       │   └── OutfitDetailSideEffect.kt
│       ├── OutfitDetailStore.kt
│       ├── OutfitDetailViewModel.kt
│       └── OutfitDetailScreen.kt
└── external/
    └── OutfitRoutes.kt  # OutfitDetail маршрут
```

## Описание экрана

Открывается по тапу на карточку образа.  
Показывает коллаж из вещей образа (flat lay), имя, теги, список вещей.

### Структура экрана

```
← (назад)         [Образ]         ⋯ (меню)
─────────────────────────────────────────────────
[Коллаж flat lay — вещи разложены по холсту 3:4]

[Название образа]
[тег1] [тег2] [тег3]

── Вещи в образе ────────────────────────────────
[Карточка вещи 1]   [Карточка вещи 2]
[Карточка вещи 3]   ...
─────────────────────────────────────────────────
```

### Коллаж (flat lay)

Вещи раскладываются вертикально на белом (light) / тёмно-сером (dark) холсте:
- 1 вещь: центр, 60% ширины
- 2 вещи: сверху и снизу, ~55% ширины
- 3 вещи: верх/центр/низ, уменьшение от 52% до 44%
- 4+ вещей: каскадом вниз

### Меню `⋯` (MoreMenu)

```
- Редактировать  →  OutfitConstructorRoute(lookId = id)
- В избранное
- Поделиться     →  shareLook(id) → копирует URL
- Удалить        →  confirm dialog → deleteLook(id) → back
```

### Карточки вещей в образе

Сетка 2 колонки:
- Фото 1:1, Имя, Тег категории
- По тапу → `ItemDetail(clotheId)`

---

## FlowMVI

### OutfitDetailState

```kotlin
data class OutfitDetailState(
    val look: Look? = null,
    val isLoading: Boolean = true,
) : MVIState
```

### OutfitDetailIntent

```kotlin
sealed interface OutfitDetailIntent : MVIIntent {
    data class Load(val lookId: Int) : OutfitDetailIntent
    data object MenuClicked : OutfitDetailIntent
    data object EditClicked : OutfitDetailIntent
    data object DeleteClicked : OutfitDetailIntent
    data object DeleteConfirmed : OutfitDetailIntent
    data object ShareClicked : OutfitDetailIntent
    data object FavoriteClicked : OutfitDetailIntent
    data class ItemClicked(val clotheId: Int) : OutfitDetailIntent
}
```

### OutfitDetailSideEffect

```kotlin
sealed class OutfitDetailSideEffect {
    data class NavigateToConstructor(val lookId: Int) : OutfitDetailSideEffect()
    data class NavigateToItem(val clotheId: Int) : OutfitDetailSideEffect()
    data object NavigateBack : OutfitDetailSideEffect()
    data class ShowError(val message: String) : OutfitDetailSideEffect()
}
```

---

## Data Layer

DTO и маппер для `Look` / `LookItem` — определены в `03_outfits_screen.md`.

### Use Cases

```kotlin
// internal/domain/usecase/GetLookByIdUseCase.kt
class GetLookByIdUseCase(private val repository: OutfitRepository) {
    suspend operator fun invoke(lookId: Int): Result<Look> =
        runCatching { repository.getLookById(lookId) }
}

// internal/domain/usecase/DeleteLookUseCase.kt  — см. 03_outfits_screen.md
// internal/domain/usecase/ShareLookUseCase.kt   — см. 03_outfits_screen.md
```

---

## Навигационные маршруты

```kotlin
// external/OutfitRoutes.kt
@Serializable object OutfitMain
@Serializable data class OutfitDetail(val lookId: Int)
```

## Используемые системные компоненты (core/compose/components)

- `AppTopBar` — шапка с кнопкой назад, заголовком, меню
- `MoreMenu` — выпадающее меню `⋯`
- `ItemCard` — карточка вещи в образе (mini)
- `TagChip` — чип-тег
