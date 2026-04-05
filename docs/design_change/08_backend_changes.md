# Изменения Backend

Базовый URL: `http://194.87.190.248:80/api/v1/`

## Сводка изменений

| # | Endpoint | Метод | Статус | Описание |
|---|---|---|---|---|
| 1 | `/clothes` | GET | Изменить ответ | Добавить новые поля в модель |
| 2 | `/clothes/{id}` | PATCH | Новый | Обновление вещи |
| 3 | `/clothes/{id}` | GET | Новый | Получить детали вещи |
| 4 | `/clothes/{id}/looks` | GET | Новый | Образы в которых используется вещь |
| 5 | `/looks` | GET | Изменить ответ | Добавить поля `style`, `tags` |
| 6 | `/user/profile` | GET | Уточнить | Профиль пользователя |
| 7 | `/user/profile` | PATCH | Новый | Обновление профиля |
| 8 | `/user/sizes` | GET | Новый | Параметры тела и размеры |
| 9 | `/user/sizes` | PATCH | Новый | Обновление параметров тела |

---

## 1. Изменения модели `Clothe`

### Текущая модель (ответ GET /clothes)

```json
{
  "id": 1,
  "name": "Белая рубашка",
  "storeUrl": "https://...",
  "imageUrl": "https://..."
}
```

### Новая модель

```json
{
  "id": 1,
  "name": "Белая рубашка",
  "imageUrl": "https://...",
  "category": 1,
  "material": 1,
  "fit": 2,
  "styles": [1, 6],
  "season": [1, 3],
  "color": 1,
  "brand": null,
  "size": 3,
  "marketplaceLinks": ["https://wildberries.ru/...", "https://ozon.ru/..."],
  "tags": ["softcore look", "minimalist style"],
  "createdAt": "2026-03-12T10:00:00Z"
}
```

