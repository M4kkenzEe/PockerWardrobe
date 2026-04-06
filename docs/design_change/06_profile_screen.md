# Экран Профиль

## Модуль

`features/profile` (существующий модуль, доработка)

Внутри профиля находятся: Профиль, Редактирование профиля, Экран Размеры, Настройки профиля.

## Файлы

```
features/profile/src/commonMain/.../
├── di/ProfileModule.kt
├── internal/
│   ├── data/
│   │   ├── dto/UserDto.kt, UserSizesDto.kt
│   │   ├── mapper/UserMapper.kt
│   │   └── repository/ProfileRepositoryImpl.kt
│   ├── domain/
│   │   ├── model/User.kt, UserSizes.kt
│   │   ├── repository/ProfileRepository.kt
│   │   └── usecase/GetProfileUseCase.kt, UpdateProfileUseCase.kt, ToggleThemeUseCase.kt
│   └── presentation/
│       ├── root/                        # Главный экран профиля (таб)
│       │   ├── interactionModel/
│       │   │   ├── ProfileState.kt
│       │   │   ├── ProfileIntent.kt
│       │   │   └── ProfileSideEffect.kt
│       │   ├── ProfileStore.kt
│       │   ├── ProfileViewModel.kt
│       │   └── ProfileScreen.kt
│       └── detail/                      # Экраны открываемые из профиля
│           ├── editProfile/
│           │   ├── interactionModel/
│           │   │   ├── EditProfileState.kt
│           │   │   ├── EditProfileIntent.kt
│           │   │   └── EditProfileSideEffect.kt
│           │   ├── EditProfileStore.kt
│           │   ├── EditProfileViewModel.kt
│           │   └── EditProfileScreen.kt
│           └── sizes/                   ← описан в 07_sizes_screen.md
│               ├── interactionModel/
│               │   ├── SizesState.kt
│               │   ├── SizesIntent.kt
│               │   └── SizesAction.kt
│               ├── SizesStore.kt
│               ├── SizesViewModel.kt
│               └── SizesScreen.kt
└── external/
    ├── ProfileRoutes.kt
    └── ProfileNavGraph.kt
```

---

## Экран Профиль (ProfileScreen)

### Структура экрана

```
← (назад)       [Профиль]
─────────────────────────────────────────────────
         [Аватар 88dp]
         [Имя пользователя]
         [@username]
         [Редактировать профиль]

── Pro Banner ───────────────────────────────────
[👑 Upgrade to Pro]
[Unlock full styling...]        [Coming soon...]

── Статистика ───────────────────────────────────
| 42 Образов | 167 Вещей | 28 Поделились |

── Настройки ────────────────────────────────────
☆ Мой стиль     Casual · Minimal      >
📐 Размеры      EU 42 · S             >
🔔 Уведомления  Включены              >

── Внешний вид ──────────────────────────────────
☀/🌙 Светлая/Тёмная тема  [toggle]

── Выйти ────────────────────────────────────────
[→ Выйти из аккаунта]
─────────────────────────────────────────────────
```

### FlowMVI

```kotlin
// ProfileState
data class ProfileState(
    val user: User? = null,
    val isLoading: Boolean = true,
    val isDarkTheme: Boolean = false,
) : MVIState

// ProfileIntent
sealed interface ProfileIntent : MVIIntent {
    data object Load : ProfileIntent
    data object EditProfileClicked : ProfileIntent
    data object SizesClicked : ProfileIntent
    data object SettingsClicked : ProfileIntent
    data object ToggleTheme : ProfileIntent
    data object LogoutClicked : ProfileIntent
    data object LogoutConfirmed : ProfileIntent
}

// ProfileSideEffect
sealed class ProfileSideEffect {
    data object NavigateToEdit : ProfileSideEffect()
    data object NavigateToSizes : ProfileSideEffect()
    data object NavigateToSettings : ProfileSideEffect()
    data object NavigateToAuth : ProfileSideEffect()    // после logout
    data class ShowError(val message: String) : ProfileSideEffect()
}
```

---

## Экран Редактирование профиля (EditProfileScreen)

### Структура экрана

```
← (назад)    [Редактирование]    [Сохранить]
─────────────────────────────────────────────────
         [Аватар + кнопка камеры]
         [Изменить фото]

Имя       [input]
Ник       @ [input]
Пол       [Женский] [Мужской] [Другой]
─────────────────────────────────────────────────
```

### FlowMVI

