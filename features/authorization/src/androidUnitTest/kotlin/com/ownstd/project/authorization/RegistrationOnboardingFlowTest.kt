package com.ownstd.project.authorization

import com.ownstd.project.authorization.internal.domain.AuthorizationRepository
import com.ownstd.project.authorization.internal.presentation.AuthorizationViewModel
import com.ownstd.project.authorization.internal.presentation.Gender
import com.ownstd.project.authorization.internal.presentation.ViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class RegistrationOnboardingFlowTest {
    private val dispatcher = StandardTestDispatcher()
    private val repo = FakeOnboardingRepository()

    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    private fun viewModel() = AuthorizationViewModel(repo, dispatcher)

    @Test fun `startRegistration transitions to onboarding name step`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.startRegistration()
        assertEquals(ViewState.ONBOARDING_NAME, vm.viewState.value)
    }

    @Test fun `name step transitions to email step and saves name`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.startRegistration()
        vm.onNameNext("Юра")
        assertEquals(ViewState.ONBOARDING_EMAIL, vm.viewState.value)
        assertEquals("Юра", vm.registrationFirstName.value)
    }

    @Test fun `email step transitions to password step and saves email`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.startRegistration()
        vm.onNameNext("Юра")
        vm.onEmailNext("yura@example.com")
        assertEquals(ViewState.ONBOARDING_PASSWORD, vm.viewState.value)
        assertEquals("yura@example.com", vm.registrationEmail.value)
    }

    @Test fun `happy path - submit password then verify OTP opens session`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.startRegistration()
        vm.onNameNext("Юра")
        vm.onEmailNext("yura@example.com")

        vm.submitRegistration("SecurePass1")
        // runCurrent() runs the registration + timer first step without advancing time
        // so countdown is 60; advanceUntilIdle() would run the 60s timer to completion
        runCurrent()

        assertEquals(ViewState.ONBOARDING_OTP, vm.viewState.value)
        assertEquals(60, vm.registrationResendCountdown.value)
        assertNull(vm.errorState.value)

        vm.verifyEmail("123456")
        advanceUntilIdle()

        assertTrue(vm.isSessionOpen.value, "После верификации OTP пользователь должен войти")
        assertNull(vm.otpError.value)
    }

    @Test fun `wrong OTP code shows inline error and does not open session`() = runTest(dispatcher) {
        repo.verifyEmailError = "Неверный код"
        val vm = viewModel()
        vm.startRegistration()
        vm.onNameNext("Юра")
        vm.onEmailNext("yura@example.com")
        vm.submitRegistration("SecurePass1")
        advanceUntilIdle()

        vm.verifyEmail("000000")
        advanceUntilIdle()

        assertFalse(vm.isSessionOpen.value)
        assertNotNull(vm.otpError.value, "Ошибка OTP должна появиться inline")
        assertEquals(ViewState.ONBOARDING_OTP, vm.viewState.value)
    }

    @Test fun `registration failure shows error and stays on password step`() = runTest(dispatcher) {
        repo.registerError = "Email уже используется"
        val vm = viewModel()
        vm.startRegistration()
        vm.onNameNext("Юра")
        vm.onEmailNext("yura@example.com")
        vm.submitRegistration("SecurePass1")
        advanceUntilIdle()

        assertNotNull(vm.errorState.value)
        assertEquals(ViewState.ONBOARDING_PASSWORD, vm.viewState.value)
        assertFalse(vm.isRegistrationLoading.value)
    }

    @Test fun `resend button locks for 60s then becomes active`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.startRegistration()
        vm.onNameNext("Юра")
        vm.onEmailNext("yura@example.com")
        vm.submitRegistration("SecurePass1")
        runCurrent()

        assertEquals(60, vm.registrationResendCountdown.value)
        advanceTimeBy(60_000)
        runCurrent()
        assertEquals(0, vm.registrationResendCountdown.value)
    }

    @Test fun `resendRegistrationVerification restarts cooldown`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.startRegistration()
        vm.onNameNext("Юра")
        vm.onEmailNext("yura@example.com")
        vm.submitRegistration("SecurePass1")
        advanceUntilIdle()
        advanceTimeBy(60_000)
        runCurrent()

        vm.resendRegistrationVerification()
        runCurrent()

        assertEquals(60, vm.registrationResendCountdown.value)
        assertEquals(1, repo.resendCount)
    }

    @Test fun `back from password step returns to email step`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.startRegistration()
        vm.onNameNext("Юра")
        vm.onEmailNext("yura@example.com")
        vm.navigateBackFromOnboarding()
        assertEquals(ViewState.ONBOARDING_EMAIL, vm.viewState.value)
    }

    @Test fun `back navigation keeps entered name and email`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.startRegistration()
        vm.onNameNext("Юра")
        vm.onEmailNext("yura@example.com")
        vm.navigateBackFromOnboarding()
        vm.navigateBackFromOnboarding()

        assertEquals(ViewState.ONBOARDING_NAME, vm.viewState.value)
        assertEquals("Юра", vm.registrationFirstName.value)
        assertEquals("yura@example.com", vm.registrationEmail.value)
    }

    @Test fun `typed email survives back and forward without pressing next`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.startRegistration()
        vm.onNameChange("Yura")
        vm.onNameNext(vm.registrationFirstName.value)
        vm.onEmailChange("yura.test@example.com")

        vm.navigateBackFromOnboarding()
        assertEquals("Yura", vm.registrationFirstName.value)
        vm.onNameNext(vm.registrationFirstName.value)

        assertEquals(ViewState.ONBOARDING_EMAIL, vm.viewState.value)
        assertEquals("yura.test@example.com", vm.registrationEmail.value)
    }

    @Test fun `repeated verifyEmail while first is in flight calls repository once`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.verifyEmail("123456")
        vm.verifyEmail("123456")
        advanceUntilIdle()

        assertEquals(1, repo.verifyEmailCount)
        assertFalse(vm.isRegistrationLoading.value)
    }

    @Test fun `clearOtpError removes inline error`() = runTest(dispatcher) {
        repo.verifyEmailError = "Неверный код"
        val vm = viewModel()
        vm.verifyEmail("000000")
        advanceUntilIdle()
        assertNotNull(vm.otpError.value)

        vm.clearOtpError()

        assertNull(vm.otpError.value)
    }

    @Test fun `startRegistration resets state on second call`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.startRegistration()
        vm.onNameNext("Юра")
        vm.onEmailNext("yura@example.com")

        vm.startRegistration()

        assertEquals(ViewState.ONBOARDING_NAME, vm.viewState.value)
        assertEquals("", vm.registrationEmail.value)
        assertNull(vm.otpError.value)
    }

    @Test fun `skipOtpVerification opens session without triggering re-registration`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.startRegistration()
        vm.onNameNext("Юра")
        vm.onEmailNext("yura@example.com")
        vm.submitRegistration("SecurePass1")
        advanceUntilIdle()
        assertEquals(ViewState.ONBOARDING_OTP, vm.viewState.value)

        val registerCallsBefore = repo.registerCallCount

        vm.skipOtpVerification()

        assertTrue(vm.isSessionOpen.value)
        assertEquals(
            registerCallsBefore,
            repo.registerCallCount,
            "skipOtpVerification must not trigger re-registration",
        )
    }
}

private class FakeOnboardingRepository : AuthorizationRepository {
    var registerError: String? = null
    var verifyEmailError: String? = null
    var resendCount = 0
    var registerCallCount = 0
    var verifyEmailCount = 0

    override suspend fun loginUser(username: String, password: String): String? = null

    override suspend fun registerUser(
        username: String,
        email: String,
        password: String,
        gender: Gender,
    ): String? {
        registerCallCount++
        return registerError
    }

    override suspend fun requestPasswordReset(email: String): String? = null

    override suspend fun resetPassword(email: String, code: String, newPassword: String): String? = null

    override suspend fun logout() = Unit

    override suspend fun verifyEmail(code: String): String? {
        verifyEmailCount++
        return verifyEmailError
    }

    override suspend fun resendVerification(): String? {
        resendCount++
        return null
    }
}