> Все enum-поля — целые числа. Расшифровку кодов см. в разделе [Перечисления](#перечисления-enumerations).

**Обратная совместимость**: поле `storeUrl` заменяется на `marketplaceLinks: List<String>`.  
Вариант: сделать `marketplaceLinks` новым полем, а `storeUrl` оставить deprecated (бэкенд сам разберётся).

Поля `category`, `material`, `fit`, `styles`, `season`, `color`, `brand`, `size`, `tags` — все опциональные (nullable), заполняются AI при загрузке вещи.

---

## 2. PATCH /clothes/{id} — обновление вещи

**Новый endpoint.**

### Запрос

```
PATCH /clothes/{id}
Authorization: Bearer {token}
Content-Type: application/json
```

```json
{
  "name": "Белая рубашка",
  "category": 1,
  "material": 1,
  "fit": 2,
  "styles": [1, 6],
  "season": [1, 3],
  "color": 1,
  "brand": "Zara",
  "size": 3,
  "marketplaceLinks": ["https://wildberries.ru/catalog/..."]
}
```

Все поля опциональны — обновляются только те что переданы (partial update).

### Ответ 200

```json
{ /* обновлённый Clothe */ }
```

---

## 3. GET /clothes/{id} — деталка вещи

**Новый endpoint.**

### Запрос

```
GET /clothes/{id}
Authorization: Bearer {token}
```

### Ответ 200

Полная модель `Clothe` (см. п.1).

---

## 4. GET /clothes/{id}/looks — образы с вещью

**Новый endpoint.**

### Запрос

```
GET /clothes/{id}/looks
Authorization: Bearer {token}
```

### Ответ 200

```json
[
  { "id": 1, "name": "Осенний образ", "url": "https://...", "tags": ["Пальто", "Брюки"] },
  ...
]
```

---

## 5. Изменения модели `Look`

### Текущая модель

```json
{
  "id": 1,
  "name": "Осенний образ",
  "url": "https://...",
  "lookItems": [...]
}
```

### Новая модель

```json
{
  "id": 1,
  "name": "Осенний образ",
  "url": "https://...",
  "lookItems": [...],
  "style": 1,
  "tags": ["Пальто", "Брюки", "Кеды"]
}
```

`style` — категория стиля для фильтрации.  
`tags` — теги для отображения на карточке и в деталке.

---

## 6. GET /profile — профиль пользователя

Endpoint существует. Используется в `ProfileRepositoryImpl` (endpoint: `"profile"`).

### Текущий ответ (что реально возвращает сервер сейчас)

```json
{
  "name": "Sophia Laurent",
  "email": "sophia@example.com",
  "gender": "FEMALE"
}
```

> `gender` приходит строкой — **нужно перевести на числовой формат** (см. enum `Gender`).

### Требуемый ответ (после доработки бэкенда)

```json
{
  "name": "Sophia Laurent",
  "username": "sophia.style",
  "email": "sophia@example.com",
  "avatarUrl": "https://...",
  "gender": 2,
  "outfitsCount": 12,
  "clothesCount": 47,
  "sharedCount": 3
}
```

### Что нужно добавить на бэкенде

| Поле | Тип | Статус | Комментарий |
|------|-----|--------|-------------|
| `name` | `String` | Есть | — |
| `email` | `String` | Есть | — |
| `gender` | `Int` | Изменить | Сейчас строка `"FEMALE"`, нужно число `2` |
| `username` | `String?` | **Добавить** | Никнейм пользователя |
| `avatarUrl` | `String?` | **Добавить** | URL аватара; `null` → показывать плейсхолдер |
| `outfitsCount` | `Int` | **Добавить** | Кол-во образов пользователя |
| `clothesCount` | `Int` | **Добавить** | Кол-во вещей в гардеробе |
| `sharedCount` | `Int` | **Добавить** | Кол-во поделившихся образов |

### Изменения в Kotlin-модели

**До (текущий код `ProfileResponse.kt`):**
```kotlin
@Serializable
data class ProfileResponse(
    val name: String,
    val email: String,
    val gender: String,           // "MALE" / "FEMALE" / "OTHER"
)
```

**После:**
```kotlin
// data/ProfileResponse.kt — DTO
@Serializable
data class ProfileResponse(
    val name: String,
    val email: String,
    val gender: Int,
    val username: String? = null,
    val avatarUrl: String? = null,
    val outfitsCount: Int = 0,
    val clothesCount: Int = 0,
    val sharedCount: Int = 0,
)

fun ProfileResponse.toUser(): User = User(
    name = name,
    email = email,
    gender = Gender.fromCode(gender),
    username = username,
    avatarUrl = avatarUrl,
    outfitsCount = outfitsCount,
    clothesCount = clothesCount,
    sharedCount = sharedCount,
)

// domain/User.kt — доменная модель
data class User(
    val name: String,
    val email: String,
    val gender: Gender,
    val username: String? = null,
    val avatarUrl: String? = null,
    val outfitsCount: Int = 0,
    val clothesCount: Int = 0,
    val sharedCount: Int = 0,
)
```

---

## 7. PATCH /profile — обновление профиля

**Новый endpoint.**

### Запрос

```
PATCH /profile
Authorization: Bearer {token}
Content-Type: application/json
```

```json
{
  "name": "Sophia Laurent",
  "username": "sophia.style",
  "gender": 2
}
```

> `gender`: `0` — Unisex, `1` — Male, `2` — Female

Аватар — через отдельный multipart endpoint (если понадобится).

### Ответ 200

Полная модель `User` (см. п.6 — Требуемый ответ).

---

## 8. GET /user/sizes — параметры тела

**Новый endpoint.**

### Ответ 200

```json
{
  "primarySize": 2,
  "height": 168,
  "weight": 58,
  "chest": 88,
  "waist": 68,
  "hips": 94
}
```

> `primarySize` — код из `ClothesSize`. Антропометрия — целые числа (см, кг).

---

## 9. PATCH /user/sizes — обновление параметров тела

**Новый endpoint.**

### Запрос

```
PATCH /user/sizes
Authorization: Bearer {token}
Content-Type: application/json
```

```json
{
  "primarySize": 2,
  "height": 168,
  "weight": 58,
  "chest": 88,
  "waist": 68,
  "hips": 94
}
```

### Ответ 200

```json
{
  "primarySize": 2,
  "height": 168,
  "weight": 58,
  "chest": 88,
  "waist": 68,
  "hips": 94
}
```

---

## Что заполняет AI автоматически

При загрузке вещи через `POST /clothes` (фото или URL), бэкенд уже умеет генерировать образы.  
Желательно также автоматически заполнять поля вещи:
- `category` — обязательно (нужно для фильтрации)
- `material`, `fit`, `styles`, `season`, `color`, `tags` — опционально, AI-генерация

Это снижает необходимость ручного редактирования пользователем.

---

## Перечисления (Enumerations)

Все перечисляемые поля передаются по API как **целые числа** (`Int`).  
На стороне клиента маппер преобразует `Int` → `EnumClass` и обратно.  
Неизвестное значение (пришло с сервера, но нет в enum) → `UNKNOWN` (код `0`).

---

### Color — цвет

| Код | Константа | Отображение |
|-----|-----------|-------------|
| 0 | `UNKNOWN` | — |
| 1 | `WHITE` | Белый |
| 2 | `BLACK` | Чёрный |
| 3 | `GRAY` | Серый |
| 4 | `BEIGE` | Бежевый |
| 5 | `BROWN` | Коричневый |
| 6 | `RED` | Красный |
| 7 | `PINK` | Розовый |
| 8 | `ORANGE` | Оранжевый |
| 9 | `YELLOW` | Жёлтый |
| 10 | `GREEN` | Зелёный |
| 11 | `OLIVE` | Оливковый |
| 12 | `BLUE` | Голубой |
| 13 | `NAVY` | Тёмно-синий |
| 14 | `PURPLE` | Фиолетовый |
| 15 | `BORDEAUX` | Бордовый |
| 16 | `KHAKI` | Хаки |
| 17 | `TURQUOISE` | Бирюзовый |
| 18 | `CORAL` | Коралловый |
| 19 | `GOLD` | Золотой |
| 20 | `SILVER` | Серебристый |
| 21 | `DENIM` | Джинсовый |
| 22 | `MULTICOLOR` | Разноцветный |

```kotlin
enum class ClothesColor(val code: Int) {
    UNKNOWN(0),
    WHITE(1), BLACK(2), GRAY(3), BEIGE(4), BROWN(5),
    RED(6), PINK(7), ORANGE(8), YELLOW(9),
    GREEN(10), OLIVE(11),
    BLUE(12), NAVY(13),
    PURPLE(14), BORDEAUX(15),
    KHAKI(16), TURQUOISE(17), CORAL(18),
    GOLD(19), SILVER(20),
    DENIM(21), MULTICOLOR(22);

    companion object {
        fun fromCode(code: Int?): ClothesColor =
            entries.find { it.code == code } ?: UNKNOWN
    }
}
```

---

### Style — стиль одежды

| Код | Константа | Отображение |
|-----|-----------|-------------|
| 0 | `UNKNOWN` | — |
| 1 | `CASUAL` | Casual |
| 2 | `FORMAL` | Формальный |
| 3 | `BUSINESS` | Деловой |
| 4 | `SPORTY` | Спортивный |
| 5 | `STREETWEAR` | Стритвир |
| 6 | `MINIMAL` | Минимализм |
| 7 | `ROMANTIC` | Романтический |
| 8 | `BOHO` | Бохо |
| 9 | `PREPPY` | Препи |
| 10 | `VINTAGE` | Винтаж |
| 11 | `GRUNGE` | Гранж |
| 12 | `ELEGANT` | Элегантный |
| 13 | `ATHLEISURE` | Атлежер |
| 14 | `CLASSIC` | Классика |
| 15 | `SMART_CASUAL` | Smart Casual |
| 16 | `AVANT_GARDE` | Авангард |

```kotlin
enum class ClothesStyle(val code: Int) {
    UNKNOWN(0),
    CASUAL(1), FORMAL(2), BUSINESS(3), SPORTY(4),
    STREETWEAR(5), MINIMAL(6), ROMANTIC(7), BOHO(8),
    PREPPY(9), VINTAGE(10), GRUNGE(11), ELEGANT(12),
    ATHLEISURE(13), CLASSIC(14), SMART_CASUAL(15), AVANT_GARDE(16);

    companion object {
        fun fromCode(code: Int?): ClothesStyle =
            entries.find { it.code == code } ?: UNKNOWN
    }
}
```

---

### Fit — посадка

| Код | Константа | Отображение |
|-----|-----------|-------------|
| 0 | `UNKNOWN` | — |
| 1 | `SLIM` | Slim |
| 2 | `REGULAR` | Regular |
| 3 | `LOOSE` | Loose |
| 4 | `OVERSIZED` | Oversized |
| 5 | `FITTED` | Fitted |
| 6 | `RELAXED` | Relaxed |
| 7 | `CROPPED` | Cropped |
| 8 | `STRAIGHT` | Straight |
| 9 | `TAPERED` | Tapered |
| 10 | `WIDE_LEG` | Wide Leg |
| 11 | `SKINNY` | Skinny |

```kotlin
enum class ClothesFit(val code: Int) {
    UNKNOWN(0),
    SLIM(1), REGULAR(2), LOOSE(3), OVERSIZED(4),
    FITTED(5), RELAXED(6), CROPPED(7), STRAIGHT(8),
    TAPERED(9), WIDE_LEG(10), SKINNY(11);

    companion object {
        fun fromCode(code: Int?): ClothesFit =
            entries.find { it.code == code } ?: UNKNOWN
    }
}
```

---

### Material — материал

| Код | Константа | Отображение |
|-----|-----------|-------------|
| 0 | `UNKNOWN` | — |
| 1 | `COTTON` | Хлопок |
| 2 | `POLYESTER` | Полиэстер |
| 3 | `WOOL` | Шерсть |
| 4 | `SILK` | Шёлк |
| 5 | `LINEN` | Лён |
| 6 | `DENIM` | Деним |
| 7 | `LEATHER` | Кожа |
| 8 | `SUEDE` | Замша |
| 9 | `NYLON` | Нейлон |
| 10 | `CASHMERE` | Кашемир |
| 11 | `VELVET` | Бархат |
| 12 | `CHIFFON` | Шифон |
| 13 | `JERSEY` | Джерси |
| 14 | `SPANDEX` | Спандекс/Эластан |
| 15 | `FLEECE` | Флис |
| 16 | `VISCOSE` | Вискоза |
| 17 | `ACRYLIC` | Акрил |
| 18 | `TWEED` | Твид |
| 19 | `FAUX_LEATHER` | Экокожа |
| 20 | `MIXED` | Смешанный |

```kotlin
enum class ClothesMaterial(val code: Int) {
    UNKNOWN(0),
    COTTON(1), POLYESTER(2), WOOL(3), SILK(4), LINEN(5),
    DENIM(6), LEATHER(7), SUEDE(8), NYLON(9), CASHMERE(10),
    VELVET(11), CHIFFON(12), JERSEY(13), SPANDEX(14), FLEECE(15),
    VISCOSE(16), ACRYLIC(17), TWEED(18), FAUX_LEATHER(19), MIXED(20);

    companion object {
        fun fromCode(code: Int?): ClothesMaterial =
            entries.find { it.code == code } ?: UNKNOWN
    }
}
```

---

### Category — категория одежды

| Код | Константа | Отображение |
|-----|-----------|-------------|
| 0 | `UNKNOWN` | — |
| 1 | `TOP` | Верх |
| 2 | `BOTTOM` | Низ |
| 3 | `OUTERWEAR` | Верхняя одежда |
| 4 | `DRESS` | Платье / Комбинезон |
| 5 | `SHOES` | Обувь |
| 6 | `BAG` | Сумка |
| 7 | `ACCESSORIES` | Аксессуары |
| 8 | `UNDERWEAR` | Бельё |
| 9 | `SWIMWEAR` | Купальник |
| 10 | `SPORTSWEAR` | Спортивная одежда |
| 11 | `SUIT` | Костюм |
| 12 | `KNITWEAR` | Трикотаж |
| 13 | `JEANS` | Джинсы |
| 14 | `SKIRT` | Юбка |
| 15 | `JACKET` | Пиджак / Жакет |

```kotlin
enum class ClothesCategory(val code: Int) {
    UNKNOWN(0),
    TOP(1), BOTTOM(2), OUTERWEAR(3), DRESS(4), SHOES(5),
    BAG(6), ACCESSORIES(7), UNDERWEAR(8), SWIMWEAR(9),
    SPORTSWEAR(10), SUIT(11), KNITWEAR(12), JEANS(13),
    SKIRT(14), JACKET(15);

    companion object {
        fun fromCode(code: Int?): ClothesCategory =
            entries.find { it.code == code } ?: UNKNOWN
    }
}
```

---

### Size — размер

Размеры передаются как целые числа без плавающей точки.  
Буквенные размеры (XS–XXXL) — коды 1–7. Числовые EU-размеры — равны самому числу.

| Код | Константа | Отображение |
|-----|-----------|-------------|
| 0 | `UNKNOWN` | — |
| 1 | `XS` | XS |
| 2 | `S` | S |
| 3 | `M` | M |
| 4 | `L` | L |
| 5 | `XL` | XL |
| 6 | `XXL` | XXL |
| 7 | `XXXL` | XXXL |
| 8 | `ONE_SIZE` | One Size |
| 32 | `EU_32` | 32 |
| 34 | `EU_34` | 34 |
| 36 | `EU_36` | 36 |
| 38 | `EU_38` | 38 |
| 40 | `EU_40` | 40 |
| 42 | `EU_42` | 42 |
| 44 | `EU_44` | 44 |
| 46 | `EU_46` | 46 |
| 48 | `EU_48` | 48 |
| 50 | `EU_50` | 50 |
| 52 | `EU_52` | 52 |
| 54 | `EU_54` | 54 |

```kotlin
enum class ClothesSize(val code: Int) {
    UNKNOWN(0),
    XS(1), S(2), M(3), L(4), XL(5), XXL(6), XXXL(7), ONE_SIZE(8),
    EU_32(32), EU_34(34), EU_36(36), EU_38(38), EU_40(40), EU_42(42),
    EU_44(44), EU_46(46), EU_48(48), EU_50(50), EU_52(52), EU_54(54);

    companion object {
        fun fromCode(code: Int?): ClothesSize =
            entries.find { it.code == code } ?: UNKNOWN
    }
}
```

---

### Season — сезон

| Код | Константа | Отображение |
|-----|-----------|-------------|
| 0 | `UNKNOWN` | — |
| 1 | `SPRING` | Весна |
| 2 | `SUMMER` | Лето |
| 3 | `AUTUMN` | Осень |
| 4 | `WINTER` | Зима |
| 5 | `DEMI_SEASON` | Межсезонье |
| 6 | `ALL_SEASON` | Всесезонный |

```kotlin
enum class ClothesSeason(val code: Int) {
    UNKNOWN(0),
    SPRING(1), SUMMER(2), AUTUMN(3), WINTER(4),
    DEMI_SEASON(5), ALL_SEASON(6);

    companion object {
        fun fromCode(code: Int?): ClothesSeason =
            entries.find { it.code == code } ?: UNKNOWN
    }
}
```

---

### Gender — пол

| Код | Константа | Отображение |
|-----|-----------|-------------|
| 0 | `UNISEX` | Унисекс |
| 1 | `MALE` | Мужской |
| 2 | `FEMALE` | Женский |

```kotlin
enum class Gender(val code: Int) {
    UNISEX(0), MALE(1), FEMALE(2);

    companion object {
        fun fromCode(code: Int?): Gender =
            entries.find { it.code == code } ?: UNISEX
    }
}
```

---

## Маппер DTO → Domain

Все enum-поля в DTO принимают `Int?`, mapper конвертирует в доменный тип:

```kotlin
// features/wardrobe/internal/data/mapper/ClotheMapper.kt
fun ClotheDto.toDomain(): Clothe = Clothe(
    id = id,
    name = name,
    imageUrl = imageUrl,
    category = ClothesCategory.fromCode(category),
    material = ClothesMaterial.fromCode(material),
    fit = ClothesFit.fromCode(fit),
    styles = styles?.map { ClothesStyle.fromCode(it) } ?: emptyList(),
    season = season?.map { ClothesSeason.fromCode(it) } ?: emptyList(),
    color = ClothesColor.fromCode(color),
    brand = brand,
    size = ClothesSize.fromCode(size),
    marketplaceLinks = marketplaceLinks ?: emptyList(),
    tags = tags ?: emptyList(),
    createdAt = createdAt,
)

fun Clothe.toDto(): ClotheDto = ClotheDto(
    id = id,
    name = name,
    imageUrl = imageUrl,
    category = category.code,
    material = material.code,
    fit = fit.code,
    styles = styles.map { it.code },
    season = season.map { it.code },
    color = color.code,
    brand = brand,
    size = size.code,
    marketplaceLinks = marketplaceLinks,
    tags = tags,
    createdAt = createdAt,
)
```

---

## Изменения в Kotlin-моделях проекта

### `ClotheDto.kt` — DTO (сетевой слой)

Принимает числа напрямую из JSON:

```kotlin
@Serializable
data class ClotheDto(
    val id: Int?,
    val name: String,
    val imageUrl: String,
    val category: Int? = null,
    val material: Int? = null,
    val fit: Int? = null,
    val styles: List<Int>? = null,
    val season: List<Int>? = null,
    val color: Int? = null,
    val brand: String? = null,
    val size: Int? = null,
    val marketplaceLinks: List<String>? = null,
    val tags: List<String>? = null,
    val createdAt: String? = null,
    // deprecated
    val storeUrl: String? = null,
)
```

### `Clothe.kt` — доменная модель

Использует enum-типы:

```kotlin
data class Clothe(
    val id: Int?,
    val name: String,
    val imageUrl: String,
    val category: ClothesCategory = ClothesCategory.UNKNOWN,
    val material: ClothesMaterial = ClothesMaterial.UNKNOWN,
    val fit: ClothesFit = ClothesFit.UNKNOWN,
    val styles: List<ClothesStyle> = emptyList(),
    val season: List<ClothesSeason> = emptyList(),
    val color: ClothesColor = ClothesColor.UNKNOWN,
    val brand: String? = null,
    val size: ClothesSize = ClothesSize.UNKNOWN,
    val marketplaceLinks: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val createdAt: String? = null,
)
```

### `Look.kt` — расширить

```kotlin
@Serializable
data class LookDto(
    val id: Int? = null,
    val name: String,
    val url: String,
    val lookItems: List<LookItem>? = null,
    val style: Int? = null,
    val tags: List<String>? = null,
)

data class Look(
    val id: Int? = null,
    val name: String,
    val url: String,
    val lookItems: List<LookItem> = emptyList(),
    val style: ClothesStyle = ClothesStyle.UNKNOWN,
    val tags: List<String> = emptyList(),
)
```

### `UserSizes.kt` — новый файл

В `features/profile/src/commonMain/.../data/model/UserSizes.kt`:

```kotlin
@Serializable
data class UserSizesDto(
    val primarySize: Int? = null,
    val height: Int? = null,
    val weight: Int? = null,
    val chest: Int? = null,
    val waist: Int? = null,
    val hips: Int? = null,
)

data class UserSizes(
    val primarySize: ClothesSize = ClothesSize.UNKNOWN,
    val height: Int? = null,
    val weight: Int? = null,
    val chest: Int? = null,
    val waist: Int? = null,
    val hips: Int? = null,
)
```

> `height`, `weight`, `chest`, `waist`, `hips` — в сантиметрах/килограммах, целые числа без плавающей точки.
