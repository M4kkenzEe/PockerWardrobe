# Верстка UI на Compose

Этот документ описывает принципы и практики создания UI с использованием Compose Multiplatform в проекте PockerWardrobe.

## Используемые библиотеки

- **UI**: Compose Multiplatform (Material Design)
- **Изображения**: Coil 3.x
- **DI**: Koin 4.x (koinViewModel, koinInject)
- **Навигация**: Jetpack Navigation Compose (multiplatform)
- **Состояние**: ViewModel + MutableStateFlow

## Компоненты UI

Используются компоненты из Material Design:

```kotlin
// Основные компоненты
import androidx.compose.material.{
    Button, FloatingActionButton, IconButton,
    TextField, OutlinedTextField, Checkbox,
    Text, Divider,
    Scaffold, ModalBottomSheetLayout,
    DropdownMenu, DropdownMenuItem,
    CircularProgressIndicator
}

// Layout компоненты
import androidx.compose.foundation.{
    Box, Column, Row, Spacer,
    LazyVerticalStaggeredGrid, LazyVerticalGrid,
    HorizontalPager
}
```

## Загрузка изображений

Для отображения изображений из сети используется `AsyncImage` из Coil 3.x.

**Пример использования:**
```kotlin
import coil3.compose.AsyncImage

@Composable
internal fun ClotheCard(
    clotheUrl: String = "",
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20))
            .background(Color.White)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = clotheUrl,
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
```

**Примеры в проекте:**
- `features/wardrobe-feature/.../compose/WardrobeCards.kt` - карточки вещей
- `features/wardrobe-feature/.../compose/LookDetailsScreen.kt` - детали образа

## Паттерн ViewModel + StateFlow

Для управления состоянием экранов используется ViewModel с MutableStateFlow.

### Структура ViewModel:

```kotlin
internal class WardrobeViewModel(
    private val useCase: WardrobeUseCase
) : ViewModel() {

    val clothes = MutableStateFlow<List<Clothe>>(emptyList())

    init {
        getClothes()
    }

    fun getClothes() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                useCase.getClothes()
            }.onSuccess {
                clothes.value = it
            }.onFailure { exception ->
                println("Error: ${exception.message}")
            }
        }
    }

    fun deleteClothe(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                useCase.deleteClothe(id)
            }.onSuccess {
                clothes.update { list -> list.filter { it.id != id } }
            }
        }
    }
}
```

### Использование в Composable:

```kotlin
@Composable
internal fun Wardrobe(
    viewModel: WardrobeViewModel = koinViewModel()
) {
    val clothes by viewModel.clothes.collectAsState()

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(clothes) { clothe ->
            ClotheCard(
                clotheUrl = clothe.imageUrl,
                onClick = { /* действие */ },
                onDelete = { viewModel.deleteClothe(clothe.id!!) }
            )
        }
    }
}
```

**Примеры в проекте:**
- `features/wardrobe-feature/.../viewmodel/WardrobeViewModel.kt`
- `features/wardrobe-feature/.../viewmodel/ConstructorViewModel.kt`
- `features/authorization/.../AuthorizationViewModel.kt`

### Enum для состояний экрана:

```kotlin
enum class ViewState {
    LOADING,
    DATA_OK,
    ERROR
}

// В ViewModel
val viewState = MutableStateFlow(ViewState.LOADING)

// В экране
@Composable
fun MyScreen(viewModel: MyViewModel = koinViewModel()) {
    val state by viewModel.viewState.collectAsState()

    when (state) {
        ViewState.LOADING -> CircularProgressIndicator()
        ViewState.ERROR -> ErrorView(onRetry = { viewModel.retry() })
        ViewState.DATA_OK -> ContentView()
    }
}
```

## Навигация

Используется Jetpack Navigation Compose с typed routes.

### Определение экранов:

```kotlin
// Screens definition
@Serializable
sealed class WardrobeNavScreens {
    @Serializable
    data object Wardrobe : WardrobeNavScreens()

    @Serializable
    data object LookConstructor : WardrobeNavScreens()

    @Serializable
    data class LookDetails(
        val lookId: Int? = null,
        val shareToken: String? = null
    ) : WardrobeNavScreens()
}
```

### Navigation Graph:

```kotlin
fun NavGraphBuilder.wardrobeNavGraph(navController: NavHostController) {
    composable<WardrobeNavScreens.Wardrobe> {
        WardrobeMainScreen(
            openConstructor = {
                navController.navigate(WardrobeNavScreens.LookConstructor)
            },
            openDetails = { lookId ->
                navController.navigate(WardrobeNavScreens.LookDetails(lookId = lookId))
            }
        )
    }

    composable<WardrobeNavScreens.LookConstructor> {
        LookConstructor(
            backClick = { navController.popBackStack() },
            navigateToSavedLooks = {
                navController.navigate(WardrobeNavScreens.Wardrobe) {
                    popUpTo(WardrobeNavScreens.LookConstructor) { inclusive = true }
                }
            }
        )
    }

    composable<WardrobeNavScreens.LookDetails> { backStackEntry ->
        val details: WardrobeNavScreens.LookDetails = backStackEntry.toRoute()
        LookDetailsScreen(
            lookId = details.lookId,
            shareToken = details.shareToken,
            onBackClick = { navController.popBackStack() }
        )
    }
}
```

