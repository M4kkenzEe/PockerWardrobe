# Деталка и редактирование вещи

## Модуль

`features/wardrobe` — экраны входят в тот же модуль что и гардероб.

## Файлы (новые)

```
features/wardrobe/src/commonMain/.../
├── internal/presentation/
│   └── detail/
│       ├── itemDetail/
│       │   ├── interactionModel/
│       │   │   ├── ItemDetailState.kt
│       │   │   ├── ItemDetailIntent.kt
│       │   │   └── ItemDetailSideEffect.kt
│       │   ├── ItemDetailStore.kt
│       │   ├── ItemDetailContainer.kt
│       │   └── ItemDetailScreen.kt
│       └── itemEdit/
│           ├── interactionModel/
│           │   ├── ItemEditState.kt
│           │   ├── ItemEditIntent.kt
│           │   └── ItemEditSideEffect.kt
│           ├── ItemEditStore.kt
│           ├── ItemEditContainer.kt
│           └── ItemEditScreen.kt
└── external/
    └── WardrobeRoutes.kt  # ItemDetail, ItemEdit маршруты
```

---

## Экран деталки вещи (ItemDetailScreen)

### Описание

Открывается по тапу на карточку в гардеробе.  
Показывает фото вещи, все характеристики, стиль-теги, образы в которых используется, ссылки на маркетплейсы.

### Структура экрана

```
← (назад)         [Вещь]         ✎ (редактировать)
─────────────────────────────────────────────────
[Фото вещи 3:4, скруглённые углы]

[Название вещи]   [тег категории]

── Характеристики ──────────────────────────────
Категория         Верх
Материал          Хлопок
Fit               Regular
Стили             Casual · Minimal
Сезон             Весна · Осень
Цвет              Белый  ●
Бренд             Не указан
Размер            M
Добавлено         12 мар 2026
─────────────────────────────────────────────────

── Теги ─────────────────────────────────────────
[softcore look] [grunge fashion] [minimalist style]

── Используется в образах ───────────────────────
[✦ Осенний образ] [✦ Минимал Стиль]

── Ссылки на маркетплейсы ───────────────────────
[Wildberries  wildberries.ru/...]
[Ozon         ozon.ru/...]
─────────────────────────────────────────────────

[Изменить]              [Удалить]
```

### FlowMVI

```kotlin
// ItemDetailState
data class ItemDetailState(
    val clothe: ClotheDetail? = null,
    val relatedLooks: List<Look> = emptyList(),
    val isLoading: Boolean = true,
) : MVIState

// ItemDetailIntent
sealed interface ItemDetailIntent : MVIIntent {
    data class Load(val clotheId: Int) : ItemDetailIntent
    data object EditClicked : ItemDetailIntent
    data object DeleteClicked : ItemDetailIntent
    data object DeleteConfirmed : ItemDetailIntent
    data class LookClicked(val lookId: Int) : ItemDetailIntent
    data class MarketplaceLinkClicked(val url: String) : ItemDetailIntent
}

// ItemDetailSideEffect
sealed class ItemDetailSideEffect {
    data class NavigateToEdit(val clotheId: Int) : ItemDetailSideEffect()
    data class NavigateToLook(val lookId: Int) : ItemDetailSideEffect()
    data class OpenUrl(val url: String) : ItemDetailSideEffect()
    data object NavigateBack : ItemDetailSideEffect()
    data class ShowDeleteConfirm(val clotheId: Int) : ItemDetailSideEffect()
}
```

---

## Экран редактирования вещи (ItemEditScreen)

### Структура экрана

```
← (отмена)    [Изменить вещь]    ✓ (сохранить)
─────────────────────────────────────────────────
[Фото вещи 3:4]

── Основные ─────────────────────────────────────
Название     [input]
Категория    [input]
Размер       [XXS] [XS] [S] [M] [L] [XL] [XXL]

── Дополнительно ────────────────────────────────
Материал     [input]
Fit          [input]
Стили        [input]
Сезон        [input]
Бренд        [input]

── Ссылки на маркетплейсы ───────────────────────
[🔗 wildberries.ru/... ]
[🔗 ozon.ru/...       ]
[+ Добавить ссылку]
─────────────────────────────────────────────────
[Сохранить изменения]
```

### FlowMVI

```kotlin
// ItemEditState
data class ItemEditState(
    val clothe: ClotheDetail? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
) : MVIState

// ItemEditIntent
sealed interface ItemEditIntent : MVIIntent {
    data class Load(val clotheId: Int) : ItemEditIntent
    data class NameChanged(val value: String) : ItemEditIntent
    data class CategoryChanged(val value: String) : ItemEditIntent
    data class SizeChanged(val value: String) : ItemEditIntent
    data class MaterialChanged(val value: String) : ItemEditIntent
    data class FitChanged(val value: String) : ItemEditIntent
    data class StylesChanged(val value: String) : ItemEditIntent
    data class SeasonChanged(val value: String) : ItemEditIntent
    data class BrandChanged(val value: String) : ItemEditIntent
    data class MarketplaceLinkChanged(val index: Int, val url: String) : ItemEditIntent
    data object AddMarketplaceLink : ItemEditIntent
    data object SaveClicked : ItemEditIntent
    data object CancelClicked : ItemEditIntent
}

// ItemEditSideEffect
sealed class ItemEditSideEffect {
    data object NavigateBack : ItemEditSideEffect()
    data class ShowError(val message: String) : ItemEditSideEffect()
}
```

