# Системные компоненты (Design System)

Компоненты которые используются на нескольких экранах — выносятся в `core/compose/components`.  
Согласно `change_documentation.txt` п.3: "Системные компоненты мы вынесем в `core/compose/components`".

## Где размещать

Размещать в модуле `core/compose` рядом с `Theme.kt`:

```
core/compose/src/commonMain/kotlin/.../
├── theme/
│   └── Theme.kt, Color.kt, ...
└── components/
    ├── AppTopBar.kt
    ├── BottomSheet.kt
    ├── CategoryChips.kt
    ├── ItemCard.kt
    ├── LookCard.kt
    ├── MoreMenu.kt
    ├── SettingRow.kt
    ├── TagChip.kt
    ├── SkeletonCard.kt
    └── ...
```

> Правила: использовать только `Theme.colors.*`, `Theme.typography.*`, `MR.images.*` — согласно `docs/RESOURCES.md`.

---

## 1. AppTopBar

Унифицированная шапка экрана.

**Используется на:** ItemDetail, ItemEdit, OutfitDetail, Constructor, Profile, Sizes, EditProfile

```kotlin
@Composable
fun AppTopBar(
    title: String,
    onBack: (() -> Unit)? = null,        // null = нет кнопки назад
    trailingIcon: @Composable (() -> Unit)? = null,  // иконка справа
)
```

**Внешний вид:**
- Высота 56dp
- Кнопка назад: round button 36dp, `MR.images.chevron_left`, `Theme.colors.component.button`
- Заголовок: по центру, `Theme.typography.body1`, `Theme.colors.label.primary`
- Border-bottom: `Theme.colors.stroke.primary`

---

## 2. BottomSheet (AppBottomSheet)

Универсальная нижняя шторка.

**Используется на:** AddBottomSheet, FilterBottomSheet, ItemPickerSheet

```kotlin
@Composable
fun AppBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit,
)
```

**Внешний вид:**
- Backdrop: `rgba(0,0,0,0.4)` при открытии
- Шторка: `Theme.colors.background.primary`, `borderTopRadius = 24dp`
- Handle: полоска 36x4dp, `Theme.colors.stroke.primary`, по центру
- Анимация: slide from bottom

---

## 3. CategoryChips

Горизонтальный скролл чипсов для фильтрации категорий.

**Используется на:** WardrobeScreen, OutfitConstructor (ItemPickerSheet), OutfitScreen

```kotlin
@Composable
fun CategoryChips(
    categories: List<String>,
    activeCategory: String?,
    onCategorySelect: (String?) -> Unit,
)
```

**Внешний вид:**
- Горизонтальный LazyRow, без скролл-индикатора
- Чип неактивный: `Theme.colors.component.chip`, `Theme.colors.label.secondary`
- Чип активный: `Theme.colors.component.chipActive`, `Theme.colors.label.onAccent`
- Скругление полное (rounded-full)

---

## 4. ItemCard

Карточка вещи для гардероба.

**Используется на:** WardrobeScreen, OutfitDetail (вещи в образе), ItemPickerSheet (вариант 3 колонки)

```kotlin
@Composable
fun ItemCard(
    imageUrl: String,
    name: String,
    category: String?,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,     // long press = меню
    selectionMode: Boolean = false,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier,
)
```

**Внешний вид:**
- Rounded corners 16dp
- Фото: aspect ratio 1:1, белый фон для фото
- Имя: `Theme.typography.caption`, `Theme.colors.label.primary`
- Тег категории: `TagChip` — см. ниже
- В режиме выбора: чекбокс кружок в правом верхнем углу

---

## 5. LookCard

Карточка образа.

**Используется на:** OutfitScreen (Looks)

```kotlin
@Composable
fun LookCard(
    imageUrl: String,
    name: String,
    isLocked: Boolean = false,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,  // кнопка ⋯
)
```

**Внешний вид:**
- Rounded corners 16dp
- Фото: aspect ratio 1:1
- Нижняя панель: имя + кнопка `⋯`
- Locked: размытие + overlay с иконкой замка и кнопкой "Подписка"

---

## 6. MoreMenu

Выпадающее меню по нажатию `⋯`.

**Используется на:** LookCard, OutfitDetailScreen (заголовок)

```kotlin
data class MoreMenuItem(
    val icon: ImageResource,
    val label: String,
    val isDestructive: Boolean = false,
    val action: () -> Unit,
)

@Composable
fun MoreMenu(
    visible: Boolean,
    items: List<MoreMenuItem>,
    onDismiss: () -> Unit,
    anchor: @Composable () -> Unit,
)
```

**Внешний вид:**
- Всплывает от точки привязки (anchor)
- Фон: `Theme.colors.background.secondary`, border: `Theme.colors.stroke.primary`
- Каждый элемент: иконка + текст, разделитель
- Деструктивный (удалить): красный цвет (`#EF4444`)

---

## 7. TagChip

Маленький чип-тег.

**Используется на:** ItemCard (категория), ItemDetailScreen (теги), OutfitDetailScreen (теги образа)

```kotlin
@Composable
fun TagChip(
    text: String,
    modifier: Modifier = Modifier,
)
```

**Внешний вид:**
- `Theme.colors.component.tag` фон
- `Theme.colors.component.tagFg` цвет текста
- `Theme.typography.tiny`
- Горизонтальные padding 8dp, вертикальные 3dp

---

## 8. SettingRow

Строка настройки с иконкой, заголовком, подзаголовком и стрелкой.