**Примеры в проекте:**
- `features/card-feature/.../navigation/AppNavHost.kt` - корневая навигация
- `features/card-feature/.../navigation/BottomNavigationNavHost.kt` - табы
- `features/wardrobe-feature/.../navigation/WardrobeNavGraph.kt` - граф фичи

## Передача событий из UI

События передаются через callback параметры в Composable функциях.

### Правильный подход:

```kotlin
@Composable
internal fun ClotheCard(
    clotheUrl: String = "",
    onClick: () -> Unit = {},
    onDelete: () -> Unit = {},
    onShare: () -> Unit = {}
) {
    var dropDownMenuState by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .combinedClickable(
                onClick = onClick,
                onLongClick = { dropDownMenuState = true }
            )
    ) {
        AsyncImage(model = clotheUrl, contentDescription = null)

        DropdownMenu(
            expanded = dropDownMenuState,
            onDismissRequest = { dropDownMenuState = false }
        ) {
            DropdownMenuItem(
                onClick = {
                    onShare()
                    dropDownMenuState = false
                }
            ) { Text("Поделиться") }

            DropdownMenuItem(
                onClick = {
                    onDelete()
                    dropDownMenuState = false
                }
            ) { Text("Удалить") }
        }
    }
}

// Использование
ClotheCard(
    clotheUrl = clothe.imageUrl,
    onClick = { navigateToDetails(clothe.id) },
    onDelete = { viewModel.deleteClothe(clothe.id) },
    onShare = { viewModel.shareClothe(clothe.id) }
)
```

### Callback с параметрами:

```kotlin
@Composable
fun LoginScreen(
    isError: Boolean,
    onLogin: (username: String, password: String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Button(onClick = { onLogin(email, password) }) {
        Text("Войти")
    }
}
```

## Табы с HorizontalPager

Для экранов с переключаемыми табами используется `HorizontalPager`.

**Пример реализации:**
```kotlin
@Composable
fun WardrobeMainScreen(
    openConstructor: () -> Unit = {},
    openDetails: (Int) -> Unit = {}
) {
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { 2 }
    )
    val coroutineScope = rememberCoroutineScope()
    val tabs = listOf("Гардероб", "Образы")

    Column {
        // Табы
        TabRow(selectedTabIndex = pagerState.currentPage) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = { Text(title) }
                )
            }
        }

        // Контент
        HorizontalPager(state = pagerState) { page ->
            when (page) {
                0 -> Wardrobe()
                1 -> Looks(openDetails = openDetails)
            }
        }
    }
}
```

**Пример в проекте:**
- `features/wardrobe-feature/.../WardrobeMainScreen.kt`

## Preview функции

Для каждого экрана рекомендуется создавать Preview функции.

```kotlin
import androidx.compose.ui.tooling.preview.Preview

@Preview(showBackground = true)
@Composable
private fun ClotheCardPreview() {
    ClotheCard(
        clotheUrl = "https://example.com/image.jpg",
        onClick = {},
        onDelete = {},
        onShare = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun WardrobePreview() {
    // Для preview можно использовать mock данные
    val mockClothes = listOf(
        Clothe(id = 1, imageUrl = "url1"),
        Clothe(id = 2, imageUrl = "url2")
    )
    // Preview content
}
```

## Маппинг данных

**ВАЖНО**: Маппинг данных в Composable функциях **ЗАПРЕЩЕН**. Весь маппинг должен выполняться в ViewModel или Mapper классах.

### Неправильно - маппинг в Compose:
```kotlin
@Composable
private fun LooksGrid(looks: List<LookResponse>) {
    LazyVerticalGrid(...) {
        items(looks) { response ->
            // ЗАПРЕЩЕНО: маппинг в Compose
            val look = Look(
                id = response.id,
                url = response.imageUrl.orEmpty(),
                items = response.items.map { it.toUiModel() }
            )
            LookCard(look = look)
        }
    }
}
```

### Правильно - маппинг в ViewModel:
```kotlin
// ViewModel
class LooksViewModel(private val useCase: LookUseCase) : ViewModel() {
    val looks = MutableStateFlow<List<Look>>(emptyList())

    fun getLooks() {
        viewModelScope.launch(Dispatchers.IO) {
            val response = useCase.getLooks()
            // Маппинг в ViewModel
            looks.value = response.map { it.toDomain() }
        }
    }
}

// Composable - просто отображает готовые данные
@Composable
private fun LooksGrid(looks: List<Look>) {
    LazyVerticalGrid(...) {
        items(looks) { look ->
            LookCard(look = look)
        }
    }
}
```

### Причины:
1. **Производительность** - маппинг в Compose выполняется при каждой рекомпозиции
2. **Тестирование** - логику маппинга легче протестировать в ViewModel
3. **Разделение ответственности** - Compose отвечает только за отображение

## UIState для сложных данных

Для сложных UI состояний используйте data class с аннотацией `@Immutable`:

```kotlin
@Immutable
internal data class LookItemUiState(
    val id: Int,
    val imgUrl: String,
    val offsetX: Float = 100f,
    val offsetY: Float = 100f,
    val zIndex: Float = 0f,
    val size: Dp = 170.dp,
    val rotation: Float = 0f,
    val isSelected: Boolean = false
)
```

**Пример в проекте:**
- `features/wardrobe-feature/.../compose/LookItemUiState.kt`
