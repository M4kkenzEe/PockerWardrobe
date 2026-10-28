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

    private fun AuthorizationViewModel.toEmailStep() {
        startRegistration()
        onNameNext("Юра")
    }

    private fun AuthorizationViewModel.toOtpStep() {
        toEmailStep()
        onEmailNext("yura@example.com")
    }

    private fun AuthorizationViewModel.toPasswordStep() {
        toOtpStep()
        dispatcher.scheduler.advanceUntilIdle()
        verifyEmail("123456")
        dispatcher.scheduler.advanceUntilIdle()
    }

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

    // ---- email step ----

    @Test fun `email step requests a code for that email and moves to OTP without creating an account`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.toOtpStep()
        advanceUntilIdle()

        assertEquals(ViewState.ONBOARDING_OTP, vm.viewState.value)
        assertEquals(listOf("yura@example.com"), repo.requestedCodeEmails)
        assertEquals(0, repo.registerCallCount, "no account may be created before the final step")
        assertFalse(vm.isSessionOpen.value)
        assertNull(vm.errorState.value)
        assertFalse(vm.isRegistrationLoading.value)
    }

    @Test fun `email step starts the 60 second resend timer`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.toOtpStep()
        runCurrent()

        assertEquals(60, vm.registrationResendCountdown.value)
    }

    @Test fun `email already taken shows the error on the email step`() = runTest(dispatcher) {
        repo.requestCodeError = "Пользователь с таким email уже существует"
        val vm = viewModel()
        vm.toOtpStep()
        advanceUntilIdle()

        assertEquals("Пользователь с таким email уже существует", vm.errorState.value)
        assertEquals(ViewState.ONBOARDING_EMAIL, vm.viewState.value)
        assertFalse(vm.isRegistrationLoading.value)
        assertEquals(0, vm.registrationResendCountdown.value)
    }

    @Test fun `double tap on next sends one request`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.toEmailStep()

        vm.onEmailNext("yura@example.com")
        vm.onEmailNext("yura@example.com")
        advanceUntilIdle()

        assertEquals(1, repo.requestedCodeEmails.size)
    }

    @Test fun `going back to email and forward with the same email does not request another code`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.toOtpStep()
        advanceUntilIdle()

        vm.navigateBackFromOnboarding()
        vm.onEmailNext("yura@example.com")

        assertEquals(ViewState.ONBOARDING_OTP, vm.viewState.value)
        assertEquals(1, repo.requestedCodeEmails.size)
    }

    @Test fun `going back to email and forward with a different email requests a code for it`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.toOtpStep()
        advanceUntilIdle()

        vm.navigateBackFromOnboarding()
        vm.onEmailNext("other@example.com")
        advanceUntilIdle()

        assertEquals(listOf("yura@example.com", "other@example.com"), repo.requestedCodeEmails)
        assertEquals(ViewState.ONBOARDING_OTP, vm.viewState.value)
    }

    // ---- OTP step ----

    @Test fun `right code moves to password step without opening a session`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.toPasswordStep()

        assertEquals(ViewState.ONBOARDING_PASSWORD, vm.viewState.value)
        assertEquals("yura@example.com", repo.lastVerifiedEmail)
        assertNull(vm.otpError.value)
        assertFalse(vm.isSessionOpen.value)
        assertEquals(0, repo.registerCallCount, "account is created only after the password is set")
    }

    @Test fun `verified email is flagged and stays flagged when stepping back from password`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.toOtpStep()
        dispatcher.scheduler.advanceUntilIdle()
        assertFalse(vm.isEmailVerified.value)

        vm.verifyEmail("123456")
        dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.isEmailVerified.value)

        vm.navigateBackFromOnboarding()
        assertEquals(ViewState.ONBOARDING_OTP, vm.viewState.value)
        assertTrue(vm.isEmailVerified.value)
    }

    @Test fun `continue after verification goes to password step without a new code`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.toPasswordStep()
        vm.navigateBackFromOnboarding()
        val callsBefore = repo.networkCalls

        vm.continueToPassword()

        assertEquals(ViewState.ONBOARDING_PASSWORD, vm.viewState.value)
        assertEquals(callsBefore, repo.networkCalls)
    }

    @Test fun `continue before verification does nothing`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.toOtpStep()
        dispatcher.scheduler.advanceUntilIdle()

        vm.continueToPassword()

        assertEquals(ViewState.ONBOARDING_OTP, vm.viewState.value)
    }

    @Test fun `next with the same email after verification requests a fresh code`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.toPasswordStep()
        vm.navigateBackFromOnboarding()
        vm.navigateBackFromOnboarding()
        assertEquals(ViewState.ONBOARDING_EMAIL, vm.viewState.value)

        vm.onEmailNext("yura@example.com")
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf("yura@example.com", "yura@example.com"), repo.requestedCodeEmails)
        assertEquals(ViewState.ONBOARDING_OTP, vm.viewState.value)
        assertFalse(vm.isEmailVerified.value, "a new code means the email must be verified again")
    }

    @Test fun `wrong code shows inline error and stays on OTP step`() = runTest(dispatcher) {
        repo.verifyError = "Неверный код"
        val vm = viewModel()
        vm.toOtpStep()
        advanceUntilIdle()

        vm.verifyEmail("000000")
        advanceUntilIdle()

        assertEquals("Неверный код", vm.otpError.value)
        assertEquals(ViewState.ONBOARDING_OTP, vm.viewState.value)
        assertFalse(vm.isSessionOpen.value)
    }

    @Test fun `repeated verifyEmail while first is in flight calls repository once`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.verifyEmail("123456")
        vm.verifyEmail("123456")
        advanceUntilIdle()

        assertEquals(1, repo.verifyCount)
        assertFalse(vm.isRegistrationLoading.value)
    }

    @Test fun `clearOtpError removes inline error`() = runTest(dispatcher) {
        repo.verifyError = "Неверный код"
        val vm = viewModel()
        vm.verifyEmail("000000")
        advanceUntilIdle()
        assertNotNull(vm.otpError.value)

        vm.clearOtpError()

        assertNull(vm.otpError.value)
    }

    @Test fun `resend button locks for 60s then becomes active`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.toOtpStep()
        runCurrent()

        assertEquals(60, vm.registrationResendCountdown.value)
        advanceTimeBy(60_000)
        runCurrent()
        assertEquals(0, vm.registrationResendCountdown.value)
    }

    @Test fun `resend requests the code again for the same email and restarts cooldown`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.toOtpStep()
        advanceUntilIdle()
        advanceTimeBy(60_000)
        runCurrent()

        vm.resendRegistrationVerification()
        runCurrent()

        assertEquals(60, vm.registrationResendCountdown.value)
        assertEquals(listOf("yura@example.com", "yura@example.com"), repo.requestedCodeEmails)
    }

    // ---- password step ----

    @Test fun `submitting the password registers with the verification token and opens the session`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.toPasswordStep()

        vm.submitRegistration("SecurePass1")
        advanceUntilIdle()

        val call = assertNotNull(repo.lastRegister)
        assertEquals("verification-token", call.verificationToken)
        assertEquals("yura@example.com", call.email)
        assertEquals("SecurePass1", call.password)
        assertNull(call.username, "first name must not be sent as username")
        assertTrue(vm.isSessionOpen.value)
        assertNull(vm.errorState.value)
    }

    @Test fun `registration failure shows error and stays on password step without a session`() = runTest(dispatcher) {
        repo.registerError = "Подтверждение email устарело"
        val vm = viewModel()
        vm.toPasswordStep()

        vm.submitRegistration("SecurePass1")
        advanceUntilIdle()

        assertEquals("Подтверждение email устарело", vm.errorState.value)
        assertEquals(ViewState.ONBOARDING_PASSWORD, vm.viewState.value)
        assertFalse(vm.isSessionOpen.value)
        assertFalse(vm.isRegistrationLoading.value)
    }

    @Test fun `double tap on create account sends one request`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.toPasswordStep()

        vm.submitRegistration("SecurePass1")
        vm.submitRegistration("SecurePass1")
        advanceUntilIdle()

        assertEquals(1, repo.registerCallCount)
    }

    @Test fun `submitting before the email is verified does not call the server`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.toOtpStep()
        advanceUntilIdle()

        vm.submitRegistration("SecurePass1")
        advanceUntilIdle()

        assertEquals(0, repo.registerCallCount)
        assertNotNull(vm.errorState.value)
        assertFalse(vm.isSessionOpen.value)
    }

    // ---- back navigation ----

    @Test fun `back from OTP returns to email step without touching the network`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.toOtpStep()
        advanceUntilIdle()
        val callsBefore = repo.networkCalls

        vm.navigateBackFromOnboarding()
        advanceUntilIdle()

        assertEquals(ViewState.ONBOARDING_EMAIL, vm.viewState.value)
        assertEquals(callsBefore, repo.networkCalls)
        assertEquals(0, repo.logoutCount)
    }

    @Test fun `back from password step returns to OTP step without touching the network`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.toPasswordStep()
        val callsBefore = repo.networkCalls

        vm.navigateBackFromOnboarding()
        advanceUntilIdle()

        assertEquals(ViewState.ONBOARDING_OTP, vm.viewState.value)
        assertEquals(callsBefore, repo.networkCalls)
    }

    @Test fun `back from email step returns to name step and back from name step to login`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.toEmailStep()

        vm.navigateBackFromOnboarding()
        assertEquals(ViewState.ONBOARDING_NAME, vm.viewState.value)

        vm.navigateBackFromOnboarding()
        assertEquals(ViewState.LOGIN, vm.viewState.value)
    }

    @Test fun `back navigation keeps entered name and email`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.toOtpStep()
        advanceUntilIdle()

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

    @Test fun `startRegistration resets state on second call`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.toPasswordStep()

        vm.startRegistration()

        assertEquals(ViewState.ONBOARDING_NAME, vm.viewState.value)
        assertEquals("", vm.registrationEmail.value)
        assertNull(vm.otpError.value)
        vm.submitRegistration("SecurePass1")
        advanceUntilIdle()
        assertEquals(0, repo.registerCallCount, "verification token must not survive a restarted registration")
        assertFalse(vm.isEmailVerified.value)
    }
}

