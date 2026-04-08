# Clean Architecture + FlowMVI

Архитектурный паттерн для всех фич проекта. Применяется к модулям: `wardrobe`, `outfit`, `outfit_constructor`, `profile`, `home`.  
Для `core`-модулей (без UI) FlowMVI необязателен.

---

## Стек

- **FlowMVI** — `pro.respawn.flowmvi:core` — MVI-фреймворк для State и Intent
- **SideEffect** — кастомная обёртка над `MutableSharedFlow` (см. ниже) — для одноразовых событий
- **Koin** — Dependency Injection
- **Clean Architecture** — разделение на data / domain / presentation слои
- **Kotlin Multiplatform** — общий код в `commonMain`

---

## Структура модуля фичи

```
features/<feature-name>/
├── build.gradle.kts
├── di/
│   └── FeatureModule.kt              # Koin-модуль фичи
├── internal/
│   ├── data/
│   │   ├── dto/                      # Сетевые/БД DTO
│   │   ├── mapper/                   # DTO → доменные модели
│   │   └── repository/               # Реализации репозиториев
│   ├── domain/
│   │   ├── model/                    # Доменные модели
│   │   ├── repository/               # Интерфейсы репозиториев
│   │   └── usecase/                  # Use cases
│   └── presentation/
│       ├── list/                     # Экран-список (или root если один экран)
│       │   ├── interactionModel/
│       │   │   ├── FeatureState.kt
│       │   │   ├── FeatureIntent.kt
│       │   │   └── FeatureSideEffect.kt
│       │   ├── FeatureStore.kt
│       │   ├── FeatureViewModel.kt
│       │   └── FeatureScreen.kt
│       └── detail/                   # Экраны-деталки (если есть)
│           ├── featureDetail/        # отдельный подпакет на каждую фичу внутри detail
│           │   ├── interactionModel/
│           │   │   ├── FeatureDetailState.kt
│           │   │   ├── FeatureDetailIntent.kt
│           │   │   └── FeatureDetailSideEffect.kt
│           │   ├── FeatureDetailStore.kt
│           │   ├── FeatureDetailViewModel.kt
│           │   └── FeatureDetailScreen.kt
│           └── featureEdit/
│               ├── interactionModel/
│               │   ├── FeatureEditState.kt
│               │   ├── FeatureEditIntent.kt
│               │   └── FeatureEditSideEffect.kt
│               ├── FeatureEditStore.kt
│               ├── FeatureEditViewModel.kt
│               └── FeatureEditScreen.kt
└── external/
    ├── FeatureRoutes.kt              # @Serializable маршруты (ключи навигации)
    └── FeatureNavGraph.kt            # NavDisplayScope extension (Navigation 3)
```

**Правило пакетов:**
- `list/` — экраны-списки (отображают коллекцию: WardrobeScreen, OutfitScreen)
- `detail/` — экраны-деталки и редактирование (ItemDetailScreen, ItemEditScreen, OutfitDetailScreen, EditProfileScreen, SizesScreen)
- `root/` — единственный экран модуля без явного списка (OutfitConstructorScreen, ProfileScreen)

---

## Outcome — утилита

`Outcome<T>` — базовый sealed class для результата Query UseCase. Размещается в `core/compose`.  
Устраняет дублирование `sealed class GetXxxResult` в каждом UseCase.

```kotlin
// core/compose/src/commonMain/.../foundation/Outcome.kt

sealed class Outcome<out T> {
    data class Success<out T>(val data: T) : Outcome<T>()
    data object Empty : Outcome<Nothing>()
    data class Error(val message: String) : Outcome<Nothing>()
}
```

Query UseCase возвращает `Flow<Outcome<T>>`:

```kotlin
class GetItemsUseCase(private val repository: FeatureRepository) {
    operator fun invoke(): Flow<Outcome<List<FeatureModel>>> = flow {
        val items = repository.getItems()
        if (items.isEmpty()) emit(Outcome.Empty)
        else emit(Outcome.Success(items))
    }.catch { e -> emit(Outcome.Error(e.message ?: "Ошибка загрузки")) }
}
```

Store маппит `Outcome` в State:

```kotlin
getItemsUseCase().collect { result ->
    when (result) {
        is Outcome.Success -> updateState { copy(items = result.data, isLoading = false) }
        is Outcome.Empty   -> updateState { copy(isLoading = false, isEmpty = true) }
        is Outcome.Error   -> {
            updateState { copy(isLoading = false) }
            onSideEffect(FeatureSideEffect.ShowError(result.message))
        }
    }
}
```