---

## Data Layer

### DTO

`ClotheDto` — общий для списка и деталки (определён в `01_wardrobe_screen.md`).  
Для PATCH-запроса — отдельный request-объект:

```kotlin
// internal/data/dto/ClotheUpdateRequest.kt
@Serializable
data class ClotheUpdateRequest(
    val name: String? = null,
    val category: String? = null,
    val material: String? = null,
    val fit: String? = null,
    val styles: List<String>? = null,
    val season: List<String>? = null,
    val color: String? = null,
    val brand: String? = null,
    val size: String? = null,
    val marketplaceLinks: List<String>? = null,
)
```

### Mapper

```kotlin
// internal/data/mapper/ClotheMapper.kt  (дополнение)
fun ClotheDto.toClotheDetail(): ClotheDetail = ClotheDetail(
    id = id,
    name = name,
    imageUrl = imageUrl,
    category = category,
    material = material,
    fit = fit,
    styles = styles,
    season = season,
    color = color,
    brand = brand,
    size = size,
    marketplaceLinks = marketplaceLinks
        ?: storeUrl?.let { listOf(it) }
        ?: emptyList(),
    tags = tags,
    createdAt = createdAt,
)

fun ClotheDetail.toUpdateRequest(): ClotheUpdateRequest = ClotheUpdateRequest(
    name = name,
    category = category,
    material = material,
    fit = fit,
    styles = styles,
    season = season,
    color = color,
    brand = brand,
    size = size,
    marketplaceLinks = marketplaceLinks,
)
```

### RepositoryImpl

```kotlin
// internal/data/repository/WardrobeRepositoryImpl.kt  (дополнение к методам из 01)
override suspend fun getClotheById(clotheId: Int): ClotheDetail =
    api.getClotheById(clotheId).toClotheDetail()

override suspend fun updateClothe(clotheId: Int, clothe: ClotheDetail): ClotheDetail =
    api.updateClothe(clotheId, clothe.toUpdateRequest()).toClotheDetail()

override suspend fun getClotheOutfits(clotheId: Int): List<Look> =
    api.getClotheOutfits(clotheId).map { it.toLook() }
```

### Use Cases

```kotlin
// internal/domain/usecase/GetClotheByIdUseCase.kt
class GetClotheByIdUseCase(private val repository: WardrobeRepository) {
    suspend operator fun invoke(clotheId: Int): Result<ClotheDetail> =
        runCatching { repository.getClotheById(clotheId) }
}

// internal/domain/usecase/UpdateClotheUseCase.kt
class UpdateClotheUseCase(private val repository: WardrobeRepository) {
    suspend operator fun invoke(clotheId: Int, clothe: ClotheDetail): Result<ClotheDetail> =
        runCatching { repository.updateClothe(clotheId, clothe) }
}

// internal/domain/usecase/GetClotheOutfitsUseCase.kt
class GetClotheOutfitsUseCase(private val repository: WardrobeRepository) {
    suspend operator fun invoke(clotheId: Int): Result<List<Look>> =
        runCatching { repository.getClotheOutfits(clotheId) }
}
```

---

## Доменная модель

```kotlin
// internal/domain/model/ClotheDetail.kt
data class ClotheDetail(
    val id: Int?,
    val name: String,
    val imageUrl: String,
    val category: String? = null,
    val material: String? = null,
    val fit: String? = null,
    val styles: List<String>? = null,
    val season: List<String>? = null,
    val color: String? = null,
    val brand: String? = null,
    val size: String? = null,
    val marketplaceLinks: List<String>? = null,
    val tags: List<String>? = null,
    val createdAt: String? = null,
)
```

> `storeUrl` заменяется на `marketplaceLinks: List<String>`. Обратная совместимость — см. `08_backend_changes.md`.

## Методы репозитория

```kotlin
// internal/domain/repository/WardrobeRepository.kt
interface WardrobeRepository {
    suspend fun getClothes(): List<Clothe>
    suspend fun getClotheById(clotheId: Int): ClotheDetail
    suspend fun updateClothe(clotheId: Int, clothe: ClotheDetail): ClotheDetail
    suspend fun deleteClothe(id: Int)
    suspend fun getClotheOutfits(clotheId: Int): List<Look>
}
```

## Навигационные маршруты

```kotlin
// external/WardrobeRoutes.kt
@Serializable object WardrobeMain
@Serializable data class ItemDetail(val clotheId: Int)
@Serializable data class ItemEdit(val clotheId: Int)
```

## Используемые системные компоненты (core/compose/components)

- `AppTopBar` — шапка с кнопкой назад, заголовком, действием
- `TagChip` — чип-тег
- `MarketplaceLinkRow` — строка со ссылкой на маркетплейс
- `SizeSelector` — горизонтальные кнопки размеров
