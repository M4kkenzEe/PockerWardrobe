# Экран Гардероб (вкладка "Одежда")

## Модуль

`features/wardrobe` (новый модуль, перенесён и доработан из `wardrobe-feature`)

## Файлы

```
features/wardrobe/src/commonMain/.../
├── di/WardrobeModule.kt
├── internal/
│   ├── data/
│   │   ├── dto/ClotheDto.kt
│   │   ├── mapper/ClotheMapper.kt
│   │   └── repository/WardrobeRepositoryImpl.kt
│   ├── domain/
│   │   ├── model/Clothe.kt, FilterOptions.kt
│   │   ├── repository/WardrobeRepository.kt
│   │   └── usecase/GetClothesUseCase.kt, DeleteClotheUseCase.kt, ...
│   └── presentation/
│       └── list/
│           ├── interactionModel/
│           │   ├── WardrobeState.kt
│           │   ├── WardrobeIntent.kt
│           │   └── WardrobeSideEffect.kt
│           ├── WardrobeStore.kt
│           ├── WardrobeContainer.kt
│           └── WardrobeScreen.kt
└── external/
    ├── WardrobeRoutes.kt
    └── WardrobeNavGraph.kt
```

## Описание экрана

Вкладка "Одежда" внутри `WardrobeMainScreen` (переключается с вкладкой "Образы" через сегментированный контрол).

Показывает все вещи пользователя в сетке 2 колонки с фильтрацией по категории и расширенными фильтрами.

### Состояния экрана

| Состояние | Описание |
|---|---|
| `normal` | Есть вещи — показываем сетку карточек |
| `empty` | Гардероб пуст — скрыть категории и фильтр, показать пустое состояние с кнопками |
| `loading` | Загрузка — показывать skeleton-карточки |
| `empty_category` | Вещей в выбранной категории нет — показываем текст-заглушку |

### Структура экрана (состояние `normal`)

```
[Количество вещей]           [Кнопка фильтра]
[Категории: Все | Верх | Низ | Обувь | Верхняя | Сумки]
[Сетка карточек 2 колонки]
                                              [FAB +]
```

**Пустое состояние** (нет вещей вообще):
- Скрывать чипсы категорий и кнопку фильтра
- Иконка сумки, заголовок "Гардероб пуст", описание
- Кнопка "Сфотографировать вещь" (primary)
- Кнопка "Выбрать из галереи" (secondary)
- FAB скрыт

### Карточка вещи (ItemCard)

- Фото вещи 1:1 (белый фон у фото)
- Имя вещи (`Theme.typography.caption`)
- Тег категории (`TagChip`)
- По тапу — открыть деталку (`WardrobeIntent.ItemClicked`)
- По долгому тапу — меню: Удалить, Изменить

### FAB и добавление вещи

Кнопка `+` (FAB) открывает `AddBottomSheet` с тремя вариантами:
1. **Сфотографировать вещь** — "AI удалит фон автоматически"
2. **Добавить из галереи** — "Выберите фото из библиотеки"
3. **Конструктор образов** — навигация к `OutfitConstructorRoute`

### Фильтр (FilterBottomSheet)

Bottom sheet с секциями:
- **Сортировка**: "По дате" | "По имени" | "По категории" (radio chips)
- **Сезон**: "Весна" | "Лето" | "Осень" | "Зима" (multi-select)
- **Цвет**: цветные кружки (multi-select)
- Кнопка "Сбросить" (если есть активные фильтры)
- Кнопка "Показать" (primary)

Индикатор на кнопке фильтра: если фильтры активны — показать точку/бейдж.

---

## FlowMVI

### WardrobeState

```kotlin
data class WardrobeState(
    val clothes: List<Clothe> = emptyList(),
    val filteredClothes: List<Clothe> = emptyList(),
    val activeCategory: String? = null,
    val filterOptions: FilterOptions = FilterOptions(),
    val isLoading: Boolean = true,
    val isEmpty: Boolean = false,
    val isFilterActive: Boolean = false,
) : MVIState
```

