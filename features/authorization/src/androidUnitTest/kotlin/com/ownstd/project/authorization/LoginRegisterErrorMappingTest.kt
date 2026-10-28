package com.ownstd.project.authorization

import com.ownstd.project.authorization.internal.data.repository.AuthServiceImpl
import com.ownstd.project.authorization.internal.data.repository.AuthorizationRepositoryImpl
import com.ownstd.project.authorization.internal.presentation.Gender
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/** Real repository and service over MockEngine: asserts the text the user sees for every server answer. */
class LoginRegisterErrorMappingTest {

    private val storage = InMemoryTokenStorage(accessToken = "stale-access", refreshToken = "stale-refresh")
    private var network = RecordingNetworkRepository()

    @Test fun `login success saves the tokens and returns no error`() = runTest {
        val repository = repository(respondingWith(HttpStatusCode.OK, TOKENS_BODY))

        assertNull(repository.loginUser("user@example.com", "SecurePass1"))

        assertEquals(1, storage.saveSessionCalls)
        assertEquals("fresh-access", storage.getAccessToken())
        assertEquals("fresh-refresh", storage.getRefreshToken())
    }

    @Test fun `login success drops the cached http client tokens after saving the session`() = runTest {
        val repository = repository(respondingWith(HttpStatusCode.OK, TOKENS_BODY))

        repository.loginUser("user@example.com", "SecurePass1")

        assertEquals(1, network.authCacheClears)
    }

    @Test fun `register success drops the cached http client tokens`() = runTest {
        val repository = repository(respondingWith(HttpStatusCode.Created, TOKENS_BODY))

        repository.registerUser(null, "a@b.com", "SecurePass1", Gender.OTHER, "verification-token")

        assertEquals(1, network.authCacheClears)
    }

    @Test fun `password reset success leaves the stored session and the http client tokens alone`() = runTest {
        val repository = repository(respondingWith(HttpStatusCode.OK, TOKENS_BODY))

        assertNull(repository.resetPassword("a@b.com", "123456", "SecurePass1"))

        assertEquals(0, storage.saveSessionCalls)
        assertEquals(0, storage.clearSessionCalls)
        assertEquals("stale-access", storage.getAccessToken())
        assertEquals(0, network.authCacheClears)
    }

    @Test fun `failed login drops the cached http client tokens along with the stored session`() = runTest {
        val repository = repository(respondingWith(HttpStatusCode.Unauthorized))

        repository.loginUser("user@example.com", "wrong")

        assertEquals(1, network.authCacheClears)
    }

    @Test fun `failed register keeps the cached http client tokens`() = runTest {
        registerError(respondingWith(HttpStatusCode.Conflict))

        assertEquals(0, network.authCacheClears)
    }

    @Test fun `login failure clears the session`() = runTest {
        val repository = repository(respondingWith(HttpStatusCode.Unauthorized))

        assertNotNull(repository.loginUser("user@example.com", "wrong"))

        assertEquals(1, storage.clearSessionCalls)
        assertEquals(0, storage.saveSessionCalls)
        assertNull(storage.getAccessToken())
    }

    @Test fun `login 400 asks to check the input`() = runTest {
        assertEquals("Проверь введённые данные", loginError(respondingWith(HttpStatusCode.BadRequest)))
    }

    @Test fun `login 401 reports wrong credentials`() = runTest {
        assertEquals("Неверное имя пользователя или пароль", loginError(respondingWith(HttpStatusCode.Unauthorized)))
    }

    @Test fun `login 429 asks to wait`() = runTest {
        assertEquals("Слишком много попыток, подожди минуту", loginError(respondingWith(HttpStatusCode.TooManyRequests)))
    }

    @Test fun `login 5xx says the server is unavailable`() = runTest {
        listOf(HttpStatusCode.InternalServerError, HttpStatusCode.BadGateway, HttpStatusCode.ServiceUnavailable)
            .forEach { status ->
                assertEquals("Сервер временно недоступен, попробуй позже", loginError(respondingWith(status)), "status $status")
            }
    }

    @Test fun `login without network says there is no internet`() = runTest {
        assertEquals("Нет подключения к интернету", loginError(failingWith(IOException("unreachable"))))
    }

    @Test fun `login with an unexpected status shows no raw http text`() = runTest {
        val message = loginError(respondingWith(HttpStatusCode(418, "I am a teapot")))

        assertEquals("Что-то пошло не так. Попробуй позже", message)
    }

    @Test fun `login 200 with an unreadable body shows no raw error text`() = runTest {
        val message = loginError(respondingWith(HttpStatusCode.OK, "not json"))

        assertEquals("Что-то пошло не так. Попробуй позже", message)
    }

    @Test fun `register 400 reports the rejected registration`() = runTest {
        assertEquals(
            "Не удалось создать аккаунт. Проверь пароль или вернись назад и запроси код заново",
            registerError(respondingWith(HttpStatusCode.BadRequest))
        )
    }

