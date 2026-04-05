# Изменения навигации

## Принятые решения

- Навигация строится на **Navigation 3** (`androidx.navigation3`) — стабильная Multiplatform-версия
- **Bottom navigation bar отсутствует** — в дизайне его нет
- Главный экран — `MainClothisScreen`: TopBar + tab switcher ("Одежда" / "Образы") внутри контента
- Профиль открывается по кнопке в TopBar → push-навигация в `ProfileScreen`
- Все остальные экраны (деталка, редактирование, конструктор) открываются поверх через back stack
- Единая точка входа `AppNavHost` — для поддержки deeplinks
- Deeplinks — в модуле `core/deeplink` (паттерн: `DeeplinkRoute` sealed class + `DeeplinkPathParser` + `DeeplinkPostfix`)
- Навигационные маршруты каждой фичи — sealed классы, объявленные в `external/`

---

## Структура навигации

```
AppNavHost (Navigation 3, единый back stack)
├── AppRoutes.Auth
│   └── AuthScreen → onSuccess → backStack.add(AppRoutes.Main)
│
└── AppRoutes.Main
    └── MainClothisScreen
        ├── TopBar
        │   ├── Заголовок "Clothis"
        │   └── Иконка профиля → backStack.add(ProfileRoutes.Main)
        └── Tab switcher
            ├── "Одежда" → WardrobeScreen
            │   └── onItemClick → backStack.add(WardrobeRoutes.ItemDetail(id))
            └── "Образы" → OutfitScreen
                ├── onLookClick  → backStack.add(OutfitRoutes.Detail(id))
                └── onFabClick   → backStack.add(OutfitRoutes.Constructor())

// Все экраны — в том же back stack
├── WardrobeRoutes.ItemDetail(clotheId)
│   └── ItemDetailScreen → onEdit → backStack.add(WardrobeRoutes.ItemEdit(clotheId))
│                        → onLookClick → backStack.add(OutfitRoutes.Detail(lookId))
├── WardrobeRoutes.ItemEdit(clotheId)
│   └── ItemEditScreen → onBack → backStack.removeLast()
├── OutfitRoutes.Detail(lookId)
│   └── OutfitDetailScreen → onEdit → backStack.add(OutfitRoutes.Constructor(lookId))
│                          → onItemClick → backStack.add(WardrobeRoutes.ItemDetail(clotheId))
├── OutfitRoutes.Constructor(lookId?)
│   └── OutfitConstructorScreen → onBack/onSaved → backStack.removeLast()
├── ProfileRoutes.Main
│   └── ProfileScreen → onEdit  → backStack.add(ProfileRoutes.Edit)
│                     → onSizes → backStack.add(ProfileRoutes.Sizes)
├── ProfileRoutes.Edit
│   └── EditProfileScreen → onBack → backStack.removeLast()
└── ProfileRoutes.Sizes
    └── SizesScreen → onBack → backStack.removeLast()
```

---

## MainClothisScreen — главный экран

`MainClothisScreen` — единственный "постоянный" экран. Не пересоздаётся при навигации вперёд.

```kotlin
// features/main/src/commonMain/.../MainClothisScreen.kt
@Composable
fun MainClothisScreen(backStack: NavBackStack) {
    var activeTab by rememberSaveable { mutableStateOf(WardrobeTab.CLOTHING) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Clothis",
                trailingIcon = {
                    IconButton(onClick = { backStack.add(ProfileRoutes.Main) }) {
                        Icon(MR.images.user)
                    }
                }
            )
        }
    ) {
        Column {
            // Сегментированный контрол
            TabSwitcher(
                tabs = listOf("Одежда", "Образы"),
                activeIndex = activeTab.ordinal,
                onTabSelected = { activeTab = WardrobeTab.entries[it] },
            )

            when (activeTab) {
                WardrobeTab.CLOTHING -> WardrobeScreen(
                    onNavigateToDetail = { backStack.add(WardrobeRoutes.ItemDetail(it)) },
                    onShowConstructor = { backStack.add(OutfitRoutes.Constructor()) },
                )
                WardrobeTab.OUTFITS -> OutfitScreen(
                    onNavigateToDetail = { backStack.add(OutfitRoutes.Detail(it)) },
                    onNavigateToConstructor = { backStack.add(OutfitRoutes.Constructor()) },
                )
            }
        }
    }
}
```

---

## AppNavHost — регистрация всех маршрутов

