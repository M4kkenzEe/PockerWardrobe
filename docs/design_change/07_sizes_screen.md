# Экран Размеры (SizesScreen)

## Модуль

`features/profile` — входит в тот же модуль что и Профиль.

## Файл (новый)

```
features/profile/src/commonMain/.../
└── internal/presentation/
    └── detail/
        └── sizes/              # подпакет внутри detail — вместе с editProfile/
            ├── interactionModel/
            │   ├── SizesState.kt
            │   ├── SizesIntent.kt
            │   └── SizesSideEffect.kt
            ├── SizesStore.kt
            ├── SizesViewModel.kt
            └── SizesScreen.kt
```

## Описание экрана

Открывается из Профиль → строка "Размеры".  
Показывает параметры тела пользователя и размеры одежды в разных региональных системах.

> **keynotes**: "Азиатские размеры — добавить в профиль, убрать таблицу размеров"  
> Добавить JP/KR/CN в переключатель регионов. Статичную таблицу размеров **убрать**.

### Структура экрана

```
← (назад)    [Размеры]
─────────────────────────────────────────────────

── ВАШ РАЗМЕР ───────────────────────────────────
         [S]  (цветной круг с буквой)

── ПАРАМЕТРЫ ТЕЛА ───────────────────────────────
Рост              168 см  >
Вес               58 кг   >
Обхват груди      88 см   >
Обхват талии      68 см   >
Обхват бёдер      94 см   >

── РАЗМЕРЫ ОДЕЖДЫ ───────────────────────────────
[🇷🇺 RU] [🇪🇺 EU] [🇺🇸 US] [🇯🇵 JP] [🇰🇷 KR] [🇨🇳 CN]

Верх    44       >
Низ     44       >
Обувь   37.5     >

Размеры используются для подбора одежды и рекомендаций от AI
─────────────────────────────────────────────────
```

### "Ваш размер"

Основной размер отображается цветным кружком. Цвета через `Theme.colors.*`:

| Размер | Цвет |
|---|---|
| XS | `#A78BFA` |
| S  | `#34D399` |
| M  | `#60A5FA` |
| L  | `#FBBF24` |
| XL | `#F87171` |

### Параметры тела

Каждая строка — тап открывает bottom sheet для ввода значения.

### Таблица конвертации размеров (на клиенте)

Хранится локально как константа, API не требует.

```kotlin
// internal/domain/model/SizeConversion.kt
data class SizeRow(
    val label: String,
    val ru: String, val eu: String, val us: String,
    val jp: String, val kr: String, val cn: String,
)

val CLOTHING_SIZES = listOf(
    SizeRow("Верх",  eu="S",  ru="44",   us="XS/S", jp="S",  kr="85",  cn="165/88A"),
    SizeRow("Низ",   eu="42", ru="44",   us="8",    jp="M",  kr="28",  cn="170/74A"),
    SizeRow("Обувь", eu="38", ru="37.5", us="7.5",  jp="24", kr="240", cn="240"),
)
```

---

## FlowMVI

### SizesState

```kotlin
data class SizesState(
    val sizes: UserSizes? = null,
    val activeRegion: SizeRegion = SizeRegion.RU,
    val isLoading: Boolean = true,
    val editingField: SizeField? = null,
) : MVIState

enum class SizeRegion { RU, EU, US, JP, KR, CN }

enum class SizeField { HEIGHT, WEIGHT, CHEST, WAIST, HIPS }
```

### SizesIntent

```kotlin
sealed interface SizesIntent : MVIIntent {
    data object Load : SizesIntent
    data class SelectRegion(val region: SizeRegion) : SizesIntent
    data class EditField(val field: SizeField) : SizesIntent
    data class SaveField(val field: SizeField, val value: Float) : SizesIntent
    data object DismissEdit : SizesIntent
}
```

### SizesSideEffect

```kotlin
sealed class SizesSideEffect {
    data class ShowError(val message: String) : SizesSideEffect()
}
```

---

## Data Layer

### DTO

```kotlin
// internal/data/dto/UserSizesDto.kt
@Serializable
data class UserSizesDto(
    @SerialName("primary_size") val primarySize: String? = null,
    @SerialName("height") val height: Float? = null,
    @SerialName("weight") val weight: Float? = null,
    @SerialName("chest") val chest: Float? = null,
    @SerialName("waist") val waist: Float? = null,
    @SerialName("hips") val hips: Float? = null,
)
```

### Mapper

```kotlin
// internal/data/mapper/UserSizesMapper.kt
fun UserSizesDto.toUserSizes(): UserSizes = UserSizes(
    primarySize = primarySize,
    height = height,
    weight = weight,
    chest = chest,
    waist = waist,
    hips = hips,
)

fun UserSizes.toDto(): UserSizesDto = UserSizesDto(
    primarySize = primarySize,
    height = height,
    weight = weight,
    chest = chest,
    waist = waist,
    hips = hips,
)
```

### RepositoryImpl

Реализация находится в `ProfileRepositoryImpl` (см. `06_profile_screen.md`):

```kotlin
override suspend fun getUserSizes(): UserSizes =
    api.getUserSizes().toUserSizes()

override suspend fun updateUserSizes(sizes: UserSizes): UserSizes =
    api.updateUserSizes(sizes.toDto()).toUserSizes()
```

### Use Cases

```kotlin
// internal/domain/usecase/GetUserSizesUseCase.kt
class GetUserSizesUseCase(private val repository: ProfileRepository) {
    suspend operator fun invoke(): Result<UserSizes> =
        runCatching { repository.getUserSizes() }
}

// internal/domain/usecase/UpdateUserSizesUseCase.kt
class UpdateUserSizesUseCase(private val repository: ProfileRepository) {
    suspend operator fun invoke(sizes: UserSizes): Result<UserSizes> =
        runCatching { repository.updateUserSizes(sizes) }
}
```

---

## Доменная модель

```kotlin
// internal/domain/model/UserSizes.kt
data class UserSizes(
    val primarySize: String? = null,   // "S", "M", "L"
    val height: Float? = null,
    val weight: Float? = null,
    val chest: Float? = null,
    val waist: Float? = null,
    val hips: Float? = null,
)
```

## Backend

- `GET /user/sizes` → `UserSizes`
- `PATCH /user/sizes` → обновление параметров

Подробнее в `08_backend_changes.md`.

## Навигационный маршрут

Описан в `06_profile_screen.md`:

```kotlin
@Serializable object Sizes
```