---

## SideEffect — утилита

SideEffect — кастомная обёртка над `MutableSharedFlow`, размещается в `core/compose`.  
Обеспечивает одноразовую доставку события в UI с автоматической отпиской.

```kotlin
// core/compose/src/commonMain/.../foundation/SideEffect.kt

fun interface Cancellation {
    fun cancel()
}

abstract class SideEffect<out T : Any> {
    abstract fun subscribe(observer: (T) -> Unit): Cancellation
}

fun <T : Any> MutableSharedFlow<T>.toSideEffect(): SideEffect<T> =
    object : SideEffect<T>() {
        private val scope = MainScope()
        override fun subscribe(observer: (T) -> Unit): Cancellation {
            val job = onEach { observer(it) }.launchIn(scope)
            return Cancellation { job.cancel() }
        }
    }

// Composable-расширение для подписки
@Composable
@NonRestartableComposable
fun <T : Any> SideEffect<T>.handle(
    key: Any? = Unit,
    onEffect: (T) -> Unit,
) {
    DisposableEffect(key) {
        val cancellation = subscribe { effect -> onEffect(effect) }
        onDispose { cancellation.cancel() }
    }
}
```

---

## Data Layer

### DTO

Объекты передачи данных — строго соответствуют JSON от API. Аннотированы `@Serializable`.  
Для мутирующих запросов (PATCH/POST) — отдельные request-классы.

Каждое поле обязательно помечается `@SerialName` с точным ключом из JSON — даже если имя совпадает с camelCase. Это защищает от молчаливой поломки при переименовании поля в коде.

```kotlin
// internal/data/dto/FeatureDto.kt
@Serializable
data class FeatureDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("optional_field") val optionalField: String? = null,
)

// internal/data/dto/FeatureUpdateRequest.kt
@Serializable
data class FeatureUpdateRequest(
    @SerialName("name") val name: String? = null,
    @SerialName("optional_field") val optionalField: String? = null,
)
```

### Mapper

Маппер — extension-функция на DTO, возвращает доменную модель. Никакой бизнес-логики.

```kotlin
// internal/data/mapper/FeatureMapper.kt
fun FeatureDto.toDomain(): FeatureModel = FeatureModel(
    id = id,
    name = name,
    optionalField = optionalField,
)

fun FeatureModel.toUpdateRequest(): FeatureUpdateRequest = FeatureUpdateRequest(
    name = name,
    optionalField = optionalField,
)
```

**Правила маппера:**
- Один файл на одну доменную модель.
- Только `fun DtoType.toDomain()` и `fun DomainType.toRequest()` — ничего лишнего.
- Обратная совместимость (deprecated поля) — обрабатывается здесь, не в репозитории.

### RepositoryImpl

```kotlin
// internal/data/repository/FeatureRepositoryImpl.kt
class FeatureRepositoryImpl(
    private val api: FeatureApi,
) : FeatureRepository {

    override suspend fun getItems(): List<FeatureModel> =
        api.getItems().map { it.toDomain() }

    override suspend fun getItemById(id: Int): FeatureModel =
        api.getItemById(id).toDomain()

    override suspend fun updateItem(id: Int, item: FeatureModel): FeatureModel =
        api.updateItem(id, item.toUpdateRequest()).toDomain()

    override suspend fun deleteItem(id: Int) {
        api.deleteItem(id)
    }
}
```

**Правила RepositoryImpl:**
- Не содержит бизнес-логики — только вызов API и маппинг.
- Обработка ошибок — на уровне Use Case, не здесь.
- Кэширование (если нужно) — через отдельный `LocalDataSource`, не в репозитории напрямую.

### Use Cases

Каждый Use Case — один класс, один метод `invoke`. Содержит бизнес-логику: обработку пустых состояний, ошибок, трансформаций. Store — чистый презентер, только маппит результат UseCase в State.

Два типа Use Case:

#### Query UseCase (наблюдение / загрузка данных)

Возвращает `Flow<Outcome<T>>`. Логика пустого состояния и ошибок — внутри UseCase.  
Индивидуальные `sealed class GetXxxResult` не нужны — используется базовый `Outcome<T>`.

