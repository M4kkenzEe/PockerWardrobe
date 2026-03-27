# Ресурсы и Theme

## Обзор

Проект использует [moko-resources](https://github.com/icerockdev/moko-resources) для управления общими ресурсами (иконки, строки, шрифты, цвета) через модуль `core/resources`.

Сгенерированный класс `MR` доступен из любого модуля, подключившего `core:resources` как зависимость.

Тема (цвета + типографика) реализована в модуле `core/compose` на основе дизайн-системы из `clothis_design`.

## Структура модулей

```
core/resources/src/commonMain/moko-resources/
├── images/          # SVG-иконки и растровые изображения
├── colors/          # Цвета (colors.xml) — light/dark токены
├── strings/         # Локализованные строки (strings.xml)
└── fonts/           # Шрифты (Inter)

core/compose/src/commonMain/kotlin/.../theme/
├── Theme.kt                    # AppTheme() + Theme object
├── Color.kt                    # ProjectColors, lightPalette, darkPalette
├── design_tokens/
│   ├── Background.kt           # Фоны экранов и карточек
│   ├── Label.kt                # Цвета текста
│   ├── Accent.kt               # Акцентные цвета (кнопки, выделения)
│   ├── Stroke.kt               # Обводки, разделители
│   └── Component.kt            # Чипсы, теги, навигация, FAB, скелетон
└── typography/
    ├── ProjectTypography.kt    # 10 уровней типографики
    └── TypographyDefaults.kt   # Inter font family + дефолтные стили
```

## Иконки (images)

### Форматы файлов

| Формат | Именование | Пример |
|--------|-----------|--------|
| SVG (вектор) | `name.svg` | `star.svg` |
| PNG/JPG (растр) | `name@1x.png`, `name@2x.png`, `name@3x.png` | `banner@1x.png` |

- SVG автоматически конвертируется в Android Vector Drawable и iOS PDF
- PNG/JPG маппится на Android `mdpi`/`xhdpi`/`xxhdpi` и iOS asset catalog 1x/2x/3x
- Имена файлов — только `snake_case`, без дефисов (Kotlin-совместимость)
- SVG не должны содержать `currentColor` — используй `#000000` (тонирование через `tint` в Compose)
- Нельзя иметь одновременно `star.svg` и `star@1x.png` — конфликт имён

### Добавление новой иконки

1. Положить SVG в `core/resources/src/commonMain/moko-resources/images/`
2. Убедиться, что в SVG нет `currentColor` (заменить на `#000000`)
3. Запустить `./gradlew :core:resources:generateMRcommonMain` для генерации MR-класса
4. Использовать в коде: `MR.images.icon_name`

### Использование в Compose

```kotlin
import com.ownstd.project.core.resources.MR
import dev.icerock.moko.resources.compose.painterResource

// Иконка с тонированием
Icon(
    painter = painterResource(MR.images.star),
    contentDescription = "Избранное",
    tint = Color.Gray,
    modifier = Modifier.size(24.dp)
)

// Изображение без тонирования
Image(
    painter = painterResource(MR.images.banner),
    contentDescription = "Баннер"
)
```

### Доступные иконки

| Иконка | MR-ключ | Описание |
|--------|---------|----------|
| arrow_left | `MR.images.arrow_left` | Стрелка назад |
| arrow_right | `MR.images.arrow_right` | Стрелка вперёд |
| bell | `MR.images.bell` | Уведомления |
| bookmark | `MR.images.bookmark` | Закладка |
| camera | `MR.images.camera` | Камера |
| check | `MR.images.check` | Галочка |
| chevron_down | `MR.images.chevron_down` | Шеврон вниз |
| chevron_left | `MR.images.chevron_left` | Шеврон влево |
| chevron_right | `MR.images.chevron_right` | Шеврон вправо |
| chevron_up | `MR.images.chevron_up` | Шеврон вверх |
| circle | `MR.images.circle` | Круг |
| crown | `MR.images.crown` | Корона |
| external_link | `MR.images.external_link` | Внешняя ссылка |
| grip_vertical | `MR.images.grip_vertical` | Захват (drag) |
| image | `MR.images.image` | Изображение |
| layers | `MR.images.layers` | Слои |
| link_2 | `MR.images.link_2` | Ссылка |
| loader_2 | `MR.images.loader_2` | Загрузка |
| lock | `MR.images.lock` | Замок |
| log_out | `MR.images.log_out` | Выход |
| minus | `MR.images.minus` | Минус |
| moon | `MR.images.moon` | Луна (тёмная тема) |
| more_horizontal | `MR.images.more_horizontal` | Ещё (горизонтальный) |
| panel_left | `MR.images.panel_left` | Панель слева |
| pencil | `MR.images.pencil` | Редактирование |
| plus | `MR.images.plus` | Плюс |
| rotate_ccw | `MR.images.rotate_ccw` | Поворот |
| ruler | `MR.images.ruler` | Линейка (размеры) |
| search | `MR.images.search` | Поиск |
| share_2 | `MR.images.share_2` | Поделиться |
| shopping_bag | `MR.images.shopping_bag` | Корзина |
| sliders_horizontal | `MR.images.sliders_horizontal` | Настройки/фильтры |
| sparkles | `MR.images.sparkles` | Рекомендации |
| star | `MR.images.star` | Звезда/избранное |
| sun | `MR.images.sun` | Солнце (светлая тема) |
| tag | `MR.images.tag` | Тег |
| trash_2 | `MR.images.trash_2` | Удаление |
| user | `MR.images.user` | Пользователь |
| wand_2 | `MR.images.wand_2` | Волшебная палочка |
| x | `MR.images.x` | Закрыть |
| zoom_in | `MR.images.zoom_in` | Приблизить |
| zoom_out | `MR.images.zoom_out` | Отдалить |

## Строки (strings)

Файл: `core/resources/src/commonMain/moko-resources/strings/strings.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">PockerWardrobe</string>
    <string name="greeting">Привет, %s!</string>
</resources>
```

Использование:
```kotlin
import dev.icerock.moko.resources.compose.stringResource

Text(text = stringResource(MR.strings.app_name))
Text(text = stringResource(MR.strings.greeting, userName))
```

## Цвета (colors)

Файл: `core/resources/src/commonMain/moko-resources/colors/colors.xml`

Все цвета хранятся как moko-resources с суффиксами `_light`/`_dark`. Используются через `colorResource(MR.colors.*)` в design-token классах.

**ЗАПРЕЩЕНО** хардкодить `Color(0xFF...)` в Composable-функциях. Все цвета берутся из `Theme.colors.*`.

## Шрифты (fonts)

Файл: `core/resources/src/commonMain/moko-resources/fonts/`

Именование: `fontname_weight.ttf` (например `inter_regular.ttf`, `inter_bold.ttf`).

Доступные шрифты Inter:
- `inter_thin.ttf` (W100)
- `inter_light.ttf` (W300)
- `inter_regular.ttf` (W400)
- `inter_medium.ttf` (W500)
- `inter_semibold.ttf` (W600)
- `inter_bold.ttf` (W700)

Font family создаётся через `interFontFamily()` в `TypographyDefaults.kt` и автоматически применяется ко всей типографике через `AppTheme`.

## Theme — дизайн-система

Две темы: **Light Minimalism** и **Dark Premium** (источник: `clothis_design/src/app/themes.ts`).

### Оборачивание приложения

```kotlin
AppTheme(darkTheme = false) {
    // весь контент приложения
}
```

### Использование цветов

```kotlin
import com.ownstd.project.core.compose.theme.Theme

// Фоны
Modifier.background(Theme.colors.background.primary)   // Фон экрана
Modifier.background(Theme.colors.background.surface)    // Фон карточки
Modifier.background(Theme.colors.background.surfaceAlt) // Вложенный блок

// Текст
Text(color = Theme.colors.label.primary)    // Заголовки, основной текст
Text(color = Theme.colors.label.secondary)  // Подзаголовки, описания
Text(color = Theme.colors.label.muted)      // Плейсхолдеры, подсказки
Text(color = Theme.colors.label.error)      // Ошибки валидации

// Акцент — кнопки, выделения
Button(colors = ButtonDefaults.buttonColors(
    backgroundColor = Theme.colors.accent.primary,    // Фон кнопки
    contentColor = Theme.colors.accent.onAccent,      // Текст на кнопке
))
Box(Modifier.background(Theme.colors.accent.subtle))  // Мягкий фон акцента

// Обводки
Divider(color = Theme.colors.stroke.primary)
BorderStroke(1.dp, Theme.colors.stroke.primary)

// Компоненты
// Чипсы
Modifier.background(Theme.colors.component.chip.background)       // Неактивный чипс
Modifier.background(Theme.colors.component.chip.activeBackground) // Активный чипс

// Навигация
Modifier.background(Theme.colors.component.nav.background)
Icon(tint = Theme.colors.component.nav.active)    // Активная вкладка
Icon(tint = Theme.colors.component.nav.inactive)  // Неактивная вкладка

// FAB
FloatingActionButton(backgroundColor = Theme.colors.component.fab.background)

// Скелетон
Modifier.background(Theme.colors.component.skeleton)
```

### Использование типографики

```kotlin
import com.ownstd.project.core.compose.theme.Theme

Text(text = "Clothis",        style = Theme.typography.h1)       // 22sp/Bold
Text(text = "Гардероб",       style = Theme.typography.h2)       // 20sp/Bold
Text(text = "Конструктор",    style = Theme.typography.h3)       // 17sp/Bold
Text(text = "Сохранить",      style = Theme.typography.subhead)  // 15sp/SemiBold
Text(text = "Описание",       style = Theme.typography.body)     // 14sp/Medium
Text(text = "Категория",      style = Theme.typography.label)    // 13sp/Medium
Text(text = "casual look",    style = Theme.typography.caption)  // 12sp/Regular
Text(text = "Coming soon",    style = Theme.typography.small)    // 11sp/Medium
Text(text = "ОДЕЖДА",         style = Theme.typography.micro)    // 10sp/Medium
Text(text = "Подпись",        style = Theme.typography.tiny)     // 9sp/SemiBold
```

Все стили используют шрифт **Inter** через moko-resources.

### Цветовая палитра (справочник)

| Токен | Light (#) | Dark (#) | Назначение |
|-------|-----------|----------|------------|
| `background.primary` | F4F4F4 | 080808 | Фон экрана |
| `background.surface` | F5F5F2 | 111111 | Карточки, диалоги |
| `background.surfaceAlt` | EEEEEB | 181818 | Вложенные блоки |
| `label.primary` | 1A1A1A | EFEFEF | Основной текст |
| `label.secondary` | 7A7A7A | 606060 | Вторичный текст |
| `label.muted` | B0B0B0 | 333333 | Приглушённый текст |
| `accent.primary` | 1A1A1A | EFEFEF | Кнопки, акцент |
| `accent.onAccent` | FFFFFF | 080808 | Текст на акценте |
| `accent.subtle` | F0F0EE | 1A1A1A | Мягкий фон акцента |
| `stroke.primary` | E8E8E5 | 1E1E1E | Разделители, бордеры |
| `component.chip.*` | F0F0EE/7A7A7A | 181818/606060 | Чипсы фильтров |
| `component.nav.*` | FFFFFF/1A1A1A | 080808/EFEFEF | Нижняя навигация |
| `component.fab.*` | 1A1A1A/FFFFFF | EFEFEF/080808 | FAB |
| `component.skeleton` | EFEFED | 1A1A1A | Скелетон-загрузка |
| `label.error` | D4183D | D4183D | Ошибки |

## Подключение модулей к фиче

В `build.gradle.kts` фичи:

```kotlin
commonMain.dependencies {
    // Ресурсы (иконки, строки, шрифты, цвета)
    implementation(libs.moko.resources)
    implementation(libs.moko.resources.compose)
    implementation(project(":core:resources"))

    // Тема (Theme.colors.*, Theme.typography.*)
    implementation(project(":core:compose"))
}
```

## Запреты

- **ЗАПРЕЩЕНО** использовать `androidx.compose.material.icons` (`Icons.Default.*`, `Icons.Filled.*`). Иконки — только из `MR.images.*`
- **ЗАПРЕЩЕНО** хардкодить цвета `Color(0xFF...)`. Цвета — только из `Theme.colors.*`
- **ЗАПРЕЩЕНО** хардкодить `fontSize`/`fontWeight`. Типографика — только из `Theme.typography.*`
