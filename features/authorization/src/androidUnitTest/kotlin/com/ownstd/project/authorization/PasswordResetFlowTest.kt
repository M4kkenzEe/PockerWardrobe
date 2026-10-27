package com.ownstd.project.authorization

import com.ownstd.project.authorization.internal.domain.AuthorizationRepository
import com.ownstd.project.authorization.internal.presentation.AuthorizationViewModel
import com.ownstd.project.authorization.internal.presentation.Gender
import com.ownstd.project.authorization.internal.presentation.ViewState
import com.ownstd.project.authorization.internal.presentation.isResendEnabled
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
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

    @Test fun `happy path - request code then reset password lands user in the app`() = runTest(dispatcher) {
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
        assertTrue(vm.isSessionOpen.value, "После сброса пароля юзер входит без повторного логина")
        assertTrue(repo.sessionSaved, "Токены из ответа должны быть сохранены")
        assertNull(vm.errorState.value)
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

        assertTrue(vm.isSessionOpen.value)
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
}

private class FakeAuthRepository : AuthorizationRepository {
    var requestResetError: String? = null
    var resetPasswordError: String? = null
    var delayMs = 0L
    var requestResetCount = 0
    var lastResetArgs: Triple<String, String, String>? = null
    var sessionSaved = false

    override suspend fun loginUser(username: String, password: String): String? = null

    override suspend fun registerUser(
        username: String,
        email: String,
        password: String,
        gender: Gender,
    ): String? = null

    override suspend fun requestPasswordReset(email: String): String? {
        requestResetCount++
        if (delayMs > 0) delay(delayMs)
        return requestResetError
    }

    override suspend fun resetPassword(email: String, code: String, newPassword: String): String? {
        lastResetArgs = Triple(email, code, newPassword)
        if (delayMs > 0) delay(delayMs)
        if (resetPasswordError == null) sessionSaved = true
        return resetPasswordError
    }

    override suspend fun logout() = Unit

    override suspend fun verifyEmail(code: String): String? = null
    override suspend fun resendVerification(): String? = null
}