```kotlin
// internal/domain/usecase/GetItemsUseCase.kt
class GetItemsUseCase(private val repository: FeatureRepository) {
    operator fun invoke(): Flow<Outcome<List<FeatureModel>>> = flow {
        val items = repository.getItems()
        if (items.isEmpty()) emit(Outcome.Empty)
        else emit(Outcome.Success(items))
    }.catch { e -> emit(Outcome.Error(e.message ?: "Ошибка загрузки")) }
}
```

#### Command UseCase (мутация: update, delete, create)

Возвращает `Result<T>` через `suspend fun`. Одноразовая операция без наблюдения.

```kotlin
// internal/domain/usecase/UpdateItemUseCase.kt
class UpdateItemUseCase(private val repository: FeatureRepository) {
    suspend operator fun invoke(id: Int, item: FeatureModel): Result<FeatureModel> =
        runCatching { repository.updateItem(id, item) }
}

// internal/domain/usecase/DeleteItemUseCase.kt
class DeleteItemUseCase(private val repository: FeatureRepository) {
    suspend operator fun invoke(id: Int): Result<Unit> =
        runCatching { repository.deleteItem(id) }
}
```

**Правила Use Case:**
- Один Use Case = одна операция. Не объединять несколько операций в один класс.
- Query UseCase владеет бизнес-логикой: пустые состояния, маппинг ошибок — здесь, не в Store.
- Store не вызывает `fold` / `onSuccess` / `onFailure` с бизнес-условиями — только маппит sealed-результат в State.
- Command UseCase возвращает `Result<T>` — Store обрабатывает `onSuccess` / `onFailure` только для навигации и показа ошибки.

---

## FlowMVI — ключевые концепции

### Компоненты

| Компонент | Тип | Описание |
|---|---|---|
| `State` | `data class : MVIState` | Текущее состояние UI (immutable) |
| `Intent` | `sealed interface : MVIIntent` | Действия от пользователя → поступают в Store |
| `SideEffect` | `sealed class` | Разовые события → навигация, тосты, открытие URL |
| `Store` | `IStore<S, I, Nothing>` | Получает Intent, обновляет State, вызывает `onSideEffect` |
| `ViewModel` | `ViewModel` | Владеет Store и `_sideEffect: MutableSharedFlow` |

### Правила

- `State` — полностью immutable. Копирование через `copy()`.
- `Intent` — всё что пользователь может сделать с экраном.
- `SideEffect` — одноразовые события. Не хранятся в State.
- Store — не знает о Composable и Android-специфике. Тип Action в FlowMVI = `Nothing`.
- ViewModel — владеет `_sideEffect` и передаёт `{ _sideEffect.tryEmit(it) }` в Store.

---

## SideEffect — подробный разбор

### State vs SideEffect

| Ситуация | Что использовать |
|---|---|
| "Перейти на экран деталки" | `SideEffect` — одноразовая навигация |
| "Показать Snackbar с ошибкой" | `SideEffect` — одноразовый показ |
| "Показать диалог подтверждения" | `State` — диалог виден пока `showDeleteDialog = true` |
| "Скопировать URL в буфер" | `SideEffect` — одноразовая операция |
| "Кнопка сохранить — в процессе" | `State` — `isSaving = true` |
| "Закрыть экран после сохранения" | `SideEffect` — одноразовый pop back stack |

---

### Объявление SideEffect

```kotlin
// internal/presentation/list/interactionModel/WardrobeSideEffect.kt
sealed class WardrobeSideEffect {
    data class NavigateToDetail(val clotheId: Int) : WardrobeSideEffect()
    data object ShowAddBottomSheet : WardrobeSideEffect()
    data class ShowError(val message: String) : WardrobeSideEffect()
}
```

---

### Испускание из Store

Store принимает `onSideEffect` лямбду и вызывает её вместо `action(...)`.  
Тип Action в FlowMVI остаётся `Nothing` — фреймворк не участвует в доставке SideEffect.