```kotlin
// features/main/src/commonMain/.../AppNavHost.kt
@Composable
fun AppNavHost() {
    val backStack = rememberNavBackStack(start = AppRoutes.Auth)

    NavDisplay(backStack = backStack) { key ->
        when (key) {
            is AppRoutes.Auth -> AuthScreen(
                onSuccess = { backStack.add(AppRoutes.Main) }
            )
            is AppRoutes.Main -> MainClothisScreen(backStack = backStack)

            // Wardrobe
            is WardrobeRoutes.ItemDetail -> ItemDetailScreen(
                clotheId = key.clotheId,
                onBack = { backStack.removeLast() },
                onNavigateToEdit = { backStack.add(WardrobeRoutes.ItemEdit(it)) },
                onNavigateToLook = { backStack.add(OutfitRoutes.Detail(it)) },
            )
            is WardrobeRoutes.ItemEdit -> ItemEditScreen(
                clotheId = key.clotheId,
                onBack = { backStack.removeLast() },
            )

            // Outfit
            is OutfitRoutes.Detail -> OutfitDetailScreen(
                lookId = key.lookId,
                onBack = { backStack.removeLast() },
                onNavigateToItem = { backStack.add(WardrobeRoutes.ItemDetail(it)) },
                onNavigateToConstructor = { backStack.add(OutfitRoutes.Constructor(key.lookId)) },
            )
            is OutfitRoutes.Constructor -> OutfitConstructorScreen(
                lookId = key.lookId,
                onBack = { backStack.removeLast() },
                onSaved = { backStack.removeLast() },
            )

            // Profile
            is ProfileRoutes.Main -> ProfileScreen(
                onBack = { backStack.removeLast() },
                onNavigateToEdit = { backStack.add(ProfileRoutes.Edit) },
                onNavigateToSizes = { backStack.add(ProfileRoutes.Sizes) },
            )
            is ProfileRoutes.Edit -> EditProfileScreen(
                onBack = { backStack.removeLast() }
            )
            is ProfileRoutes.Sizes -> SizesScreen(
                onBack = { backStack.removeLast() }
            )
        }
    }
}
```

---

## Навигационные маршруты (по модулям)

Каждая фича объявляет маршруты как **sealed class** в `external/`.

```kotlin
// features/app/external/AppRoutes.kt
sealed class AppRoutes {
    @Serializable data object Auth : AppRoutes()
    @Serializable data object Main : AppRoutes()
}

// features/wardrobe/external/WardrobeRoutes.kt
sealed class WardrobeRoutes {
    @Serializable data object Main : WardrobeRoutes()
    @Serializable data class ItemDetail(val clotheId: Int) : WardrobeRoutes()
    @Serializable data class ItemEdit(val clotheId: Int) : WardrobeRoutes()
}

// features/outfit/external/OutfitRoutes.kt
sealed class OutfitRoutes {
    @Serializable data object Main : OutfitRoutes()
    @Serializable data class Detail(val lookId: Int) : OutfitRoutes()
    @Serializable data class Constructor(val lookId: Int? = null) : OutfitRoutes()
}

// features/profile/external/ProfileRoutes.kt
sealed class ProfileRoutes {
    @Serializable data object Main : ProfileRoutes()
    @Serializable data object Edit : ProfileRoutes()
    @Serializable data object Sizes : ProfileRoutes()
}
```

---

## Tab switcher — сегментированный контрол

Кнопки "Одежда" / "Образы" внутри `MainClothisScreen`. Стиль — две кнопки в общем rounded-контейнере, активная кнопка белая.

```kotlin
// core/compose/components/TabSwitcher.kt
@Composable
fun TabSwitcher(
    tabs: List<String>,
    activeIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .background(Theme.colors.component.tabBackground, shape = RoundedCornerShape(12.dp))
            .padding(4.dp),
    ) {
        tabs.forEachIndexed { index, label ->
            val isActive = index == activeIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        color = if (isActive) Theme.colors.background.primary else Color.Transparent,
                        shape = RoundedCornerShape(10.dp),
                    )
                    .clickable { onTabSelected(index) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = Theme.typography.body2,
                    color = if (isActive) Theme.colors.label.primary else Theme.colors.label.secondary,
                )
            }
        }
    }
}
```

---

## Deep Links

Deeplinks вынесены в модуль `core/deeplink`.

### Архитектура

```
URL → IDeeplinkHandler → DeeplinkRoute → backStack.add(Route)
```