```kotlin
// EditProfileState
data class EditProfileState(
    val user: User? = null,
    val name: String = "",
    val username: String = "",
    val gender: String? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
) : MVIState

// EditProfileIntent
sealed interface EditProfileIntent : MVIIntent {
    data object Load : EditProfileIntent
    data class NameChanged(val value: String) : EditProfileIntent
    data class UsernameChanged(val value: String) : EditProfileIntent
    data class GenderChanged(val value: String) : EditProfileIntent
    data object SaveClicked : EditProfileIntent
    data object CancelClicked : EditProfileIntent
}

// EditProfileSideEffect
sealed class EditProfileSideEffect {
    data object NavigateBack : EditProfileSideEffect()
    data class ShowError(val message: String) : EditProfileSideEffect()
}
```

---

## Data Layer

### DTO

```kotlin
// internal/data/dto/UserDto.kt
@Serializable
data class UserDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("email") val email: String,
    @SerialName("username") val username: String,
    @SerialName("gender") val gender: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("outfits_count") val outfitsCount: Int = 0,
    @SerialName("clothes_count") val clothesCount: Int = 0,
    @SerialName("shared_count") val sharedCount: Int = 0,
)

// internal/data/dto/UpdateProfileRequest.kt
@Serializable
data class UpdateProfileRequest(
    @SerialName("name") val name: String? = null,
    @SerialName("username") val username: String? = null,
    @SerialName("gender") val gender: String? = null,
)
```

### Mapper

```kotlin
// internal/data/mapper/UserMapper.kt
fun UserDto.toUser(): User = User(
    id = id,
    name = name,
    email = email,
    username = username,
    gender = gender,
    avatarUrl = avatarUrl,
    outfitsCount = outfitsCount,
    clothesCount = clothesCount,
    sharedCount = sharedCount,
)
```

### RepositoryImpl

```kotlin
// internal/data/repository/ProfileRepositoryImpl.kt
class ProfileRepositoryImpl(
    private val api: ProfileApi,
    private val tokenStorage: TokenStorage,
) : ProfileRepository {

    override suspend fun getProfile(): User =
        api.getProfile().toUser()

    override suspend fun updateProfile(
        name: String,
        username: String,
        gender: String?,
    ): User = api.updateProfile(
        UpdateProfileRequest(name = name, username = username, gender = gender)
    ).toUser()

    override suspend fun getUserSizes(): UserSizes =
        api.getUserSizes().toUserSizes()

    override suspend fun updateUserSizes(sizes: UserSizes): UserSizes =
        api.updateUserSizes(sizes.toDto()).toUserSizes()

    override suspend fun logout() {
        tokenStorage.clear()
    }
}
```

### Use Cases

```kotlin
// internal/domain/usecase/GetProfileUseCase.kt
class GetProfileUseCase(private val repository: ProfileRepository) {
    operator fun invoke(): Flow<Outcome<User>> = flow {
        emit(Outcome.Success(repository.getProfile()))
    }.catch { e -> emit(Outcome.Error(e.message ?: "Ошибка загрузки")) }
}

// internal/domain/usecase/UpdateProfileUseCase.kt — Command UseCase
class UpdateProfileUseCase(private val repository: ProfileRepository) {
    suspend operator fun invoke(
        name: String,
        username: String,
        gender: String?,
    ): Result<User> = runCatching {
        repository.updateProfile(name, username, gender)
    }
}

// internal/domain/usecase/LogoutUseCase.kt — Command UseCase
class LogoutUseCase(private val repository: ProfileRepository) {
    suspend operator fun invoke(): Result<Unit> =
        runCatching { repository.logout() }
}
```

---

## Методы репозитория

```kotlin
// internal/domain/repository/ProfileRepository.kt
interface ProfileRepository {
    suspend fun getProfile(): User
    suspend fun updateProfile(name: String, username: String, gender: String?): User
    suspend fun getUserSizes(): UserSizes
    suspend fun updateUserSizes(sizes: UserSizes): UserSizes
    suspend fun logout()
}
```

## Навигационные маршруты

```kotlin
// external/ProfileRoutes.kt
@Serializable object ProfileMain
@Serializable object EditProfile
@Serializable object Sizes
@Serializable object ProfileSettingsRoute
```

## Что нужно исправить в существующем коде

- Заменить хардкодные цвета `Color(0xFF...)` → `Theme.colors.*` (нарушение CLAUDE.md)
- Перейти с `StateFlow` в ViewModel на FlowMVI Store

## Используемые системные компоненты (core/compose/components)

- `AppTopBar` — шапка
- `SettingRow` — строка настройки (иконка + заголовок + подзаголовок + стрелка)
- `StatItem` — элемент статистики (число + подпись)
