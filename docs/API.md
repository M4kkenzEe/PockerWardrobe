# API Integration в проекте PockerWardrobe

Этот документ описывает принципы и практики работы с API в проекте PockerWardrobe, использующем Kotlin Multiplatform (KMP) архитектуру.

## Оглавление

1. [Архитектура API слоя](#архитектура-api-слоя)
2. [Структура модулей](#структура-модулей)
3. [Сетевой слой (core/network)](#сетевой-слой-corenetwork)
4. [Создание API интеграции](#создание-api-интеграции)
5. [Обработка ошибок](#обработка-ошибок)
6. [Аутентификация](#аутентификация)
7. [Примеры из проекта](#примеры-из-проекта)
8. [Best Practices](#best-practices)
9. [Чек-лист](#чек-лист)

---

## Архитектура API слоя

Проект использует **трёхслойную архитектуру** для работы с API:

```
┌─────────────────────────────────────────────────────┐
│  Presentation Layer (ViewModel)                      │
│  - Jetpack ViewModel + Compose                       │
│  - StateFlow для UI состояния                        │
│  - viewModelScope для корутин                        │
└─────────────────┬───────────────────────────────────┘
                  │ использует
┌─────────────────▼───────────────────────────────────┐
│  Domain Layer (Repository Interface + UseCase)       │
│  - Интерфейсы репозиториев                           │
│  - UseCase для бизнес-логики                         │
└─────────────────┬───────────────────────────────────┘
                  │ реализует
┌─────────────────▼───────────────────────────────────┐
│  Data Layer (Repository Implementation)              │
│  - Реализация репозиториев с Ktor                    │
│  - DTO модели с @Serializable                        │
│  - Маппинг DTO → Domain Models                       │
└─────────────────────────────────────────────────────┘
```

### Ключевые принципы:

1. **Разделение ответственности**: Domain интерфейсы отделены от реализации
2. **Clean Architecture**: Каждый feature модуль имеет internal/external структуру
3. **Kotlin Result**: Использование `Result<T>` или sealed classes для обработки ошибок
4. **Ktor напрямую**: HTTP клиент без дополнительных обёрток (Ktorfit не используется)
5. **Koin DI**: Dependency Injection через Koin 4.x

---

## Структура модулей

### Общая структура проекта

```
├── core/
│   ├── network/                    # HTTP клиент и конфигурация сервера
│   │   └── src/commonMain/kotlin/com/ownstd/project/network/
│   │       ├── api/                # Публичные интерфейсы
│   │       │   ├── NetworkRepository.kt
│   │       │   ├── ServerConfig.kt
│   │       │   └── di/NetworkModule.kt
│   │       └── internal/           # Внутренняя реализация
│   │           ├── NetworkClient.kt
│   │           └── NetworkRepositoryImpl.kt
│   └── storage/                    # Хранение токенов
│       └── src/commonMain/kotlin/com/ownstd/project/storage/
│           ├── TokenStorage.kt
│           ├── TokenStorageImpl.kt
│           └── di/StorageModule.kt
│
└── features/
    └── {feature-name}/             # Feature модуль
        └── src/commonMain/kotlin/com/ownstd/project/{feature}/
            ├── di/                 # Koin модуль
            │   └── {Feature}Module.kt
            ├── internal/           # Внутренняя реализация
            │   ├── data/           # Data layer
            │   │   ├── model/      # DTO и модели данных
            │   │   └── repository/ # Реализации репозиториев
            │   ├── domain/         # Domain layer
            │   │   ├── repository/ # Интерфейсы репозиториев
            │   │   └── usecase/    # Use cases
            │   └── presentation/   # UI layer
            │       └── {Feature}ViewModel.kt
            └── external/           # Публичный API модуля
```

### Feature Module Pattern

Каждый feature следует **Clean Architecture** с internal/external visibility:

```kotlin
// internal/ - видно только внутри модуля
internal interface WardrobeRepository { ... }
internal class WardrobeRepositoryImpl(...) : WardrobeRepository { ... }
internal class WardrobeViewModel(...) : ViewModel() { ... }

// external/ - публичный API для других модулей
// Например: навигационные routes, shared models
```

---

## Сетевой слой (core/network)

### NetworkClient

Конфигурация Ktor HttpClient:

```kotlin
// core/network/.../internal/NetworkClient.kt
package com.ownstd.project.network.internal

internal object NetworkClient {
    fun getClient(): HttpClient {
        return HttpClient {
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
            }
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }
    }
}
```

### ServerConfig

Централизованная конфигурация сервера:

```kotlin
// core/network/.../api/ServerConfig.kt
package com.ownstd.project.network.api

object ServerConfig {
    const val SERVER_HOST = "194.87.190.248:80"
    const val BASE_URL = "http://$SERVER_HOST/api/v1/"
}
```

### NetworkRepository

Интерфейс для доступа к HTTP клиенту:

```kotlin
// core/network/.../api/NetworkRepository.kt
package com.ownstd.project.network.api

interface NetworkRepository {
    val baseUrl: String
    fun getClient(): HttpClient
}
```

### DI Module

```kotlin
// core/network/.../api/di/NetworkModule.kt
package com.ownstd.project.network.api.di

val networkModule = module {
    single<NetworkRepository> { NetworkRepositoryImpl(ServerConfig.BASE_URL) }
}
```

---

## Создание API интеграции

### Шаг 1: Создание DTO модели

DTO модели находятся в `internal/data/model/` и аннотированы `@Serializable`:

```kotlin
// features/{feature}/internal/data/model/MyModel.kt
package com.ownstd.project.{feature}.internal.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Clothe(
    val id: Int?,
    val name: String,
    val storeUrl: String,
    val imageUrl: String,
)
```

**Правила:**
- Используйте `@Serializable` для всех DTO
- Используйте nullable типы для опциональных полей
- Именуйте поля в camelCase (Ktor/kotlinx.serialization поддерживает автоматический маппинг)

### Шаг 2: Создание интерфейса репозитория (Domain)

Интерфейс определяет контракт для работы с данными:

```kotlin
// features/{feature}/internal/domain/repository/MyRepository.kt
package com.ownstd.project.{feature}.internal.domain.repository

internal interface WardrobeRepository {
    suspend fun getClothes(): List<Clothe>
    suspend fun loadClothe(bitmap: ImageBitmap)
    suspend fun uploadFromUrl(pageUrl: String): Clothe
    suspend fun deleteClothe(clotheId: Int)
}
```

**Правила:**
- Используйте `internal` modifier для ограничения видимости
- Используйте `suspend` функции для асинхронных операций
- Возвращайте Domain Models или Result<T> для обработки ошибок

### Шаг 3: Реализация репозитория (Data)

Реализация использует Ktor HttpClient напрямую:

```kotlin
// features/{feature}/internal/data/repository/MyRepositoryImpl.kt
package com.ownstd.project.{feature}.internal.data.repository

class WardrobeRepositoryImpl(
    private val networkRepository: NetworkRepository,
    private val storage: TokenStorage
) : WardrobeRepository {

    private val client = networkRepository.getClient()
    private val baseUrl = networkRepository.baseUrl

    private fun getToken(): String? = storage.getToken()

    override suspend fun getClothes(): List<Clothe> {
        return try {
            val response = client.get(baseUrl + ENDPOINT) {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer ${getToken()}")
            }
            response.body()
        } catch (e: Exception) {
            emptyList()
        }
    }

    companion object {
        private const val ENDPOINT = "clothes"
    }
}
```

### Шаг 4: Регистрация в Koin модуле

```kotlin
// features/{feature}/di/{Feature}Module.kt
package com.ownstd.project.{feature}.di

fun featureModule(): Module = module {
    includes(networkModule)
    includes(storageModule)

    factory<WardrobeRepository> {
        WardrobeRepositoryImpl(
            networkRepository = get(),
            storage = get()
        )
    }

    viewModel<WardrobeViewModel> { WardrobeViewModel(repository = get()) }
}
```

---

## Обработка ошибок

### Kotlin Result<T>

Для простых сценариев используйте `Result<T>`:

```kotlin
// Domain interface
interface AuthService {
    suspend fun login(username: String, password: String): Result<String>
    suspend fun register(...): Result<User>
}

// Implementation
override suspend fun login(username: String, password: String): Result<String> {
    return try {
        val response = client.post(baseUrl + LOGIN_URL) {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(username, password))
        }
        when (response.status) {
            HttpStatusCode.OK -> Result.success(response.body<LoginResponse>().token)
            HttpStatusCode.Unauthorized -> Result.failure(Exception("Invalid credentials"))
            else -> Result.failure(Exception("Unexpected response: ${response.status}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

### Sealed Classes для сложных сценариев

Для более детальной обработки ошибок используйте sealed classes:

```kotlin
// features/{feature}/internal/data/model/RepositoryResult.kt
sealed class LookRepositoryResult<out T> {
    data class Success<out T>(val data: T) : LookRepositoryResult<T>()
    data class BadRequest(val message: String) : LookRepositoryResult<Nothing>()
    data class InternalServerError(val message: String) : LookRepositoryResult<Nothing>()
    data class NetworkError(val exception: Throwable) : LookRepositoryResult<Nothing>()
}

// Usage in repository
override suspend fun addLook(look: DraftLook, image: ByteArray): LookRepositoryResult<Unit> {
    return try {
        // ... upload logic
        LookRepositoryResult.Success(Unit)
    } catch (e: Exception) {
        LookRepositoryResult.NetworkError(e)
    }
}
```

### Обработка в ViewModel

```kotlin
// С Result<T>
fun loginUser(username: String, password: String) {
    viewModelScope.launch(Dispatchers.IO) {
        val result = authorizationRepository.loginUser(username, password)
        if (result) {
            isSessionOpen.value = true
        } else {
            errorState.value = true
        }
    }
}

// С sealed class
fun addLook(look: DraftLook, image: ByteArray) {
    viewModelScope.launch(Dispatchers.IO) {
        when (val result = lookRepository.addLook(look, image)) {
            is LookRepositoryResult.Success -> {
                // Handle success
            }
            is LookRepositoryResult.NetworkError -> {
                errorMessage.value = "Network error: ${result.exception.message}"
            }
            is LookRepositoryResult.BadRequest -> {
                errorMessage.value = result.message
            }
            is LookRepositoryResult.InternalServerError -> {
                errorMessage.value = "Server error"
            }
        }
    }
}
```

---

## Аутентификация

### TokenStorage

Интерфейс для хранения JWT токенов:

```kotlin
// core/storage/.../TokenStorage.kt
interface TokenStorage {
    fun saveToken(token: String)
    fun getToken(): String?
    fun clearToken()
}
```

### Реализация с multiplatform-settings

```kotlin
// core/storage/.../TokenStorageImpl.kt
internal class TokenStorageImpl(
    private val settings: Settings
) : TokenStorage {
    private companion object {
        const val TOKEN_KEY = "jwt_token"
    }

    override fun saveToken(token: String) {
        settings[TOKEN_KEY] = token
    }

    override fun getToken(): String? {
        return settings.getStringOrNull(TOKEN_KEY)
    }

    override fun clearToken() {
        settings.remove(TOKEN_KEY)
    }
}
```

### Добавление Authorization header

```kotlin
// В репозитории
private fun getToken(): String? = storage.getToken()

override suspend fun getClothes(): List<Clothe> {
    return client.get(baseUrl + ENDPOINT) {
        contentType(ContentType.Application.Json)
        header("Authorization", "Bearer ${getToken()}")
    }.body()
}
```

---

## Примеры из проекта

### Пример 1: AuthServiceImpl — Регистрация и логин

```kotlin
// features/authorization/internal/data/repository/AuthServiceImpl.kt

class AuthServiceImpl(
    private val networkRepository: NetworkRepository
) : AuthService {

    private val client = networkRepository.getClient()
    private val baseUrl = networkRepository.baseUrl

    override suspend fun register(
        username: String,
        email: String,
        password: String,
        gender: String
    ): Result<User> {
        return try {
            val response = client.post(baseUrl + REGISTER_URL) {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest(username, email, password, gender))
            }
            when (response.status) {
                HttpStatusCode.Created -> Result.success(response.body<User>())
                HttpStatusCode.Conflict -> Result.failure(
                    Exception("User with this username or email already exists")
                )
                else -> Result.failure(Exception("Unexpected response: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(username: String, password: String): Result<String> {
        return try {
            val response = client.post(baseUrl + LOGIN_URL) {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(username, password))
            }
            when (response.status) {
                HttpStatusCode.OK -> Result.success(response.body<LoginResponse>().token)
                HttpStatusCode.Unauthorized -> Result.failure(Exception("Invalid credentials"))
                else -> Result.failure(Exception("Unexpected response: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        const val REGISTER_URL = "register"
        const val LOGIN_URL = "login"
    }
}
```

### Пример 2: WardrobeRepositoryImpl — CRUD операции

```kotlin
// features/wardrobe-feature/internal/data/repository/WardrobeRepositoryImpl.kt

class WardrobeRepositoryImpl(
    private val networkRepository: NetworkRepository,
    private val storage: TokenStorage
) : WardrobeRepository {

    private val client = networkRepository.getClient()
    private val baseUrl = networkRepository.baseUrl

    private fun getToken(): String? = storage.getToken()

    // GET запрос с авторизацией
    override suspend fun getClothes(): List<Clothe> {
        return try {
            client.get(baseUrl + ENDPOINT) {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer ${getToken()}")
            }.body()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // GET с query параметрами
    override suspend fun uploadFromUrl(pageUrl: String): Clothe {
        val response = client.get(baseUrl + ENDPOINT + FROM_URL) {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer ${getToken()}")
            url {
                parameters.append("url", pageUrl)
            }
        }
        return if (response.status.isSuccess()) {
            response.body()
        } else {
            Clothe.empty()
        }
    }

    // DELETE запрос
    override suspend fun deleteClothe(clotheId: Int) {
        try {
            client.delete("$baseUrl$ENDPOINT/$clotheId") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer ${getToken()}")
            }
        } catch (e: Exception) {
            println("Error: ${e.message}")
        }
    }

    companion object {
        private const val ENDPOINT = "clothes"
        private const val FROM_URL = "/from_url"
    }
}
```

### Пример 3: LookRepositoryImpl — Multipart upload

```kotlin
// features/wardrobe-feature/internal/data/repository/LookRepositoryImpl.kt

override suspend fun addLook(
    look: DraftLook,
    image: ByteArray
): LookRepositoryResult<Unit> {
    return try {
        // Шаг 1: Загрузка изображения
        val uploadResponse = client.post(baseUrl + ENDPOINT + ADD_IMAGE) {
            header("Authorization", "Bearer ${getToken()}")
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("image", image, Headers.build {
                            append(HttpHeaders.ContentType, "image/jpeg")
                            append(HttpHeaders.ContentDisposition, "filename=\"image.jpg\"")
                        })
                    }
                )
            )
        }

        val uploadResult = uploadResponse.body<Map<String, String>>()
        val imageUrl = uploadResult["imageUrl"]
            ?: return LookRepositoryResult.NetworkError(
                IllegalStateException("Image URL missing in response")
            )

        // Шаг 2: Создание Look с URL изображения
        val lookWithImage = Look(
            name = look.name,
            lookItems = look.lookItems,
            url = imageUrl
        )

        client.post(baseUrl + ENDPOINT) {
            header("Authorization", "Bearer ${getToken()}")
            contentType(ContentType.Application.Json)
            setBody(lookWithImage)
        }

        LookRepositoryResult.Success(Unit)
    } catch (e: Exception) {
        LookRepositoryResult.NetworkError(e)
    }
}
```

---

## Best Practices

### DO (Делайте)

1. **Используйте Ktor напрямую**
   ```kotlin
   // Правильно
   client.get(baseUrl + endpoint) {
       contentType(ContentType.Application.Json)
       header("Authorization", "Bearer $token")
   }.body<ResponseType>()
   ```

2. **Используйте Result<T> или sealed classes для ошибок**
   ```kotlin
   // Правильно
   suspend fun getData(): Result<Data>
   suspend fun addItem(): RepositoryResult<Unit>
   ```

3. **Выносите endpoints в companion object**
   ```kotlin
   // Правильно
   companion object {
       private const val ENDPOINT = "clothes"
       private const val FROM_URL = "/from_url"
   }
   ```

4. **Используйте internal modifier для реализаций**
   ```kotlin
   // Правильно
   internal interface WardrobeRepository { ... }
   internal class WardrobeRepositoryImpl(...) : WardrobeRepository { ... }
   ```

5. **Инжектируйте зависимости через конструктор**
   ```kotlin
   // Правильно
   class MyRepositoryImpl(
       private val networkRepository: NetworkRepository,
       private val storage: TokenStorage
   ) : MyRepository
   ```

6. **Используйте Dispatchers.IO для сетевых операций в ViewModel**
   ```kotlin
   // Правильно
   viewModelScope.launch(Dispatchers.IO) {
       val result = repository.getData()
       // ...
   }
   ```

### DON'T (Не делайте)

1. **Не используйте Ktorfit** — проект использует Ktor напрямую
   ```kotlin
   // Неправильно
   @GET("endpoint")
   suspend fun getData(): Response
   ```

2. **Не используйте Arrow Either** — проект использует Kotlin Result
   ```kotlin
   // Неправильно
   suspend fun getData(): Either<Throwable, Data>
   ```

3. **Не возвращайте null для ошибок**
   ```kotlin
   // Неправильно
   suspend fun getData(): Data?

   // Правильно
   suspend fun getData(): Result<Data>
   ```

4. **Не храните токен в репозитории**
   ```kotlin
   // Неправильно
   class MyRepository {
       private var token: String? = null
   }

   // Правильно — используйте TokenStorage
   class MyRepository(private val storage: TokenStorage) {
       private fun getToken() = storage.getToken()
   }
   ```

5. **Не делайте сетевые вызовы в Main thread**
   ```kotlin
   // Неправильно (в ViewModel)
   viewModelScope.launch {
       repository.getData() // Main thread!
   }

   // Правильно
   viewModelScope.launch(Dispatchers.IO) {
       repository.getData()
   }
   ```

---

## Чек-лист

### При создании нового API endpoint

- [ ] Создана DTO модель с `@Serializable` в `internal/data/model/`
- [ ] Создан или обновлён интерфейс репозитория в `internal/domain/repository/`
- [ ] Реализован репозиторий в `internal/data/repository/`
- [ ] Добавлен Authorization header (если требуется)
- [ ] Обработаны ошибки (Result или sealed class)
- [ ] Зарегистрированы зависимости в Koin модуле
- [ ] Endpoint вынесен в companion object

### При использовании в ViewModel

- [ ] Используется `viewModelScope.launch(Dispatchers.IO)`
- [ ] Обработан Result/sealed class результат
- [ ] Обновлён UI state через StateFlow

### При добавлении нового feature модуля

- [ ] Создана структура `internal/data/`, `internal/domain/`, `internal/presentation/`
- [ ] Создан Koin модуль с `includes(networkModule, storageModule)`
- [ ] Модуль добавлен в корневой `initKoin()`

---

## Полезные ссылки

- **Ktor Client**: https://ktor.io/docs/client.html
- **Kotlinx Serialization**: https://github.com/Kotlin/kotlinx.serialization
- **Koin**: https://insert-koin.io/docs/quickstart/kotlin
- **Multiplatform Settings**: https://github.com/russhwolf/multiplatform-settings
- **Jetpack Navigation Compose**: https://developer.android.com/jetpack/compose/navigation

---

**Последнее обновление**: 2025-01-27
**Версия документа**: 2.0
