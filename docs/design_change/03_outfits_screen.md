# Экран Образы (вкладка "Образы")

## Модуль

`features/outfit` (новый модуль, перенесён из `wardrobe-feature/Looks.kt`)

## Файлы

```
features/outfit/src/commonMain/.../
├── di/OutfitModule.kt
├── internal/
│   ├── data/
│   │   ├── dto/LookDto.kt
│   │   ├── mapper/LookMapper.kt
│   │   └── repository/OutfitRepositoryImpl.kt
│   ├── domain/
│   │   ├── model/Look.kt
│   │   ├── repository/OutfitRepository.kt
│   │   └── usecase/GetLooksUseCase.kt, DeleteLookUseCase.kt, ShareLookUseCase.kt
│   └── presentation/
│       └── list/
│           ├── interactionModel/
│           │   ├── OutfitState.kt
│           │   ├── OutfitIntent.kt
│           │   └── OutfitSideEffect.kt
│           ├── OutfitStore.kt
│           ├── OutfitViewModel.kt
│           └── OutfitScreen.kt
└── external/
    ├── OutfitRoutes.kt
    └── OutfitNavGraph.kt
```

## Описание экрана

Вкладка "Образы" внутри `WardrobeMainScreen` (переключается с вкладкой "Одежда" через сегментированный контрол).

Показывает AI-сгенерированные и пользовательские образы в сетке 2 колонки.

### Состояния экрана

| Состояние | Описание |
|---|---|
| `normal` | Есть образы — показываем сетку |
| `loading` | Загрузка — skeleton-карточки |
| `generating` | AI генерирует образ — показать `AIGeneratingCard` + скелетоны |
| `empty` | Нет образов — пустое состояние |

### Структура экрана (состояние `normal`)

```
[Категории стилей: Casual | Smart Casual | Business | Sport | Evening]
[Сетка образов 2 колонки]
                                              [FAB +]
```

### Карточка образа (LookCard)

- Фото образа 1:1
- Имя образа
- Кнопка `⋯` → меню:
  - В избранное
  - Поделиться
  - Удалить
- Если `locked = true`: размытие + оверлей с иконкой замка и кнопкой "Подписка"

### AI-карточка генерации (AIGeneratingCard)

Показывается первой пока AI генерирует:
- Иконка спиннера (анимированный)
- "AI создаёт образ…"
- "Анализирует ваш гардероб"

### FAB

FAB `+` в табе "Образы" открывает `OutfitConstructorRoute` напрямую (не bottom sheet).

---

## FlowMVI

### OutfitState

```kotlin
data class OutfitState(
    val looks: List<Look> = emptyList(),
    val activeStyle: String? = null,
    val isLoading: Boolean = true,
    val isEmpty: Boolean = false,
    val isGenerating: Boolean = false,
) : MVIState
```

### OutfitIntent

```kotlin
sealed interface OutfitIntent : MVIIntent {
    data object LoadLooks : OutfitIntent
    data class SelectStyle(val style: String?) : OutfitIntent
    data class LookClicked(val lookId: Int) : OutfitIntent
    data class DeleteLook(val id: Int) : OutfitIntent
    data class ShareLook(val id: Int) : OutfitIntent
    data class FavoriteLook(val id: Int) : OutfitIntent
    data object FabClicked : OutfitIntent
}
```

### OutfitSideEffect

```kotlin
sealed class OutfitSideEffect {
    data class NavigateToDetail(val lookId: Int) : OutfitSideEffect()
    data object NavigateToConstructor : OutfitSideEffect()
    data class ShowError(val message: String) : OutfitSideEffect()
}
```

---

## Data Layer

### DTO

```kotlin
// internal/data/dto/LookDto.kt
@Serializable
data class LookDto(
    @SerialName("id") val id: Int? = null,
    @SerialName("name") val name: String,
    @SerialName("url") val url: String,
    @SerialName("look_items") val lookItems: List<LookItemDto>? = null,
    @SerialName("style") val style: String? = null,
    @SerialName("tags") val tags: List<String>? = null,
)

@Serializable
data class LookItemDto(
    @SerialName("id") val id: Int,
    @SerialName("clothe_id") val clotheId: Int,
    @SerialName("image_url") val imageUrl: String,
    @SerialName("name") val name: String? = null,
    @SerialName("category") val category: String? = null,
)
```

### Mapper

```kotlin
// internal/data/mapper/LookMapper.kt
fun LookDto.toLook(): Look = Look(
    id = id,
    name = name,
    url = url,
    lookItems = lookItems?.map { it.toLookItem() },
    style = style,
    tags = tags,
)

fun LookItemDto.toLookItem(): LookItem = LookItem(
    id = id,
    clotheId = clotheId,
    imageUrl = imageUrl,
    name = name.orEmpty(),
    category = category,
)
```

### RepositoryImpl

```kotlin
// internal/data/repository/OutfitRepositoryImpl.kt
class OutfitRepositoryImpl(
    private val api: OutfitApi,
) : OutfitRepository {

    override suspend fun getLooks(): List<Look> =
        api.getLooks().map { it.toLook() }

    override suspend fun getLookById(id: Int): Look =
        api.getLookById(id).toLook()

    override suspend fun deleteLook(id: Int) {
        api.deleteLook(id)
    }

    override suspend fun shareLook(id: Int): String =
        api.shareLook(id).url

    override suspend fun addLook(look: DraftLook, image: ByteArray): Look =
        api.addLook(look.toRequest(), image).toLook()
}
```

### Use Cases

```kotlin
// internal/domain/usecase/GetLooksUseCase.kt
class GetLooksUseCase(private val repository: OutfitRepository) {
    suspend operator fun invoke(): Result<List<Look>> =
        runCatching { repository.getLooks() }
}

// internal/domain/usecase/DeleteLookUseCase.kt
class DeleteLookUseCase(private val repository: OutfitRepository) {
    suspend operator fun invoke(id: Int): Result<Unit> =
        runCatching { repository.deleteLook(id) }
}

// internal/domain/usecase/ShareLookUseCase.kt
class ShareLookUseCase(private val repository: OutfitRepository) {
    suspend operator fun invoke(id: Int): Result<String> =
        runCatching { repository.shareLook(id) }
}
```

---

## Доменная модель `Look`

```kotlin
// internal/domain/model/Look.kt
data class Look(
    val id: Int? = null,
    val name: String,
    val url: String,
    val lookItems: List<LookItem>? = null,
    val style: String? = null,        // "Casual", "Business", etc.
    val tags: List<String>? = null,   // ["Пальто", "Брюки", "Кеды"]
    val isLocked: Boolean = false,
)
```

## Методы репозитория

```kotlin
// internal/domain/repository/OutfitRepository.kt
interface OutfitRepository {
    suspend fun getLooks(): List<Look>
    suspend fun getLookById(id: Int): Look
    suspend fun deleteLook(id: Int)
    suspend fun shareLook(id: Int): String   // возвращает URL для копирования
    suspend fun addLook(look: DraftLook, image: ByteArray): Look
}
```

## Навигационные маршруты

```kotlin
// external/OutfitRoutes.kt
@Serializable object OutfitMain
@Serializable data class OutfitDetail(val lookId: Int)
```

## Используемые системные компоненты (core/compose/components)

- `CategoryChips` — горизонтальный скролл чипсов стилей
- `LookCard` — карточка образа с меню `⋯`
- `MoreMenu` — выпадающее меню
- `SkeletonCard` — skeleton загрузки