private data class RegisterCall(
    val username: String?,
    val email: String,
    val password: String,
    val verificationToken: String,
)

private class FakeOnboardingRepository : AuthorizationRepository {
    var requestCodeError: String? = null
    var verifyError: String? = null
    var registerError: String? = null

    val requestedCodeEmails = mutableListOf<String>()
    var lastVerifiedEmail: String? = null
    var verifyCount = 0
    var registerCallCount = 0
    var lastRegister: RegisterCall? = null
    var logoutCount = 0

    val networkCalls: Int
        get() = requestedCodeEmails.size + verifyCount + registerCallCount + logoutCount

    override suspend fun requestRegistrationCode(email: String): String? {
        requestedCodeEmails += email
        return requestCodeError
    }

    override suspend fun verifyRegistrationCode(email: String, code: String): Result<String> {
        verifyCount++
        lastVerifiedEmail = email
        return verifyError?.let { Result.failure(Exception(it)) } ?: Result.success("verification-token")
    }

    override suspend fun registerUser(
        username: String?,
        email: String,
        password: String,
        gender: Gender,
        verificationToken: String,
    ): String? {
        registerCallCount++
        lastRegister = RegisterCall(username, email, password, verificationToken)
        return registerError
    }

    override suspend fun loginUser(username: String, password: String): String? = null

    override suspend fun requestPasswordReset(email: String): String? = null

    override suspend fun resetPassword(email: String, code: String, newPassword: String): String? = null

    override suspend fun logout() {
        logoutCount++
    }

    override suspend fun verifyEmail(code: String): String? = null

    override suspend fun resendVerification(): String? = null
}
