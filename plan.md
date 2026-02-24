# План редизайна экрана профиля

## Текущее состояние

`ProfileScreen.kt` — устаревший дизайн: фото на весь экран с градиентом, имя/email/gender поверх него, кнопка «Выйти».

Доменная модель `User(name, email, gender)`.

---

## Целевой дизайн (img.png)

| Зона | Описание |
|------|----------|
| Фон | Тёплый бежевый `#F5F4F2` на всём экране |
| Топ-бар | Заголовок «Профиль» слева (bold), иконка шестерёнки в круглой кнопке справа |
| Аватар | Круглое фото (~88 dp), маленький тёмный индикатор редактирования (dot) снизу-справа |
| Имя | Жирное имя «Sophia Laurent» под аватаром |
| Хэндл | «@sophia.style» серым цветом |
| Кнопка | Outlined-кнопка «Редактировать профиль» по центру |
| Статистика | Белая карточка с тремя колонками: **Образов / Вещей / Поделились** |
| Раздел | Надпись «НАСТРОЙКИ» заглавными буквами (мелкий серый) |
| Список настроек | Белая карточка со строками: иконка + заголовок + subtitle + шеврон (`>`) |

Строки настроек (статичные для MVP):
- ☆ Мой стиль · `Casual · Minimal`
- 🏷 Размеры · `EU 42 · S`
- 🔔 Уведомления

---

## Что нужно изменить/создать

### 1. Доменная модель `User`
Добавить поля:
- `username: String` — хэндл (@sophia.style), пока берём из email (часть до `@`)
- `outfitsCount: Int` — кол-во образов (временно `0` или из API)
- `clothesCount: Int` — кол-во вещей (временно `0`)
- `sharedCount: Int` — кол-во поделившихся (временно `0`)

> Если API не возвращает эти поля — добавляем как `Int = 0` с заглушкой, не трогая слой данных.

### 2. `ProfileScreen.kt` — полная замена вёрстки

Структура экрана (сверху вниз):

```
Scaffold (backgroundColor = бежевый)
└── Column (вертикальный скролл)
    ├── ProfileTopBar           // Заголовок + иконка настроек
    ├── ProfileAvatarSection    // Аватар + имя + хэндл + кнопка
    ├── ProfileStatsCard        // Три стата в карточке
    ├── Spacer(16.dp)
    ├── Text("НАСТРОЙКИ")       // Лейбл секции
    └── ProfileSettingsCard     // Список строк настроек
```

#### Компоненты (private composable внутри файла):

- **`ProfileTopBar`** — `Row` с `Text("Профиль")` + `IconButton` с иконкой `Icons.Default.Settings` в `Box` с круглым фоном
- **`ProfileAvatarSection`** — `Column(horizontalAlignment = Center)`:
  - `Box` → `AsyncImage` (Coil, круглый clip) + маленький `Box`-dot внизу-справа
  - `Text` (имя, bold, 20sp)
  - `Text` (@хэндл, серый, 14sp)
  - `OutlinedButton("Редактировать профиль")`
- **`ProfileStatsCard`** — `Card(shape = RoundedCorner(16.dp))` → `Row` из трёх `ProfileStatItem(count, label)`
- **`ProfileStatItem`** — `Column(Center)`: число (bold 20sp) + метка (серый 12sp)
- **`ProfileSettingsCard`** — `Card` → `Column` из `ProfileSettingsRow` + `Divider`
- **`ProfileSettingsRow`** — `Row`: иконка + `Column(title, subtitle)` + `Spacer` + `Icon(ChevronRight)`

### 3. Цветовая палитра (локальные val внутри файла)

```kotlin
private val BackgroundColor = Color(0xFFF5F4F2)
private val CardColor = Color.White
private val TextPrimary = Color(0xFF1A1A1A)
private val TextSecondary = Color(0xFF9E9E9E)
private val DividerColor = Color(0xFFF0F0F0)
```

### 4. Убрать кнопку «Выйти» с основного экрана
Logout переносится в экран настроек (за иконкой шестерёнки) — **в рамках этого редизайна не реализуем**, просто убираем с главного экрана. Функциональность `onLogout` оставляем как параметр `ProfileScreen`, но не отображаем кнопку.

---

## Файлы, которые будут изменены

| Файл | Изменение |
|------|-----------|
| `features/profile/.../domain/User.kt` | Добавить поля `username`, `outfitsCount`, `clothesCount`, `sharedCount` |
| `features/profile/.../presentation/ProfileScreen.kt` | Полная замена вёрстки по дизайну |

## Файлы, которые НЕ меняем

- `ProfileViewModel.kt` — логика не меняется
- `ProfileRepositoryImpl.kt` / `ProfileResponse.kt` — data-слой без изменений
- `di.kt` — DI без изменений
- Навигация — без изменений

---

## Порядок реализации

1. Обновить `User.kt` — добавить новые поля со значениями по умолчанию
2. Переписать `ProfileScreen.kt` — новый UI по дизайну
3. Проверить, что компилируется без ошибок

---

## Принятые решения

| Вопрос | Решение |
|--------|---------|
| Статистика (образов/вещей) | Заглушки: `outfitsCount=0`, `clothesCount=0`, `sharedCount=0` в `User.kt` |
| Экран настроек | Реализован: `ProfileSettingsScreen` с переключателем уведомлений и кнопкой «Выйти» |
| Аватар | `AsyncImage` из Coil 3, `avatarUrl=""` → показывает локальный `img_profile_default.jpeg` как fallback |

## Статус реализации: ВЫПОЛНЕНО ✓

Изменённые файлы:
- `features/profile/.../domain/User.kt` — добавлены поля username, avatarUrl, outfitsCount, clothesCount, sharedCount
- `features/profile/.../data/ProfileResponse.kt` — обновлён `toUser()`, username берётся из email
- `features/profile/.../presentation/ProfileNavScreens.kt` — **новый** sealed class с маршрутами Profile / Settings
- `features/profile/.../presentation/ProfileScreen.kt` — полностью переписан по дизайну
- `features/profile/.../presentation/ProfileSettingsScreen.kt` — **новый** экран настроек
- `features/card-feature/.../screens/ProfileScreen.kt` — добавлен параметр `onOpenSettings`
- `features/card-feature/.../navigation/BottomNavigationNavHost.kt` — подключён `ProfileSettingsScreen`
