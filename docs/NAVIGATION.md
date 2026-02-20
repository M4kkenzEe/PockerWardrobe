# Навигация в проекте

Этот документ описывает архитектуру навигации в проекте PockerWardrobe.

## Используемые библиотеки

- **Jetpack Navigation Compose** (multiplatform) - основная навигация
- **kotlinx.serialization** - для typed routes

## Архитектура навигации

Навигация в проекте имеет **трёхуровневую структуру**:

```
AppNavHost (корневая навигация)
├── Authorization (экран авторизации)
└── Main (главный экран)
    └── BottomNavigationNavHost (таб-навигация)
        ├── Home (лента рекомендаций)
        ├── Shop → Wardrobe (гардероб)
        │   └── wardrobeNavGraph (вложенный граф)
        │       ├── Wardrobe (главный экран гардероба)
        │       ├── LookConstructor (конструктор образов)
        │       └── LookDetails (детали образа)
        └── Profile (профиль)
```

## Уровень 1: AppNavHost (корневая навигация)

Отвечает за переключение между авторизацией и основным приложением.

**Файл:** `features/card-feature/.../navigation/AppNavHost.kt`

```kotlin
@Composable
internal fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val tokenStorage: TokenStorage = koinInject()
    val token = tokenStorage.getToken()
    val isAuthorized = !token.isNullOrEmpty()

    // Определяем startDestination в зависимости от авторизации
    val startDestination = if (isAuthorized) {
        AppScreens.Main
    } else {
        AppScreens.Authorization
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier.fillMaxSize()
    ) {
        composable<AppScreens.Authorization> {
            AuthorizationScreen(
                openSession = {
                    navController.navigate(AppScreens.Main) {
                        popUpTo(AppScreens.Authorization) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable<AppScreens.Main> {
            MainScreen(parentNavController = navController)
        }
    }
}
```

**Определение экранов:**

**Файл:** `features/card-feature/.../navigation/AppScreens.kt`

```kotlin
internal sealed class AppScreens {
    @Serializable
    data object Authorization : AppScreens()

    @Serializable
    data object Main : AppScreens()
}
```

## Уровень 2: BottomNavigationNavHost (таб-навигация)

Отвечает за переключение между вкладками нижней навигации.

**Файл:** `features/card-feature/.../navigation/BottomNavigationNavHost.kt`

```kotlin
@Composable
internal fun BottomNavigationNavHost(
    navController: NavHostController,
    deepLink: DeepLink? = null,
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = BottomNavigationScreens.Home(),
        modifier = modifier.fillMaxSize()
    ) {
        composable<BottomNavigationScreens.Home> {
            HomeScreen()
        }

        composable<BottomNavigationScreens.Shop> {
            // Редирект на Wardrobe из вложенного графа
            LaunchedEffect(Unit) {
                navController.navigate(WardrobeNavScreens.Wardrobe) {
                    popUpTo(BottomNavigationScreens.Shop()) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }

        composable<BottomNavigationScreens.Profile> {
            ProfileScreen(onLogout = onLogout)
        }

        // Вложенный граф wardrobe
        wardrobeNavGraph(navController)
    }

    // Обработка deep links
    LaunchedEffect(deepLink) {
        if (deepLink is DeepLink.Look) {
            navController.navigate(
                WardrobeNavScreens.LookDetails(shareToken = deepLink.shareToken)
            )
        }
    }
}
```

**Определение экранов:**

**Файл:** `features/card-feature/.../navigation/BottomNavigationScreens.kt`

```kotlin
internal sealed class BottomNavigationScreens {
    abstract val label: String
    abstract val enabledIcon: String
    abstract val disabledIcon: String

    @Serializable
    data class Home(
        override val label: String = "Лента",
        override val enabledIcon: String = "shopping_cart_enabled",
        override val disabledIcon: String = "shopping_cart_disabled"
    ) : BottomNavigationScreens()

    @Serializable
    data class Shop(
        override val label: String = "Гардероб",
        override val enabledIcon: String = "shopping_cart_enabled",
        override val disabledIcon: String = "shopping_cart_disabled"
    ) : BottomNavigationScreens()

    @Serializable
    data class Profile(
        override val label: String = "Профиль",
        override val enabledIcon: String = "shopping_cart_enabled",
        override val disabledIcon: String = "shopping_cart_disabled"
    ) : BottomNavigationScreens()
}
```

## Уровень 3: Feature Navigation Graph

Каждая фича может иметь собственный navigation graph, который подключается к основной навигации.

### WardrobeNavGraph

**Файл:** `features/wardrobe-feature/.../navigation/WardrobeNavGraph.kt`

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
        val lookDetails: WardrobeNavScreens.LookDetails = backStackEntry.toRoute()
        LookDetailsScreen(
            lookId = lookDetails.lookId,
            shareToken = lookDetails.shareToken,
            onBackClick = { navController.popBackStack() }
        )
    }
}
```

**Определение экранов:**

**Файл:** `features/wardrobe-feature/.../navigation/WardrobeNavScreens.kt`

```kotlin
sealed class WardrobeNavScreens {
    @Serializable
    data object LookConstructor : WardrobeNavScreens()

    @Serializable
    data object Wardrobe : WardrobeNavScreens()

    @Serializable
    data class LookDetails(
        val lookId: Int? = null,
        val shareToken: String? = null
    ) : WardrobeNavScreens()
}
```

## Typed Routes (Type-Safe Navigation)

Проект использует typed routes с `@Serializable` аннотацией для type-safe навигации.

### Определение route:

```kotlin
@Serializable
data class LookDetails(
    val lookId: Int? = null,      // Опциональный параметр
    val shareToken: String? = null // Опциональный параметр
) : WardrobeNavScreens()
```

### Навигация с параметрами:

```kotlin
// Навигация по ID
navController.navigate(WardrobeNavScreens.LookDetails(lookId = 123))

