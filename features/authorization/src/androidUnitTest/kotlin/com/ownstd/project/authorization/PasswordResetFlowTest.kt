package com.ownstd.project.authorization

import com.ownstd.project.authorization.internal.data.repository.AuthServiceImpl
import com.ownstd.project.authorization.internal.data.repository.AuthorizationRepositoryImpl
import com.ownstd.project.authorization.internal.domain.AuthorizationRepository
import com.ownstd.project.authorization.internal.presentation.AuthorizationViewModel
import com.ownstd.project.authorization.internal.presentation.Gender
import com.ownstd.project.authorization.internal.presentation.PASSWORD_CHANGED_NOTICE
import com.ownstd.project.authorization.internal.presentation.PASSWORD_RULE_ERROR
import com.ownstd.project.authorization.internal.presentation.ViewState
import com.ownstd.project.authorization.internal.presentation.isResendEnabled
import com.ownstd.project.authorization.internal.presentation.resetCodeSentText
import com.ownstd.project.authorization.internal.presentation.sanitizeResetCode
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PasswordResetFlowTest {
    private val dispatcher = StandardTestDispatcher()
    private val repo = FakeAuthRepository()

    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    private fun viewModel() = AuthorizationViewModel(repo, dispatcher)

    @Test fun `happy path - request code then reset password lands user on the login screen`() = runTest(dispatcher) {
        val vm = viewModel()
        assertEquals(ViewState.LOGIN, vm.viewState.value)

        vm.requestPasswordReset("user@example.com")
        runCurrent()

        assertEquals(ViewState.RESET_PASSWORD, vm.viewState.value)
        assertEquals("user@example.com", vm.forgotPasswordEmail.value)
        assertEquals(60, vm.resendCountdown.value)
        assertNull(vm.errorState.value)
        assertFalse(vm.isForgotPasswordLoading.value)

        vm.resetPassword("user@example.com", "123456", "NewPass1")
        runCurrent()

        assertEquals(Triple("user@example.com", "123456", "NewPass1"), repo.lastResetArgs)
        assertFalse(vm.isSessionOpen.value, "После сброса пароля сессия не открывается: юзер входит с новым паролем")
        assertEquals(ViewState.LOGIN, vm.viewState.value)
        assertEquals(PASSWORD_CHANGED_NOTICE, vm.passwordResetNotice.value)
        assertEquals("user@example.com", vm.loginPrefillEmail.value)
        assertNull(vm.errorState.value)
    }

    @Test fun `successful reset drops the recovery state so the code screen cannot come back`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.requestPasswordReset("user@example.com")
        runCurrent()
        vm.resendPasswordResetCode("user@example.com")
        runCurrent()
        assertTrue(vm.isResetCodeResent.value)

        vm.resetPassword("user@example.com", "123456", "NewPass1")
        runCurrent()

        assertEquals("", vm.forgotPasswordEmail.value)
        assertFalse(vm.isResetCodeResent.value)
        assertEquals(0, vm.resendCountdown.value)
        assertFalse(vm.isForgotPasswordLoading.value)
        advanceTimeBy(60_000)
        runCurrent()
        assertEquals(0, vm.resendCountdown.value, "Таймер ресенда не должен тикать после выхода с экрана кода")
    }

    @Test fun `the notice disappears with the next login attempt`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.resetPassword("user@example.com", "123456", "NewPass1")
        runCurrent()
        assertNotNull(vm.passwordResetNotice.value)

        vm.loginUser("user@example.com", "NewPass1")
        runCurrent()

        assertNull(vm.passwordResetNotice.value)
    }

    @Test fun `the notice disappears when registration starts`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.resetPassword("user@example.com", "123456", "NewPass1")
        runCurrent()

        vm.startRegistration()

        assertNull(vm.passwordResetNotice.value)
    }

    @Test fun `email delivery failure keeps user on the forgot password screen`() = runTest(dispatcher) {
        repo.requestResetError = "Не удалось отправить письмо. Проверь интернет-соединение"
        val vm = viewModel()
        vm.viewState.value = ViewState.FORGOT_PASSWORD

        vm.requestPasswordReset("user@example.com")
        advanceUntilIdle()

        assertEquals(ViewState.FORGOT_PASSWORD, vm.viewState.value, "Без отправленного кода переход на ввод кода бессмыслен")
        assertNotNull(vm.errorState.value)
        assertEquals(0, vm.resendCountdown.value, "Кулдаун не должен стартовать, если письмо не ушло")
        assertFalse(vm.isForgotPasswordLoading.value)
    }

    @Test fun `wrong code shows error and does not open a session`() = runTest(dispatcher) {
        repo.resetPasswordError = "Неверный код или email"
        val vm = viewModel()

        vm.resetPassword("user@example.com", "000000", "NewPass1")
        advanceUntilIdle()

        assertFalse(vm.isSessionOpen.value)
        assertEquals("Неверный код или email", vm.errorState.value)
        assertNull(vm.passwordResetNotice.value)
        assertFalse(vm.isForgotPasswordLoading.value)
    }

    @Test fun `retry after a wrong code succeeds and clears the error`() = runTest(dispatcher) {
        repo.resetPasswordError = "Неверный код или email"
        val vm = viewModel()
        vm.resetPassword("user@example.com", "000000", "NewPass1")
        advanceUntilIdle()
        assertFalse(vm.isSessionOpen.value)

        repo.resetPasswordError = null
        vm.resetPassword("user@example.com", "123456", "NewPass1")
        advanceUntilIdle()

        assertFalse(vm.isSessionOpen.value)
        assertEquals(ViewState.LOGIN, vm.viewState.value)
        assertNull(vm.errorState.value, "Прошлая ошибка не должна висеть после успеха")
    }

    @Test fun `resend stays locked for the whole cooldown then restarts it`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.requestPasswordReset("user@example.com")
        runCurrent()
        assertEquals(1, repo.requestResetCount)
        assertFalse(isResendEnabled(vm.resendCountdown.value, vm.isForgotPasswordLoading.value))

        advanceTimeBy(30_000)
        runCurrent()
        assertEquals(30, vm.resendCountdown.value)
        assertFalse(isResendEnabled(vm.resendCountdown.value, vm.isForgotPasswordLoading.value))

        advanceTimeBy(30_000)
        runCurrent()
        assertEquals(0, vm.resendCountdown.value)
        assertTrue(isResendEnabled(vm.resendCountdown.value, vm.isForgotPasswordLoading.value))

        vm.resendPasswordResetCode("user@example.com")
        runCurrent()

        assertEquals(2, repo.requestResetCount)
        assertEquals(60, vm.resendCountdown.value, "Повторная отправка перезапускает кулдаун")
        assertEquals(ViewState.RESET_PASSWORD, vm.viewState.value, "Resend не уводит с экрана ввода кода")
    }

    @Test fun `spinner is shown while the request is in flight`() = runTest(dispatcher) {
        repo.delayMs = 500
        val vm = viewModel()

        vm.requestPasswordReset("user@example.com")
        runCurrent()
        assertTrue(vm.isForgotPasswordLoading.value)

        advanceUntilIdle()
        assertFalse(vm.isForgotPasswordLoading.value)
    }

    @Test fun `double tap on send code reaches the server once`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.requestPasswordReset("user@example.com")
        vm.requestPasswordReset("user@example.com")
        advanceUntilIdle()

        assertEquals(1, repo.requestResetCount, "A second request would silently invalidate the code from the first email")
    }

    @Test fun `double tap on change password sends one request`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.resetPassword("user@example.com", "123456", "NewPass1")
        vm.resetPassword("user@example.com", "123456", "NewPass1")
        advanceUntilIdle()

        assertEquals(1, repo.resetCount)
    }

    @Test fun `email is trimmed before the code is requested`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.requestPasswordReset("  user@example.com ")
        advanceUntilIdle()

        assertEquals("user@example.com", repo.lastRequestEmail)
        assertEquals("user@example.com", vm.forgotPasswordEmail.value)
    }

    @Test fun `resend from the code screen tells that only the latest email works`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.requestPasswordReset("user@example.com")
        advanceUntilIdle()
        assertFalse(vm.isResetCodeResent.value)

        vm.resendPasswordResetCode("user@example.com")
        advanceUntilIdle()

        assertTrue(vm.isResetCodeResent.value)
        assertEquals(
            "Новый код отправлен на user@example.com. Действует только код из последнего письма",
            resetCodeSentText(vm.forgotPasswordEmail.value, vm.isResetCodeResent.value)
        )
    }

    @Test fun `pasted code with spaces or a newline keeps its six digits`() {
        assertEquals("934754", sanitizeResetCode("934 754"))
        assertEquals("934754", sanitizeResetCode("934754\n"))
        assertEquals("934754", sanitizeResetCode("9347541"))
    }

    @Test fun `wrong code from the backend is shown as wrong code`() = runTest(dispatcher) {
        assertEquals(
            "Неверный код. Действует только код из последнего письма",
            errorShownFor(HttpStatusCode.BadRequest, """{"error":"INVALID_CODE","message":"Invalid code or email"}""")
        )
    }

    @Test fun `expired code from the backend is shown as expired`() = runTest(dispatcher) {
        assertEquals(
            "Код истёк или уже использован. Запроси новый",
            errorShownFor(HttpStatusCode.BadRequest, """{"error":"CODE_EXPIRED","message":"Code has expired or was already used"}""")
        )
    }

    @Test fun `rejected password from the backend is shown as a password problem`() = runTest(dispatcher) {
        assertEquals(
            "Пароль не подходит. $PASSWORD_RULE_ERROR",
            errorShownFor(HttpStatusCode.BadRequest, """{"error":"INVALID_PASSWORD","message":"Password must contain at least one digit"}""")
        )
    }

    @Test fun `unknown error code from an older backend gets a general message`() = runTest(dispatcher) {
        assertEquals(
            "Не удалось сменить пароль. Проверь код из последнего письма или запроси новый",
            errorShownFor(HttpStatusCode.BadRequest, """{"error":"BadRequest","message":"Invalid or expired code"}""")
        )
    }

    @Test fun `rate limit is shown as too many requests`() = runTest(dispatcher) {
        assertEquals(
            "Слишком много запросов. Подождите немного",
            errorShownFor(HttpStatusCode.TooManyRequests, """{"error":"Too many requests","message":"Rate limit exceeded"}""")
        )
    }

    @Test fun `server failure is shown as server unavailable`() = runTest(dispatcher) {
        assertEquals("Сервер временно недоступен. Попробуй позже", errorShownFor(HttpStatusCode.BadGateway, ""))
    }

    @Test fun `network failure is shown as no connection`() = runTest(dispatcher) {
        val vm = AuthorizationViewModel(realRepository(MockEngine { throw IOException("offline") }), dispatcher)

        vm.resetPassword("user@example.com", "934754", "Qwerty1234!")
        awaitRequestFinished(vm)

        assertEquals("Нет связи с сервером. Проверь интернет-соединение", vm.errorState.value)
    }

    @Test fun `reset answered with a message writes no tokens and leaves the session closed`() = runTest(dispatcher) {
        assertResetLeavesSessionClosed("""{"message":"Password has been reset"}""")
    }

    @Test fun `reset answered with tokens by an older backend writes no tokens either`() = runTest(dispatcher) {
        assertResetLeavesSessionClosed(
            """{"accessToken":"leaked-access","refreshToken":"leaked-refresh","expiresAt":4000000000000,"userId":7}"""
        )
    }

    @Test fun `reset answered with an empty body still succeeds`() = runTest(dispatcher) {
        assertResetLeavesSessionClosed("")
    }

    private suspend fun assertResetLeavesSessionClosed(responseBody: String) {
        val network = networkRepositoryOver(respondingWith(HttpStatusCode.OK, responseBody))
        val tokenStorage = InMemoryTokenStorage()
        val vm = AuthorizationViewModel(
            AuthorizationRepositoryImpl(AuthServiceImpl(network), tokenStorage, network),
            dispatcher
        )

        vm.resetPassword("user@example.com", "934754", "Qwerty1234!")
        awaitRequestFinished(vm)

        assertNull(vm.errorState.value)
        assertEquals(ViewState.LOGIN, vm.viewState.value)
        assertEquals(PASSWORD_CHANGED_NOTICE, vm.passwordResetNotice.value)
        assertFalse(vm.isSessionOpen.value)
        assertEquals(0, tokenStorage.saveSessionCalls)
        assertNull(tokenStorage.getAccessToken())
        assertNull(tokenStorage.getRefreshToken())
        assertEquals(0, network.authCacheClears)
    }

    @Test fun `rate limit on send code is shown as too many requests`() = runTest(dispatcher) {
        assertEquals("Слишком много запросов. Подождите немного", forgotErrorShownFor(respondingWith(HttpStatusCode.TooManyRequests, "")))
    }

    @Test fun `server failure on send code asks to retry later`() = runTest(dispatcher) {
        assertEquals(
            "Не удалось отправить письмо. Попробуй позже",
            forgotErrorShownFor(respondingWith(HttpStatusCode.InternalServerError, """{"error":"EmailError","message":"Не удалось отправить письмо"}"""))
        )
    }

    @Test fun `network failure on send code asks to check the connection`() = runTest(dispatcher) {
        assertEquals(
            "Не удалось отправить письмо. Проверь интернет-соединение",
            forgotErrorShownFor(MockEngine { throw IOException("offline") })
        )
    }

    private suspend fun forgotErrorShownFor(engine: MockEngine): String? {
        val vm = AuthorizationViewModel(realRepository(engine), dispatcher)

        vm.requestPasswordReset("user@example.com")
        awaitRequestFinished(vm)

        assertEquals(ViewState.LOGIN, vm.viewState.value, "Without a sent code there is no code screen to show")
        return vm.errorState.value
    }

    private fun respondingWith(status: HttpStatusCode, body: String) = MockEngine {
        respond(content = body, status = status, headers = headersOf(HttpHeaders.ContentType, "application/json"))
    }

    private suspend fun errorShownFor(status: HttpStatusCode, body: String): String? {
        val vm = AuthorizationViewModel(realRepository(respondingWith(status, body)), dispatcher)

        vm.resetPassword("user@example.com", "934754", "Qwerty1234!")
        awaitRequestFinished(vm)

        assertFalse(vm.isSessionOpen.value)
        return vm.errorState.value
    }

    // MockEngine answers on Ktor's own threads, so virtual time alone does not wait for the response.
    private suspend fun awaitRequestFinished(vm: AuthorizationViewModel) {
        vm.isForgotPasswordLoading.first { !it }
    }

    private fun realRepository(engine: MockEngine): AuthorizationRepository {
        val network = networkRepositoryOver(engine)
        return AuthorizationRepositoryImpl(AuthServiceImpl(network), InMemoryTokenStorage(), network)
    }
}

private class FakeAuthRepository : AuthorizationRepository {
    var requestResetError: String? = null
    var resetPasswordError: String? = null
    var delayMs = 0L
    var requestResetCount = 0
    var lastRequestEmail: String? = null
    var resetCount = 0
    var lastResetArgs: Triple<String, String, String>? = null

    override suspend fun loginUser(username: String, password: String): String? = null

    override suspend fun registerUser(
        username: String?,
        email: String,
        password: String,
        gender: Gender,
        verificationToken: String,
    ): String? = null

    override suspend fun requestPasswordReset(email: String): String? {
        requestResetCount++
        lastRequestEmail = email
        if (delayMs > 0) delay(delayMs)
        return requestResetError
    }

    override suspend fun resetPassword(email: String, code: String, newPassword: String): String? {
        resetCount++
        lastResetArgs = Triple(email, code, newPassword)
        if (delayMs > 0) delay(delayMs)
        return resetPasswordError
    }

    override suspend fun logout() = Unit

    override suspend fun verifyEmail(code: String): String? = null
    override suspend fun resendVerification(): String? = null
    override suspend fun requestRegistrationCode(email: String): String? = null
    override suspend fun verifyRegistrationCode(email: String, code: String): Result<String> = Result.success("token")
}