```kotlin
// internal/presentation/list/WardrobeStore.kt
fun wardrobeStore(
    getClothesUseCase: GetClothesUseCase,
    deleteClotheUseCase: DeleteClotheUseCase,
    onSideEffect: (WardrobeSideEffect) -> Unit,
): IStore<WardrobeState, WardrobeIntent, Nothing> = store(
    initial = WardrobeState(isLoading = true)
) {
    init {
        send(WardrobeIntent.LoadClothes)
    }

    reduce { intent ->
        when (intent) {
            is WardrobeIntent.LoadClothes -> {
                updateState { copy(isLoading = true) }
                // Store — чистый презентер: только маппит Outcome в State
                getClothesUseCase().collect { result ->
                    when (result) {
                        is Outcome.Success ->
                            updateState { copy(clothes = result.data, isLoading = false, isEmpty = false) }
                        is Outcome.Empty ->
                            updateState { copy(isLoading = false, isEmpty = true) }
                        is Outcome.Error -> {
                            updateState { copy(isLoading = false) }
                            onSideEffect(WardrobeSideEffect.ShowError(result.message))
                        }
                    }
                }
            }
            is WardrobeIntent.ItemClicked ->
                onSideEffect(WardrobeSideEffect.NavigateToDetail(intent.clotheId))

            is WardrobeIntent.AddClicked ->
                onSideEffect(WardrobeSideEffect.ShowAddBottomSheet)

            is WardrobeIntent.DeleteClothe -> {
                // Command UseCase — Result<T>, Store обрабатывает только навигацию/ошибку
                deleteClotheUseCase(intent.id).fold(
                    onSuccess = { send(WardrobeIntent.LoadClothes) },
                    onFailure = { e -> onSideEffect(WardrobeSideEffect.ShowError(e.message ?: "Ошибка удаления")) }
                )
            }
            is WardrobeIntent.SelectCategory ->
                updateState { copy(activeCategory = intent.category) }

            is WardrobeIntent.ApplyFilter ->
                updateState { copy(filterOptions = intent.options) }
        }
    }
}
```

---

### ViewModel — владеет `_sideEffect`

```kotlin
// internal/presentation/list/WardrobeViewModel.kt
class WardrobeViewModel(
    getClothesUseCase: GetClothesUseCase,
    deleteClotheUseCase: DeleteClotheUseCase,
) : ViewModel() {

    private val _sideEffect = MutableSharedFlow<WardrobeSideEffect>(extraBufferCapacity = 1)
    val sideEffect = _sideEffect.toSideEffect()

    val store = wardrobeStore(
        getClothesUseCase = getClothesUseCase,
        deleteClotheUseCase = deleteClotheUseCase,
        onSideEffect = { _sideEffect.tryEmit(it) },
    ).also { lifecycle.subscribe(it) }
}
```

---

### Подписка в Composable — `.handle { }`

```kotlin
// internal/presentation/list/WardrobeScreen.kt
@Composable
fun WardrobeScreen(
    onNavigateToDetail: (clotheId: Int) -> Unit,
    onShowAddSheet: () -> Unit,
) {
    val container = koinViewModel<WardrobeViewModel>()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    container.sideEffect.handle { effect ->
        when (effect) {
            is WardrobeSideEffect.NavigateToDetail ->
                onNavigateToDetail(effect.clotheId)

            is WardrobeSideEffect.ShowAddBottomSheet ->
                onShowAddSheet()

            is WardrobeSideEffect.ShowError ->
                scope.launch { snackbarHostState.showSnackbar(effect.message) }
        }
    }

    val state by container.store.subscribe()

    WardrobeContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = { container.store.intent(it) },
    )
}
```

---

### Паттерн: диалог подтверждения

Диалог — в `State` (видимость). Навигация после удаления — через `SideEffect`.

```kotlin
// State
data class ItemDetailState(
    val clothe: ClotheDetail? = null,
    val showDeleteDialog: Boolean = false,
    val isLoading: Boolean = true,
) : MVIState

// SideEffect
sealed class ItemDetailSideEffect {
    data object NavigateBack : ItemDetailSideEffect()
    data class NavigateToEdit(val clotheId: Int) : ItemDetailSideEffect()
    data class ShowError(val message: String) : ItemDetailSideEffect()
}

// Store
is ItemDetailIntent.DeleteClicked ->
    updateState { copy(showDeleteDialog = true) }

is ItemDetailIntent.DeleteConfirmed -> {
    updateState { copy(showDeleteDialog = false) }
    deleteClotheUseCase(withState { clothe!!.id!! }).fold(
        onSuccess = { onSideEffect(ItemDetailSideEffect.NavigateBack) },
        onFailure = { e -> onSideEffect(ItemDetailSideEffect.ShowError(e.message ?: "Ошибка")) }
    )
}

is ItemDetailIntent.DeleteCancelled ->
    updateState { copy(showDeleteDialog = false) }

// Composable
@Composable
fun ItemDetailScreen(onBack: () -> Unit, onNavigateToEdit: (Int) -> Unit) {
    val container = koinViewModel<ItemDetailViewModel>()
    val scope = rememberCoroutineScope()

    container.sideEffect.handle { effect ->
        when (effect) {
            is ItemDetailSideEffect.NavigateBack -> onBack()
            is ItemDetailSideEffect.NavigateToEdit -> onNavigateToEdit(effect.clotheId)
            is ItemDetailSideEffect.ShowError -> scope.launch { snackbarHostState.showSnackbar(effect.message) }
        }
    }

    val state by container.store.subscribe()

    if (state.showDeleteDialog) {
        DeleteConfirmDialog(
            onConfirm = { container.store.intent(ItemDetailIntent.DeleteConfirmed) },
            onDismiss = { container.store.intent(ItemDetailIntent.DeleteCancelled) },
        )
    }

    ItemDetailContent(state = state, onIntent = { container.store.intent(it) })
}
```

