# Ресурсы (moko-resources)

## Обзор

Проект использует [moko-resources](https://github.com/icerockdev/moko-resources) для управления общими ресурсами (иконки, строки, шрифты) через модуль `core/resources`.

Сгенерированный класс `MR` доступен из любого модуля, подключившего `core:resources` как зависимость.

## Структура модуля

```
core/resources/src/commonMain/moko-resources/
├── images/          # SVG-иконки и растровые изображения
├── strings/         # Локализованные строки (strings.xml)
└── fonts/           # Шрифты
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

## Шрифты (fonts)

Положить `.ttf`/`.otf` файлы в `core/resources/src/commonMain/moko-resources/fonts/`.

Именование: `FontName_weight.ttf` (например `Roboto_regular.ttf`, `Roboto_bold.ttf`).

Использование:
```kotlin
import dev.icerock.moko.resources.compose.fontFamilyResource

Text(
    text = "Hello",
    fontFamily = fontFamilyResource(MR.fonts.Roboto.regular)
)
```

## Подключение модуля к фиче

В `build.gradle.kts` фичи:

```kotlin
commonMain.dependencies {
    implementation(libs.moko.resources)
    implementation(libs.moko.resources.compose)
    implementation(project(":core:resources"))
}
```

## Запрет использования Material Icons

В проекте **запрещено** использование `androidx.compose.material.icons` (`Icons.Default.*`, `Icons.Filled.*` и т.д.). Все иконки берутся из `MR.images.*` через moko-resources.
