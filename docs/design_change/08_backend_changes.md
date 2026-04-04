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
  "category": "Верх",
  "material": "Хлопок",
  "fit": "Regular",
  "styles": ["Casual", "Minimal"],
  "season": ["Весна", "Осень"],
  "color": "Белый",
  "brand": null,
  "size": "M",
  "marketplaceLinks": ["https://wildberries.ru/...", "https://ozon.ru/..."],
  "tags": ["softcore look", "minimalist style"],
  "createdAt": "2026-03-12T10:00:00Z"
}
```

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
  "category": "Верх",
  "material": "Хлопок",
  "fit": "Regular",
  "styles": ["Casual", "Minimal"],
  "season": ["Весна", "Осень"],
  "color": "Белый",
  "brand": "Zara",
  "size": "M",
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
  "style": "Casual",
  "tags": ["Пальто", "Брюки", "Кеды"]
}
```

`style` — категория стиля для фильтрации.  
`tags` — теги для отображения на карточке и в деталке.

---

## 6. GET /profile — профиль пользователя

Уже существует. Используется в `ProfileRepositoryImpl`.

Текущая `User` модель (из `ProfileScreen`): `name`, `email`, `gender`, `username`, `avatarUrl`, `outfitsCount`, `clothesCount`, `sharedCount`.

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
  "gender": "FEMALE"
}
```

Аватар — через отдельный multipart endpoint (если понадобится).

### Ответ 200

```json
{ /* обновлённый User */ }
```

---

## 8. GET /user/sizes — параметры тела

**Новый endpoint.**

### Ответ 200

```json
{
  "primarySize": "S",
  "height": 168.0,
  "weight": 58.0,
  "chest": 88.0,
  "waist": 68.0,
  "hips": 94.0
}
```

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
  "primarySize": "S",
  "height": 168.0,
  "weight": 58.0,
  "chest": 88.0,
  "waist": 68.0,
  "hips": 94.0
}
```

### Ответ 200

```json
{ /* обновлённые UserSizes */ }
```

---

## Что заполняет AI автоматически

При загрузке вещи через `POST /clothes` (фото или URL), бэкенд уже умеет генерировать образы.  
Желательно также автоматически заполнять поля вещи:
- `category` — обязательно (нужно для фильтрации)
- `material`, `fit`, `styles`, `season`, `color`, `tags` — опционально, AI-генерация

Это снижает необходимость ручного редактирования пользователем.

---

## Изменения в Kotlin-моделях проекта

### `Clothe.kt` — расширить

```kotlin
@Serializable
data class Clothe(
    val id: Int?,
    val name: String,
    val imageUrl: String,
    val category: String? = null,
    val material: String? = null,
    val fit: String? = null,
    val styles: List<String>? = null,
    val season: List<String>? = null,
    val color: String? = null,
    val brand: String? = null,
    val size: String? = null,
    val marketplaceLinks: List<String>? = null,
    val tags: List<String>? = null,
    val createdAt: String? = null,
    // deprecated
    val storeUrl: String? = null,
)
```

### `Look.kt` — расширить

```kotlin
@Serializable
data class Look(
    val id: Int? = null,
    val name: String,
    val url: String,
    val lookItems: List<LookItem>? = null,
    val style: String? = null,         // новое
    val tags: List<String>? = null,    // новое
)
```

### `UserSizes.kt` — новый файл

В `features/profile/src/commonMain/.../data/model/UserSizes.kt`:

```kotlin
@Serializable
data class UserSizes(
    val primarySize: String? = null,
    val height: Float? = null,
    val weight: Float? = null,
    val chest: Float? = null,
    val waist: Float? = null,
    val hips: Float? = null,
)
```