---

### Паттерн: открытие URL

```kotlin
// SideEffect
data class OpenUrl(val url: String) : ItemDetailSideEffect()

// Store
is ItemDetailIntent.MarketplaceLinkClicked ->
    onSideEffect(ItemDetailSideEffect.OpenUrl(intent.url))

// Composable — LocalUriHandler из Compose Multiplatform
@Composable
fun ItemDetailScreen(...) {
    val uriHandler = LocalUriHandler.current

    container.sideEffect.handle { effect ->
        when (effect) {
            is ItemDetailSideEffect.OpenUrl -> uriHandler.openUri(effect.url)
            // ...
        }
    }
}
```

---

### Чего НЕ делать

```kotlin
// ❌ Навигация через State
data class WardrobeState(val navigateTo: Int? = null)  // НЕЛЬЗЯ

// ❌ LaunchedEffect на флаг в State вместо SideEffect
LaunchedEffect(state.shouldNavigate) {
    if (state.shouldNavigate) onNavigate()  // НЕЛЬЗЯ
}

// ✅ Навигация только через SideEffect
container.sideEffect.handle { effect ->
    when (effect) {
        is FeatureSideEffect.Navigate -> onNavigate(effect.id)
    }
}
```

---

## DI (Koin)

```kotlin
// di/WardrobeModule.kt
val wardrobeModule = module {
    // single — Repository и Use Case stateless/держат только синглтоны, пересоздавать не нужно
    single<WardrobeRepository> { WardrobeRepositoryImpl(get()) }
    single { GetClothesUseCase(get()) }
    single { DeleteClotheUseCase(get()) }
    single { GetClotheByIdUseCase(get()) }
    single { UpdateClotheUseCase(get()) }

    // viewModel — lifecycle управляется Koin + AndroidX, не трогать
    viewModel { WardrobeViewModel(get(), get()) }
    viewModel { ItemDetailViewModel(get(), get(), get()) }
    viewModel { ItemEditViewModel(get()) }
}
```

**Правило DI:** `single` — Repository и Use Case. `factory` не использовать: Use Case держит только синглтоны, пересоздавать каждый раз нет смысла.

---

## Правила

1. **Store** не импортирует ничего из `android.*`, `androidx.*`.
2. **Store** получает `onSideEffect` лямбдой — не знает о `MutableSharedFlow`.
3. **ViewModel** владеет `_sideEffect` и передаёт `{ _sideEffect.tryEmit(it) }` в Store.
4. **Навигация** — через callback-параметры Composable (`onBack`, `onNavigateToDetail`), вызываемые из `.handle { }`.
5. **SideEffect** — только одноразовые события. Постоянное состояние — в `State`.
6. **Composable** не вызывает бизнес-логику напрямую — только `container.store.intent(...)`.

---

## Зависимости (libs.versions.toml)

```toml
[versions]
flowmvi = "3.x.x"   # уточнить актуальную стабильную версию

[libraries]
flowmvi-core        = { module = "pro.respawn.flowmvi:core",       version.ref = "flowmvi" }
flowmvi-compose     = { module = "pro.respawn.flowmvi:compose",    version.ref = "flowmvi" }
flowmvi-savedstate  = { module = "pro.respawn.flowmvi:savedstate", version.ref = "flowmvi" }
```

`SideEffect<T>` и `.handle {}` — собственный код в `core/compose/src/commonMain`, зависимостей не требует.