**Используется на:** ProfileScreen (Мой стиль, Размеры, Уведомления)

```kotlin
@Composable
fun SettingRow(
    icon: ImageResource,
    title: String,
    subtitle: String? = null,
    isLast: Boolean = false,
    onClick: () -> Unit = {},
)
```

**Внешний вид:**
- Иконка: 36x36dp rounded, `Theme.colors.background.secondary`
- Заголовок: `Theme.typography.body1`
- Подзаголовок: `Theme.typography.caption`, `Theme.colors.label.tertiary`
- Стрелка: `MR.images.chevron_right`, `Theme.colors.label.tertiary`
- Разделитель (если не `isLast`): indent от левого края иконки

---

## 9. StatItem

Элемент статистики (число + подпись).

**Используется на:** ProfileScreen (Образов | Вещей | Поделились)

```kotlin
@Composable
fun StatItem(
    value: String,
    label: String,
    showDivider: Boolean = true,
)
```

---

## 10. SkeletonCard

Карточка-заглушка для состояния загрузки.

**Используется на:** WardrobeScreen (loading), OutfitScreen (loading, AI generating)

```kotlin
@Composable
fun SkeletonCard(
    aspectRatio: Float = 1f,
    modifier: Modifier = Modifier,
)
```

**Внешний вид:**
- Фон: `Theme.colors.component.skeleton` с анимацией пульсации
- Rounded corners 16dp
- Под изображением: 2 строки текста-скелетона

---

## 11. MarketplaceLinkRow

Строка со ссылкой на маркетплейс.

**Используется на:** ItemDetailScreen

```kotlin
@Composable
fun MarketplaceLinkRow(
    url: String,
    onClick: () -> Unit,
)
```

Логика определения названия маркетплейса и цвета иконки по домену:
- `wildberries.ru` → "Wildberries", #CB11AB
- `ozon.ru` → "Ozon", #005BFF
- Остальные → домен, нейтральный цвет

---

## 12. SizeSelector

Горизонтальные кнопки выбора размера.

**Используется на:** ItemEditScreen, (SizePickerScreen)

```kotlin
@Composable
fun SizeSelector(
    sizes: List<String>,
    selectedSize: String?,
    onSizeSelect: (String) -> Unit,
)
```

**Внешний вид:**
- Кнопка неактивная: `Theme.colors.background.secondary`, border
- Кнопка активная: `Theme.colors.accent.primary`, текст `Theme.colors.label.onAccent`

---

## Иконки

Все нужные иконки **уже есть** в `core/resources/moko-resources/images/` и доступны через `MR.images.*`:

| Иконка | MR.images |
|---|---|
| Назад | `MR.images.chevron_left` |
| Вперёд | `MR.images.chevron_right` |
| Три точки | `MR.images.more_horizontal` |
| Карандаш | `MR.images.pencil` |
| Корзина | `MR.images.trash_2` |
| Закрыть | `MR.images.x` |
| Плюс | `MR.images.plus` |
| Галочка | `MR.images.check` |
| Поделиться | `MR.images.share_2` |
| Закладка | `MR.images.bookmark` |
| Замок | `MR.images.lock` |
| Корона | `MR.images.crown` |
| Выйти | `MR.images.log_out` |
| Камера | `MR.images.camera` |
| Галерея | `MR.images.image` |
| Ссылка | `MR.images.link_2` |
| Внешняя ссылка | `MR.images.external_link` |
| Фильтр | `MR.images.sliders_horizontal` |
| Увеличить | `MR.images.zoom_in` |
| Уменьшить | `MR.images.zoom_out` |
| Сброс | `MR.images.rotate_ccw` |
| Тег | `MR.images.tag` |
| Звёздочка | `MR.images.sparkles` |
| Луна/Солнце | `MR.images.moon` / `MR.images.sun` |
| Линейка | `MR.images.ruler` |
| Звезда | `MR.images.star` |
| Колокол | `MR.images.bell` |

---

## Токены дизайн-системы

Существующие токены в `core/compose/theme/` (уже определены):

| Токен | Где используется |
|---|---|
| `Theme.colors.component.chip.background` | CategoryChips неактивный |
| `Theme.colors.component.chip.activeBackground` | CategoryChips активный |
| `Theme.colors.component.chip.foreground` | CategoryChips текст неактивный |
| `Theme.colors.component.chip.activeForeground` | CategoryChips текст активный |
| `Theme.colors.component.skeleton` | SkeletonCard |
| `Theme.colors.component.fab.background` | FAB кнопка |
| `Theme.colors.component.fab.foreground` | FAB иконка |
| `Theme.colors.accent.subtle` | Акцентный фон (бейджи, тонкие выделения) |
| `Theme.colors.accent.onAccent` | Текст/иконка на акцентном цвете |
| `Theme.colors.label.primary` | Основной текст |
| `Theme.colors.label.secondary` | Вторичный текст |
| `Theme.colors.label.muted` | Третичный текст (серый) |
| `Theme.colors.stroke.primary` | Границы, разделители |
| `Theme.colors.background.primary` | Основной фон экрана |
| `Theme.colors.background.surface` | Поверхность карточек |

**Токены которые нужно добавить** (если их нет в `design_tokens/Component.kt`):

| Токен | Light | Dark | Где используется |
|---|---|---|---|
| `component.tag.background` | #F0F0EE | #1A1A1A | TagChip фон |
| `component.tag.foreground` | #7A7A7A | #606060 | TagChip текст |
| `component.button.background` | #F5F5F2 | #111111 | Round buttons фон |
