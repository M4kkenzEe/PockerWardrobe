# Изменения навигации

## Принятые решения

- Навигация строится на **Navigation 3** (`androidx.navigation3`) — стабильная Multiplatform-версия
- **Bottom navigation bar отсутствует** — в дизайне его нет
- Главный экран — `MyItemsScreen`: TopBar + tab switcher ("Одежда" / "Образы") внутри контента
- Профиль открывается по кнопке в TopBar → push-навигация в `ProfileScreen`
- Все остальные экраны (деталка, редактирование, конструктор) открываются поверх через back stack
- Единая точка входа `AppNavHost` — для поддержки deeplinks
- Deeplinks — в модуле `core/deeplink`
- Навигационный граф каждой фичи — в её пакете `external/`

---

## Структура навигации

```
AppNavHost (Navigation 3, единый back stack)
├── AuthRoute
│   └── AuthScreen → onSuccess → backStack.add(MainRoute)
│
└── MainRoute
    └── MyItemsScreen
        ├── TopBar
        │   ├── Заголовок "Clothis"
        │   └── Иконка профиля → backStack.add(ProfileMain)
        └── Tab switcher
            ├── "Одежда" → WardrobeScreen
            │   └── onItemClick → backStack.add(ItemDetail(id))
            └── "Образы" → OutfitScreen
                ├── onLookClick  → backStack.add(OutfitDetail(id))
                └── onFabClick   → backStack.add(OutfitConstructorRoute())

// Все экраны — в том же back stack
├── ItemDetail(clotheId)
│   └── ItemDetailScreen → onEdit → backStack.add(ItemEdit(clotheId))
│                        → onLookClick → backStack.add(OutfitDetail(lookId))
├── ItemEdit(clotheId)
│   └── ItemEditScreen → onBack → backStack.removeLast()
├── OutfitDetail(lookId)
│   └── OutfitDetailScreen → onEdit → backStack.add(OutfitConstructorRoute(lookId))
│                          → onItemClick → backStack.add(ItemDetail(clotheId))
├── OutfitConstructorRoute(lookId?)
│   └── OutfitConstructorScreen → onBack/onSaved → backStack.removeLast()
├── ProfileMain
│   └── ProfileScreen → onEdit  → backStack.add(EditProfile)
│                     → onSizes → backStack.add(Sizes)
├── EditProfile
│   └── EditProfileScreen → onBack → backStack.removeLast()
└── Sizes
    └── SizesScreen → onBack → backStack.removeLast()
```

---

## MyItemsScreen — главный экран

`MyItemsScreen` — единственный "постоянный" экран. Не пересоздаётся при навигации вперёд.

```kotlin
// features/main/src/commonMain/.../MyItemsScreen.kt
@Composable
fun MyItemsScreen(backStack: NavBackStack) {
    var activeTab by rememberSaveable { mutableStateOf(WardrobeTab.CLOTHING) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Clothis",
                trailingIcon = {
                    IconButton(onClick = { backStack.add(ProfileMain) }) {
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
                    onNavigateToDetail = { backStack.add(ItemDetail(it)) },
                    onShowConstructor = { backStack.add(OutfitConstructorRoute()) },
                )
                WardrobeTab.OUTFITS -> OutfitScreen(
                    onNavigateToDetail = { backStack.add(OutfitDetail(it)) },
                    onNavigateToConstructor = { backStack.add(OutfitConstructorRoute()) },
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
    val backStack = rememberNavBackStack(start = AuthRoute)

    NavDisplay(backStack = backStack) { key ->
        when (key) {
            is AuthRoute -> AuthScreen(
                onSuccess = { backStack.add(MainRoute) }
            )
            is MainRoute -> MyItemsScreen(backStack = backStack)

            // Wardrobe
            is ItemDetail -> ItemDetailScreen(
                clotheId = key.clotheId,
                onBack = { backStack.removeLast() },
                onNavigateToEdit = { backStack.add(ItemEdit(it)) },
                onNavigateToLook = { backStack.add(OutfitDetail(it)) },
            )
            is ItemEdit -> ItemEditScreen(
                clotheId = key.clotheId,
                onBack = { backStack.removeLast() },
            )

            // Outfit
            is OutfitDetail -> OutfitDetailScreen(
                lookId = key.lookId,
                onBack = { backStack.removeLast() },
                onNavigateToItem = { backStack.add(ItemDetail(it)) },
                onNavigateToConstructor = { backStack.add(OutfitConstructorRoute(key.lookId)) },
            )

            // Outfit Constructor
            is OutfitConstructorRoute -> OutfitConstructorScreen(
                lookId = key.lookId,
                onBack = { backStack.removeLast() },
                onSaved = { backStack.removeLast() },
            )

            // Profile
            is ProfileMain -> ProfileScreen(
                onBack = { backStack.removeLast() },
                onNavigateToEdit = { backStack.add(EditProfile) },
                onNavigateToSizes = { backStack.add(Sizes) },
            )
            is EditProfile -> EditProfileScreen(
                onBack = { backStack.removeLast() }
            )
            is Sizes -> SizesScreen(
                onBack = { backStack.removeLast() }
            )
        }
    }
}
```

---

## Навигационные маршруты (по модулям)

Каждая фича объявляет свои маршруты в `external/`.

```kotlin
// features/wardrobe/external/WardrobeRoutes.kt
@Serializable object WardrobeMain
@Serializable data class ItemDetail(val clotheId: Int)
@Serializable data class ItemEdit(val clotheId: Int)

// features/outfit/external/OutfitRoutes.kt
@Serializable object OutfitMain
@Serializable data class OutfitDetail(val lookId: Int)

// features/outfit_constructor/external/OutfitConstructorRoutes.kt
@Serializable data class OutfitConstructorRoute(val lookId: Int? = null)

// features/profile/external/ProfileRoutes.kt
@Serializable object ProfileMain
@Serializable object EditProfile
@Serializable object Sizes

// features/main/external/MainRoutes.kt
@Serializable object AuthRoute
@Serializable object MainRoute
```

---

## Tab switcher — сегментированный контрол

Кнопки "Одежда" / "Образы" внутри `MyItemsScreen`. Стиль — две кнопки в общем rounded-контейнере, активная кнопка белая.

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

```kotlin
// core/deeplink/src/commonMain/.../DeepLinkManager.kt
class DeepLinkManager {
    fun resolve(uri: String): Any? = when {
        uri.startsWith("clothis://item/")    -> ItemDetail(uri.substringAfterLast("/").toInt())
        uri.startsWith("clothis://look/")    -> OutfitDetail(uri.substringAfterLast("/").toInt())
        uri.startsWith("clothis://profile")  -> ProfileMain
        else -> null
    }
}
```

При получении deep link — добавить маршрут в `backStack`.

---

## Зависимости (Gradle)

```kotlin
// androidApp/build.gradle.kts
implementation("androidx.navigation3:navigation3-ui:1.0.0")
implementation("androidx.navigation3:navigation3-runtime:1.0.0")
```

Версию уточнить по актуальной стабильной в `libs.versions.toml`.
