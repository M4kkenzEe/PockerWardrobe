# Конструктор образа (OutfitConstructorScreen)

## Модуль

`features/outfit_constructor` (новый модуль, перенесён из `wardrobe-feature/LookConstructor.kt`)

## Файлы

```
features/outfit_constructor/src/commonMain/.../
├── di/OutfitConstructorModule.kt
├── internal/
│   ├── data/repository/OutfitConstructorRepositoryImpl.kt
│   ├── domain/
│   │   ├── repository/OutfitConstructorRepository.kt
│   │   └── usecase/AddLookUseCase.kt, GetClothesUseCase.kt
│   └── presentation/
│       └── root/
│           ├── interactionModel/
│           │   ├── OutfitConstructorState.kt
│           │   ├── OutfitConstructorIntent.kt
│           │   └── OutfitConstructorSideEffect.kt
│           ├── OutfitConstructorStore.kt
│           ├── OutfitConstructorViewModel.kt
│           └── OutfitConstructorScreen.kt
└── external/
    ├── OutfitConstructorRoutes.kt
    └── OutfitConstructorNavGraph.kt
```

## Описание экрана

Открывается из вкладки "Образы" по FAB `+`.  
Свободный холст: пользователь добавляет вещи из гардероба, перетаскивает, масштабирует.

> **keynotes**: "Конструктор не в выборке — вещи выбираются внутри конструктора"

### Структура экрана

```
← (назад)    [Конструктор]    [+ Вещь] (кнопка)
─────────────────────────────────────────────────
[Холст — белый/тёмный фон, dashed border]
  - Вещи на холсте (draggable, scalable)
  - Корзина (появляется при drag)
  - Пустое состояние если вещей нет
─────────────────────────────────────────────────
[ZoomOut] [ZoomIn] [Trash]   ...   [кол-во] [✓]
```

### Холст

- Белый фон — светлая тема; `#151515` — тёмная тема (через `Theme.colors.*`)
- Dashed border
- Вещи позиционируются абсолютно (x, y)
- Тап на вещь — выделить (рамка акцентного цвета), показать лейбл
- Тап по пустому холсту — снять выделение
- Drag вещи — перемещение
- Drag на корзину в нижнем центре — удалить с холста

### ItemPickerSheet

Bottom sheet для выбора вещей:
- `CategoryChips` для фильтрации
- Сетка 3 колонки
- Уже добавленные на холст — затемнены с галочкой

### Нижняя панель управления

- Вещь выбрана: `[ZoomOut] [ZoomIn] [Trash]`
- Вещь не выбрана: `[RotateCcw reset]`
- Правая часть: счётчик вещей + `✓` (активна только если есть вещи)

### Сохранение

По нажатию `✓`:
1. Сделать скриншот холста
2. Вызвать `AddLookUseCase(DraftLook(...), imageBytes)`
3. Вернуться к экрану образов

---

## FlowMVI

### OutfitConstructorState

```kotlin
data class CanvasItem(
    val clothe: Clothe,
    val x: Float,
    val y: Float,
    val scale: Float = 1f,
    val isSelected: Boolean = false,
)

data class OutfitConstructorState(
    val canvasItems: List<CanvasItem> = emptyList(),
    val availableClothes: List<Clothe> = emptyList(),
    val activeCategory: String? = null,
    val isPickerVisible: Boolean = false,
    val isSaving: Boolean = false,
) : MVIState {
    val selectedItem: CanvasItem? get() = canvasItems.firstOrNull { it.isSelected }
    val canSave: Boolean get() = canvasItems.isNotEmpty()
}
```

### OutfitConstructorIntent

```kotlin
sealed interface OutfitConstructorIntent : MVIIntent {
    data object LoadClothes : OutfitConstructorIntent
    data class AddItem(val clothe: Clothe) : OutfitConstructorIntent
    data class SelectItem(val clotheId: Int?) : OutfitConstructorIntent
    data class MoveItem(val clotheId: Int, val x: Float, val y: Float) : OutfitConstructorIntent
    data class ScaleItem(val clotheId: Int, val scale: Float) : OutfitConstructorIntent
    data class RemoveItem(val clotheId: Int) : OutfitConstructorIntent
    data object ResetCanvas : OutfitConstructorIntent
    data object ShowPicker : OutfitConstructorIntent
    data object HidePicker : OutfitConstructorIntent
    data class FilterCategory(val category: String?) : OutfitConstructorIntent
    data class SaveClicked(val captureCanvas: () -> ByteArray) : OutfitConstructorIntent
    data object BackClicked : OutfitConstructorIntent
}
```

### OutfitConstructorSideEffect

```kotlin
sealed class OutfitConstructorSideEffect {
    data object NavigateBack : OutfitConstructorSideEffect()
    data class ShowError(val message: String) : OutfitConstructorSideEffect()
}
```

---

## Data Layer

### DTO

```kotlin
// internal/data/dto/AddLookRequest.kt
@Serializable
data class AddLookRequest(
    @SerialName("name") val name: String,
    @SerialName("clothe_ids") val clotheIds: List<Int>,
)
```

### Mapper

```kotlin
// internal/data/mapper/DraftLookMapper.kt
fun DraftLook.toRequest(): AddLookRequest = AddLookRequest(
    name = name,
    clotheIds = clotheIds,
)
```

`ClotheDto` / `ClotheMapper` — переиспользуются из модуля `wardrobe` или дублируются локально в зависимости от решения по зависимостям (см. раздел "Зависимости модуля").

### RepositoryImpl

```kotlin
// internal/data/repository/OutfitConstructorRepositoryImpl.kt
class OutfitConstructorRepositoryImpl(
    private val api: OutfitConstructorApi,
) : OutfitConstructorRepository {

    override suspend fun getClothes(): List<Clothe> =
        api.getClothes().map { it.toClothe() }

    override suspend fun addLook(look: DraftLook, image: ByteArray): Look =
        api.addLook(
            request = look.toRequest(),
            image = image,
        ).toLook()
}
```

### Use Cases

```kotlin
// internal/domain/usecase/GetClothesUseCase.kt
class GetClothesUseCase(private val repository: OutfitConstructorRepository) {
    suspend operator fun invoke(): Result<List<Clothe>> =
        runCatching { repository.getClothes() }
}

// internal/domain/usecase/AddLookUseCase.kt
class AddLookUseCase(private val repository: OutfitConstructorRepository) {
    suspend operator fun invoke(look: DraftLook, image: ByteArray): Result<Look> =
        runCatching { repository.addLook(look, image) }
}
```

---

## Навигационные маршруты

```kotlin
// external/OutfitConstructorRoutes.kt
@Serializable data class OutfitConstructorRoute(val lookId: Int? = null)
```

## Зависимости модуля

Конструктору нужен доступ к списку вещей пользователя. Два варианта:
1. **Переиспользовать** `OutfitConstructorRepository` с методом `getClothes()` — репозиторий делает тот же сетевой вызов.
2. **Зависеть** от `wardrobe` модуля через `external/` API (предпочтительнее при необходимости кэша).

Решается при реализации — важно не создавать циклических зависимостей.

## Используемые системные компоненты (core/compose/components)

- `CategoryChips` — в `ItemPickerSheet`
- `ItemCard` — в пикере (3 колонки)