// Навигация по shareToken (для deep links)
navController.navigate(WardrobeNavScreens.LookDetails(shareToken = "uuid-token"))
```

### Получение параметров в экране:

```kotlin
composable<WardrobeNavScreens.LookDetails> { backStackEntry ->
    val lookDetails: WardrobeNavScreens.LookDetails = backStackEntry.toRoute()

    LookDetailsScreen(
        lookId = lookDetails.lookId,
        shareToken = lookDetails.shareToken,
        onBackClick = { navController.popBackStack() }
    )
}
```

## Deep Links

Deep links позволяют открывать экраны приложения по URL.

### Архитектура

```
URL → DeepLinkManager → DeepLink → NavController → Screen
```

### DeepLink sealed class

**Файл:** `features/card-feature/.../deeplink/DeepLink.kt`

```kotlin
sealed class DeepLink {
    /**
     * Deep link к shared образу
     * @param shareToken UUID токен образа
     */
    data class Look(val shareToken: String) : DeepLink()

    // Будущие типы deep links:
    // data class Profile(val userId: String) : DeepLink()
    // data class Settings(val section: String) : DeepLink()
}
```

### DeepLinkManager

**Файл:** `features/card-feature/.../deeplink/DeepLinkManager.kt`

```kotlin
expect class DeepLinkManager() {
    /** Текущий deep link */
    val deepLinkFlow: StateFlow<DeepLink?>

    /** Pending deep link (для применения после авторизации) */
    val pendingDeepLink: StateFlow<DeepLink?>

    /** Обработать URL */
    fun handleDeepLinkUrl(url: String): Boolean

    /** Очистить текущий deep link */
    fun clearDeepLink()

    /** Установить pending deep link */
    fun setPendingDeepLink(deepLink: DeepLink?)

    /** Применить pending deep link */
    fun applyPendingDeepLink()
}
```

### Обработка deep link в навигации

```kotlin
// В BottomNavigationNavHost
LaunchedEffect(deepLink) {
    if (deepLink is DeepLink.Look) {
        navController.navigate(
            WardrobeNavScreens.LookDetails(shareToken = deepLink.shareToken)
        )
    }
}
```

### Формат URL

```
http://pocketwardrobe/share/{shareToken}
```

Пример: `http://pocketwardrobe/share/2aeb7515-d11d-4180-ab3c-3e73a13b99bb`

## Паттерны навигации

### 1. Переход с очисткой backstack

Используется при авторизации/логауте:

```kotlin
navController.navigate(AppScreens.Main) {
    popUpTo(AppScreens.Authorization) { inclusive = true }
    launchSingleTop = true
}
```

### 2. Переход назад

```kotlin
navController.popBackStack()
```

### 3. Переход с заменой текущего экрана

```kotlin
navController.navigate(WardrobeNavScreens.Wardrobe) {
    popUpTo(WardrobeNavScreens.LookConstructor) { inclusive = true }
}
```

### 4. Редирект на другой граф

```kotlin
composable<BottomNavigationScreens.Shop> {
    LaunchedEffect(Unit) {
        navController.navigate(WardrobeNavScreens.Wardrobe) {
            popUpTo(BottomNavigationScreens.Shop()) { inclusive = true }
            launchSingleTop = true
        }
    }
}
```

## Создание нового Navigation Graph

### 1. Создайте sealed class для экранов:

```kotlin
// features/my-feature/.../navigation/MyFeatureNavScreens.kt
sealed class MyFeatureNavScreens {
    @Serializable
    data object List : MyFeatureNavScreens()

    @Serializable
    data class Details(val id: Int) : MyFeatureNavScreens()
}
```

### 2. Создайте extension function для NavGraphBuilder:

```kotlin
// features/my-feature/.../navigation/MyFeatureNavGraph.kt
fun NavGraphBuilder.myFeatureNavGraph(navController: NavHostController) {
    composable<MyFeatureNavScreens.List> {
        MyListScreen(
            onItemClick = { id ->
                navController.navigate(MyFeatureNavScreens.Details(id))
            }
        )
    }

    composable<MyFeatureNavScreens.Details> { backStackEntry ->
        val details: MyFeatureNavScreens.Details = backStackEntry.toRoute()
        MyDetailsScreen(
            id = details.id,
            onBackClick = { navController.popBackStack() }
        )
    }
}
```

### 3. Подключите граф к основной навигации:

```kotlin
// В BottomNavigationNavHost или другом родительском NavHost
NavHost(...) {
    // Существующие routes...

    myFeatureNavGraph(navController)
}
```

## Структура файлов навигации

```
features/
├── card-feature/
│   └── .../navigation/
│       ├── AppNavHost.kt           # Корневая навигация
│       ├── AppScreens.kt           # Экраны корневого уровня
│       ├── BottomNavigationNavHost.kt  # Таб-навигация
│       ├── BottomNavigationScreens.kt  # Экраны табов
│       └── DeepLinkHandler.kt      # Обработчик deep links
│   └── .../deeplink/
│       ├── DeepLink.kt             # Типы deep links
│       └── DeepLinkManager.kt      # Менеджер deep links
└── wardrobe-feature/
    └── .../navigation/
        ├── WardrobeNavGraph.kt     # Граф фичи
        └── WardrobeNavScreens.kt   # Экраны фичи
```