### WardrobeIntent

```kotlin
sealed interface WardrobeIntent : MVIIntent {
    data object LoadClothes : WardrobeIntent
    data class SelectCategory(val category: String?) : WardrobeIntent
    data class ApplyFilter(val options: FilterOptions) : WardrobeIntent
    data class DeleteClothe(val id: Int) : WardrobeIntent
    data class ItemClicked(val clotheId: Int) : WardrobeIntent
    data class ItemLongClicked(val clotheId: Int) : WardrobeIntent
    data object FabClicked : WardrobeIntent
    data object FilterClicked : WardrobeIntent
}
```

### WardrobeSideEffect

```kotlin
sealed class WardrobeSideEffect {
    data class NavigateToDetail(val clotheId: Int) : WardrobeSideEffect()
    data object ShowAddBottomSheet : WardrobeSideEffect()
    data object ShowFilterBottomSheet : WardrobeSideEffect()
    data class ShowItemMenu(val clotheId: Int) : WardrobeSideEffect()
    data class ShowError(val message: String) : WardrobeSideEffect()
}
```

---

## Data Layer

### DTO

```kotlin
// internal/data/dto/ClotheDto.kt
@Serializable
data class ClotheDto(
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
    val storeUrl: String? = null,  // deprecated — обратная совместимость
)
```

### Mapper

```kotlin
// internal/data/mapper/ClotheMapper.kt
fun ClotheDto.toClothe(): Clothe = Clothe(
    id = id,
    name = name,
    imageUrl = imageUrl,
    category = category,
    styles = styles,
    season = season,
    color = color,
    size = size,
    tags = tags,
    marketplaceLinks = marketplaceLinks
        ?: storeUrl?.let { listOf(it) }  // fallback для старого API
        ?: emptyList(),
)
```

### RepositoryImpl

```kotlin
// internal/data/repository/WardrobeRepositoryImpl.kt
class WardrobeRepositoryImpl(
    private val api: WardrobeApi,
) : WardrobeRepository {

    override suspend fun getClothes(): List<Clothe> =
        api.getClothes().map { it.toClothe() }

    override suspend fun deleteClothe(id: Int) {
        api.deleteClothe(id)
    }

    override suspend fun loadClothe(bitmap: ByteArray): Clothe =
        api.uploadClothe(bitmap).toClothe()

    override suspend fun uploadFromUrl(url: String): Clothe =
        api.uploadFromUrl(url).toClothe()
}
```

### Use Cases

```kotlin
// internal/domain/usecase/GetClothesUseCase.kt
class GetClothesUseCase(private val repository: WardrobeRepository) {
    suspend operator fun invoke(): Result<List<Clothe>> =
        runCatching { repository.getClothes() }
}

// internal/domain/usecase/DeleteClotheUseCase.kt
class DeleteClotheUseCase(private val repository: WardrobeRepository) {
    suspend operator fun invoke(id: Int): Result<Unit> =
        runCatching { repository.deleteClothe(id) }
}
```

---

## Что перенести из `wardrobe-feature`

- `WardrobeRepositoryImpl` — перенести в `wardrobe/internal/data/repository/`
- `Clothe` доменная модель — расширить новыми полями (см. `08_backend_changes.md`)
- `deleteClothe(id)` — остаётся в репозитории
- `loadClothe(bitmap)`, `uploadFromUrl(url)` — остаётся

Что из `wardrobe-feature` **не** берём в этот модуль:
- Образы (`Looks.kt`) → уходят в `features/outfit`
- Конструктор (`LookConstructor.kt`) → уходит в `features/outfit_constructor`

## Используемые системные компоненты (core/compose/components)

- `CategoryChips` — горизонтальный скролл чипсов
- `ItemCard` — карточка вещи с тегом категории
- `SkeletonCard` — скелетон загрузки
- `AppTopBar` — шапка (на деталке)

Компоненты `FilterBottomSheet` и `AddBottomSheet` — внутренние для модуля, если не используются в других фичах.
