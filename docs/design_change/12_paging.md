# Paging — постраничная загрузка данных

Документ описывает реализацию пагинации для экранов **Гардероба** (`features/wardrobe`) и **Образов** (`features/outfit`) с использованием **Paging 3** в связке с FlowMVI-архитектурой.

Референсы:
- [androidx.paging releases](https://developer.android.com/jetpack/androidx/releases/paging?hl=ru)
- [Pagination in Jetpack Compose with and without Paging 3](https://medium.com/@avanisoam/pagination-in-jetpack-compose-with-and-without-paging-3-library-28b54f0d0525)

---

## Почему Paging 3

- `paging-common` и `paging-compose` поддерживают KMP с версии **3.4.0** (JVM, iOS, macOS, JS, WasmJS)
- Автоматическое управление кешем, повторные запросы, отслеживание LoadState
- Встроенная интеграция с `LazyColumn` / `LazyVerticalGrid` через `collectAsLazyPagingItems()`
- Альтернатива — ручная пагинация: проще, но требует самостоятельного управления состоянием и кешем

---

## Зависимости (libs.versions.toml)

```toml
[versions]
paging = "3.4.2"

[libraries]
paging-common  = { module = "androidx.paging:paging-common",  version.ref = "paging" }
paging-compose = { module = "androidx.paging:paging-compose", version.ref = "paging" }
```

### build.gradle.kts фичи (commonMain)

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.paging.common)
            implementation(libs.paging.compose)
        }
    }
}
```

> `paging-runtime` — только Android. В `commonMain` используем только `paging-common` и `paging-compose`.

---

## Компоненты

| Компонент | Описание |
|---|---|
| `PagingSource<Key, Value>` | Загружает одну страницу данных. Реализуется в data-слое. |
| `Pager` | Конфигурирует пагинацию (`PagingConfig`), создаёт `Flow<PagingData<T>>`. |
| `PagingData<T>` | Контейнер с данными одной "порции". Не хранится в State. |
| `LazyPagingItems<T>` | Compose-обёртка для `Flow<PagingData<T>>`. Используется в UI. |
| `LoadState` | Текущее состояние загрузки: `NotLoading`, `Loading`, `Error`. |

---

## Data Layer

### PagingSource

Один `PagingSource` — одна сущность. Реализуется в `internal/data/`.

```kotlin
// features/wardrobe/internal/data/paging/ClothePagingSource.kt
class ClothePagingSource(
    private val api: WardrobeApi,
    private val category: String?,
) : PagingSource<Int, ClotheModel>() {

    override fun getRefreshKey(state: PagingState<Int, ClotheModel>): Int? =
        state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
        }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ClotheModel> {
        val page = params.key ?: FIRST_PAGE
        return try {
            val response = api.getClothes(page = page, size = params.loadSize, category = category)
            LoadResult.Page(
                data = response.items.map { it.toDomain() },
                prevKey = if (page == FIRST_PAGE) null else page - 1,
                nextKey = if (response.items.isEmpty()) null else page + 1,
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    companion object {
        const val FIRST_PAGE = 1
        const val PAGE_SIZE = 20
        const val PREFETCH_DISTANCE = 5
    }
}
```

```kotlin
// features/outfit/internal/data/paging/OutfitPagingSource.kt
class OutfitPagingSource(
    private val api: OutfitApi,
) : PagingSource<Int, OutfitModel>() {

    override fun getRefreshKey(state: PagingState<Int, OutfitModel>): Int? =
        state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
        }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, OutfitModel> {
        val page = params.key ?: FIRST_PAGE
        return try {
            val response = api.getOutfits(page = page, size = params.loadSize)
            LoadResult.Page(
                data = response.items.map { it.toDomain() },
                prevKey = if (page == FIRST_PAGE) null else page - 1,
                nextKey = if (response.items.isEmpty()) null else page + 1,
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    companion object {
        const val FIRST_PAGE = 1
        const val PAGE_SIZE = 20
        const val PREFETCH_DISTANCE = 5
    }
}
```

### Repository

```kotlin
// features/wardrobe/internal/data/repository/WardrobeRepositoryImpl.kt
class WardrobeRepositoryImpl(
    private val api: WardrobeApi,
) : WardrobeRepository {

    override fun getPagedClothes(category: String?): Flow<PagingData<ClotheModel>> =
        Pager(
            config = PagingConfig(
                pageSize = ClothePagingSource.PAGE_SIZE,
                prefetchDistance = ClothePagingSource.PREFETCH_DISTANCE,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = { ClothePagingSource(api, category) },
        ).flow
}
```

```kotlin
// features/wardrobe/internal/domain/repository/WardrobeRepository.kt
interface WardrobeRepository {
    fun getPagedClothes(category: String?): Flow<PagingData<ClotheModel>>
}
```

### Use Case

`PagingData` передаётся напрямую — `Result<T>` здесь не нужен, ошибки обрабатываются через `LoadState`.

```kotlin
// features/wardrobe/internal/domain/usecase/GetPagedClothesUseCase.kt
class GetPagedClothesUseCase(private val repository: WardrobeRepository) {
    operator fun invoke(category: String?): Flow<PagingData<ClotheModel>> =
        repository.getPagedClothes(category)
}
```

---

## Presentation Layer (FlowMVI)

### State и Intent

`PagingData` **не хранится в State** — он живёт в `Flow` внутри ViewModel и собирается в Composable.

```kotlin
// interactionModel/WardrobeState.kt
data class WardrobeState(
    val activeCategory: String? = null,
    val filterOptions: FilterOptions = FilterOptions(),
    val isRefreshing: Boolean = false,
) : MVIState

// interactionModel/WardrobeIntent.kt
sealed interface WardrobeIntent : MVIIntent {
    data class SelectCategory(val category: String?) : WardrobeIntent
    data class ApplyFilter(val options: FilterOptions) : WardrobeIntent
    data class ItemClicked(val clotheId: Int) : WardrobeIntent
    data class DeleteClothe(val id: Int) : WardrobeIntent
    data object AddClicked : WardrobeIntent
    data object Refresh : WardrobeIntent
}
```

### Store

```kotlin
// features/wardrobe/internal/presentation/list/WardrobeStore.kt
fun wardrobeStore(
    deleteClotheUseCase: DeleteClotheUseCase,
    onSideEffect: (WardrobeSideEffect) -> Unit,
): IStore<WardrobeState, WardrobeIntent, Nothing> = store(
    initial = WardrobeState()
) {
    reduce { intent ->
        when (intent) {
            is WardrobeIntent.SelectCategory ->
                updateState { copy(activeCategory = intent.category) }

            is WardrobeIntent.ApplyFilter ->
                updateState { copy(filterOptions = intent.options) }

            is WardrobeIntent.Refresh ->
                updateState { copy(isRefreshing = true) }

            is WardrobeIntent.ItemClicked ->
                onSideEffect(WardrobeSideEffect.NavigateToDetail(intent.clotheId))

            is WardrobeIntent.AddClicked ->
                onSideEffect(WardrobeSideEffect.ShowAddBottomSheet)

            is WardrobeIntent.DeleteClothe -> {
                deleteClotheUseCase(intent.id).fold(
                    onSuccess = { onSideEffect(WardrobeSideEffect.Refresh) },
                    onFailure = { e ->
                        onSideEffect(WardrobeSideEffect.ShowError(e.message ?: "Ошибка удаления"))
                    }
                )
            }
        }
    }
}
```

### ViewModel

ViewModel владеет `pagingFlow` — `Flow<PagingData<T>>` кешируется через `cachedIn(viewModelScope)`.  
При смене категории/фильтра создаётся новый flow через `flatMapLatest`.

```kotlin
// features/wardrobe/internal/presentation/list/WardrobeViewModel.kt
class WardrobeViewModel(
    getPagedClothesUseCase: GetPagedClothesUseCase,
    deleteClotheUseCase: DeleteClotheUseCase,
) : ViewModel() {

    private val _sideEffect = MutableSharedFlow<WardrobeSideEffect>(extraBufferCapacity = SIDE_EFFECT_BUFFER_CAPACITY)

    companion object {
        private const val SIDE_EFFECT_BUFFER_CAPACITY = 1
    }
    val sideEffect = _sideEffect.toSideEffect()

    val store = wardrobeStore(
        deleteClotheUseCase = deleteClotheUseCase,
        onSideEffect = { _sideEffect.tryEmit(it) },
    ).also { lifecycle.subscribe(it) }

    // Flow пересоздаётся при смене категории
    val pagingFlow: Flow<PagingData<ClotheModel>> = store.state
        .map { it.activeCategory }
        .distinctUntilChanged()
        .flatMapLatest { category -> getPagedClothesUseCase(category) }
        .cachedIn(viewModelScope)
}
```

---

## UI (Composable)

```kotlin
// features/wardrobe/internal/presentation/list/WardrobeScreen.kt
@Composable
fun WardrobeScreen(
    onNavigateToDetail: (clotheId: Int) -> Unit,
    onShowAddSheet: () -> Unit,
) {
    val container = koinViewModel<WardrobeViewModel>()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // PagingData собирается здесь — не в Store, не в ViewModel
    val lazyClothes = container.pagingFlow.collectAsLazyPagingItems()

    container.sideEffect.handle { effect ->
        when (effect) {
            is WardrobeSideEffect.NavigateToDetail -> onNavigateToDetail(effect.clotheId)
            is WardrobeSideEffect.ShowAddBottomSheet -> onShowAddSheet()
            is WardrobeSideEffect.ShowError -> scope.launch { snackbarHostState.showSnackbar(effect.message) }
            is WardrobeSideEffect.Refresh -> lazyClothes.refresh()
        }
    }

    val state by container.store.state.collectAsState()

    WardrobeContent(
        lazyClothes = lazyClothes,
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = { container.store.intent(it) },
    )
}

private const val GRID_COLUMNS = 2

@Composable
private fun WardrobeContent(
    lazyClothes: LazyPagingItems<ClotheModel>,
    state: WardrobeState,
    snackbarHostState: SnackbarHostState,
    onIntent: (WardrobeIntent) -> Unit,
) {
    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(GRID_COLUMNS),
            contentPadding = padding,
        ) {
            items(
                count = lazyClothes.itemCount,
                key = lazyClothes.itemKey { it.id },
                contentType = lazyClothes.itemContentType { "clothe" },
            ) { index ->
                val clothe = lazyClothes[index]
                ClotheCard(
                    clothe = clothe,
                    onClick = { clothe?.let { onIntent(WardrobeIntent.ItemClicked(it.id)) } },
                )
            }

            // LoadState — первая загрузка
            when (lazyClothes.loadState.refresh) {
                is LoadState.Loading -> item(span = { GridItemSpan(maxLineSpan) }) {
                    LoadingIndicator()
                }
                is LoadState.Error -> item(span = { GridItemSpan(maxLineSpan) }) {
                    ErrorRetryButton(onRetry = { lazyClothes.retry() })
                }
                else -> Unit
            }

            // LoadState — подгрузка следующей страницы
            when (lazyClothes.loadState.append) {
                is LoadState.Loading -> item(span = { GridItemSpan(maxLineSpan) }) {
                    LoadingIndicator()
                }
                is LoadState.Error -> item(span = { GridItemSpan(maxLineSpan) }) {
                    ErrorRetryButton(onRetry = { lazyClothes.retry() })
                }
                else -> Unit
            }
        }
    }
}
```

---

## Ручная пагинация (альтернатива)

Если Paging 3 избыточен (небольшие коллекции, офлайн-данные), используется ручная пагинация.  
Подходит для конструктора образа — коллекция вещей загружается целиком или с простым offset.

```kotlin
// State с ручной пагинацией
data class ConstructorState(
    val clothes: List<ClotheModel> = emptyList(),
    val isLoading: Boolean = false,
    val hasMore: Boolean = true,
    val currentPage: Int = 0,
) : MVIState

// Store
is ConstructorIntent.LoadMore -> {
    if (!withState { hasMore } || withState { isLoading }) return@reduce
    updateState { copy(isLoading = true) }
    getClothesPageUseCase(page = withState { currentPage }).fold(
        onSuccess = { newItems ->
            updateState {
                copy(
                    clothes = clothes + newItems,
                    isLoading = false,
                    hasMore = newItems.isNotEmpty(),
                    currentPage = currentPage + 1,
                )
            }
        },
        onFailure = { e ->
            updateState { copy(isLoading = false) }
            onSideEffect(ConstructorSideEffect.ShowError(e.message ?: "Ошибка"))
        }
    )
}

// UI — триггер при достижении конца списка
LazyColumn {
    items(state.clothes) { clothe -> ClotheCard(clothe) }
    if (state.hasMore) {
        item {
            LaunchedEffect(Unit) { onIntent(ConstructorIntent.LoadMore) }
            LoadingIndicator()
        }
    }
}
```

---

## Когда что использовать

| Сценарий | Подход |
|---|---|
| Гардероб (Одежда) — большой список с фильтрами | **Paging 3** |
| Образы (Outfits) — большой список | **Paging 3** |
| Конструктор образа — выбор вещей | Ручная пагинация или Paging 3 |
| Офлайн-данные из Room | **Paging 3** (через `PagingSource` из Room) |

---

## DI (Koin)

```kotlin
// features/wardrobe/di/WardrobeModule.kt
val wardrobeModule = module {
    single<WardrobeRepository> { WardrobeRepositoryImpl(get()) }
    single { GetPagedClothesUseCase(get()) }
    single { DeleteClotheUseCase(get()) }

    viewModel { WardrobeViewModel(get(), get()) }
    viewModel { ItemDetailViewModel(get(), get(), get()) }
    viewModel { ItemEditViewModel(get()) }
}
```

---

## Правила

1. `PagingData` никогда не хранится в `State` — только в `Flow` внутри ViewModel.
2. `cachedIn(viewModelScope)` обязателен — без него Flow пересоздаётся при каждом `collectAsLazyPagingItems`.
3. При смене фильтра/категории — `flatMapLatest` в ViewModel, не `refresh()` из UI.
4. `LoadState.refresh` — первичная загрузка / pull-to-refresh. `LoadState.append` — дозагрузка.
5. `LoadState.Error` всегда показывает кнопку `retry()` — не глотать ошибки молча.
6. Store не знает про `LazyPagingItems` и `PagingData` — только ViewModel и Composable.