    @Test fun `register 409 reports an existing user`() = runTest {
        assertEquals(
            "Пользователь с таким именем или email уже существует",
            registerError(respondingWith(HttpStatusCode.Conflict))
        )
    }

    @Test fun `register 429 asks to wait`() = runTest {
        assertEquals("Слишком много попыток, подожди минуту", registerError(respondingWith(HttpStatusCode.TooManyRequests)))
    }

    @Test fun `register 5xx says the server is unavailable`() = runTest {
        assertEquals(
            "Сервер временно недоступен, попробуй позже",
            registerError(respondingWith(HttpStatusCode.ServiceUnavailable))
        )
    }

    @Test fun `register without network says there is no internet`() = runTest {
        assertEquals("Нет подключения к интернету", registerError(failingWith(IOException("unreachable"))))
    }

    @Test fun `register 201 saves the tokens`() = runTest {
        val repository = repository(respondingWith(HttpStatusCode.Created, TOKENS_BODY))

        assertNull(repository.registerUser(null, "a@b.com", "SecurePass1", Gender.OTHER, "verification-token"))

        assertEquals(1, storage.saveSessionCalls)
    }

    @Test fun `failed register does not touch the stored session`() = runTest {
        registerError(respondingWith(HttpStatusCode.Conflict))

        assertEquals(0, storage.clearSessionCalls)
        assertEquals(0, storage.saveSessionCalls)
    }

    @Test fun `verify code 429 asks to wait`() = runTest {
        assertEquals(
            "Слишком много попыток, подожди минуту",
            verifyCodeError(respondingWith(HttpStatusCode.TooManyRequests))
        )
    }

    @Test fun `verify code 5xx says the server is unavailable`() = runTest {
        assertEquals(
            "Сервер временно недоступен, попробуй позже",
            verifyCodeError(respondingWith(HttpStatusCode.BadGateway))
        )
    }

    @Test fun `verify code without network says there is no internet`() = runTest {
        assertEquals("Нет подключения к интернету", verifyCodeError(failingWith(IOException("unreachable"))))
    }

    @Test fun `verify code 400 keeps the invalid or expired text`() = runTest {
        assertEquals(
            "Неверный или истёкший код. Запроси новый",
            verifyCodeError(respondingWith(HttpStatusCode.BadRequest))
        )
    }

    @Test fun `request code 5xx says the server is unavailable`() = runTest {
        val repository = repository(respondingWith(HttpStatusCode.ServiceUnavailable))

        assertEquals("Сервер временно недоступен, попробуй позже", repository.requestRegistrationCode("a@b.com"))
    }

    @Test fun `request code without network keeps the connection hint`() = runTest {
        val repository = repository(failingWith(IOException("unreachable")))

        assertEquals("Не удалось отправить код. Проверь интернет-соединение", repository.requestRegistrationCode("a@b.com"))
    }

    @Test fun `request code 429 keeps the existing rate limit text`() = runTest {
        val repository = repository(respondingWith(HttpStatusCode.TooManyRequests))

        assertEquals("Слишком много запросов. Подождите немного", repository.requestRegistrationCode("a@b.com"))
    }

    @Test fun `no error text for login or register contains an http status`() = runTest {
        val statuses = listOf(
            HttpStatusCode.BadRequest, HttpStatusCode.Unauthorized, HttpStatusCode.Conflict,
            HttpStatusCode.TooManyRequests, HttpStatusCode.InternalServerError, HttpStatusCode(418, "I am a teapot")
        )
        statuses.forEach { status ->
            val messages = listOf(loginError(respondingWith(status)), registerError(respondingWith(status)))
            messages.forEach { message ->
                assertFalse(message.contains("Unexpected", ignoreCase = true), "status $status: $message")
                assertFalse(Regex("\\b\\d{3}\\b").containsMatchIn(message), "status $status: $message")
            }
        }
    }

    private suspend fun loginError(engine: MockEngine): String =
        checkNotNull(repository(engine).loginUser("user@example.com", "SecurePass1"))

    private suspend fun registerError(engine: MockEngine): String = checkNotNull(
        repository(engine).registerUser(null, "a@b.com", "SecurePass1", Gender.OTHER, "verification-token")
    )

    private suspend fun verifyCodeError(engine: MockEngine): String =
        checkNotNull(repository(engine).verifyRegistrationCode("a@b.com", "123456").exceptionOrNull()?.message)

    private fun repository(engine: MockEngine): AuthorizationRepositoryImpl {
        network = networkRepositoryOver(engine)
        return AuthorizationRepositoryImpl(AuthServiceImpl(network), storage, network)
    }

    private fun respondingWith(status: HttpStatusCode, body: String = "") = MockEngine {
        respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
    }

    private fun failingWith(error: Exception) = MockEngine { throw error }

    private companion object {
        const val TOKENS_BODY =
            """{"accessToken":"fresh-access","refreshToken":"fresh-refresh","expiresAt":4000000000000,"userId":7}"""
    }
}