### DeeplinkPostfix — перечень сегментов пути

```kotlin
// core/deeplink/src/commonMain/.../DeeplinkPostfix.kt
enum class DeeplinkPostfix(val postfix: String) {
    ITEM("item"),
    LOOK("look"),
    PROFILE("profile"),
    NONE("");

    companion object {
        fun from(postfix: String?): DeeplinkPostfix =
            entries.find { it.postfix == postfix } ?: NONE
    }
}
```

### DeeplinkRoute — типы маршрутов

```kotlin
// core/deeplink/src/commonMain/.../DeeplinkRoute.kt
sealed class DeeplinkRoute {
    data class ItemDetail(val clotheId: Int) : DeeplinkRoute()
    data class LookDetail(val lookId: Int) : DeeplinkRoute()
    data object Profile : DeeplinkRoute()
    data object Unknown : DeeplinkRoute()
}
```

### DeeplinkPathParser — разбор пути

```kotlin
// core/deeplink/src/commonMain/.../DeeplinkPathParser.kt
class DeeplinkPathParser(private val path: String) {

    private var segments = emptyList<String>()
    private var cursor = 0

    fun parse(): DeeplinkRoute {
        cursor = 0
        segments = path.split("/").filter { it.isNotEmpty() }
        return parseRoute()
    }

    private fun parseRoute(): DeeplinkRoute = when (nextSegment()) {
        DeeplinkPostfix.ITEM.postfix    -> parseItemDetail()
        DeeplinkPostfix.LOOK.postfix    -> parseLookDetail()
        DeeplinkPostfix.PROFILE.postfix -> DeeplinkRoute.Profile
        else -> DeeplinkRoute.Unknown
    }

    private fun parseItemDetail(): DeeplinkRoute {
        val id = nextSegment()?.toIntOrNull() ?: return DeeplinkRoute.Unknown
        return DeeplinkRoute.ItemDetail(id)
    }

    private fun parseLookDetail(): DeeplinkRoute {
        val id = nextSegment()?.toIntOrNull() ?: return DeeplinkRoute.Unknown
        return DeeplinkRoute.LookDetail(id)
    }

    private fun nextSegment(): String? = segments.getOrNull(cursor++)
    private fun peekSegment(): String? = segments.getOrNull(cursor)
}
```

### IDeeplinkHandler — интерфейс обработчика

```kotlin
// core/deeplink/src/commonMain/.../IDeeplinkHandler.kt
interface IDeeplinkHandler {
    fun canHandle(uri: String): DeeplinkRoute
    fun handle(route: DeeplinkRoute, backStack: NavBackStack)
}
```

### DeepLinkManager — хранение pending deeplink

```kotlin
// core/deeplink/src/commonMain/.../DeepLinkManager.kt
object DeepLinkManager {
    private val _pendingDeeplink = MutableStateFlow<String?>(null)
    val pendingDeeplink: StateFlow<String?> = _pendingDeeplink

    fun emit(uri: String) { _pendingDeeplink.tryEmit(uri) }
    fun onHandled() { _pendingDeeplink.tryEmit(null) }
}
```

### Обработка deep link в AppNavHost

```kotlin
// В AppNavHost, после создания backStack
LaunchedEffect(Unit) {
    DeepLinkManager.pendingDeeplink.filterNotNull().collect { uri ->
        val route = DeeplinkPathParser(
            uri.removePrefix("clothis://")
        ).parse()
        when (route) {
            is DeeplinkRoute.ItemDetail -> backStack.add(WardrobeRoutes.ItemDetail(route.clotheId))
            is DeeplinkRoute.LookDetail -> backStack.add(OutfitRoutes.Detail(route.lookId))
            is DeeplinkRoute.Profile    -> backStack.add(ProfileRoutes.Main)
            is DeeplinkRoute.Unknown    -> Unit
        }
        DeepLinkManager.onHandled()
    }
}
```

### Формат URL

```
clothis://item/{clotheId}      → ItemDetailScreen
clothis://look/{lookId}        → OutfitDetailScreen
clothis://profile              → ProfileScreen
```

---

## Зависимости (Gradle)

```kotlin
// androidApp/build.gradle.kts
implementation("androidx.navigation3:navigation3-ui:1.0.0-alpha02")
implementation("androidx.navigation3:navigation3-runtime:1.0.0-alpha02")
```

Версию уточнить по актуальной стабильной в `libs.versions.toml`.
