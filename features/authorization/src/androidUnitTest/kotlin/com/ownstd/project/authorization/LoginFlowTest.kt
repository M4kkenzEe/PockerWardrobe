package com.ownstd.project.authorization

import com.ownstd.project.authorization.internal.presentation.AuthorizationViewModel
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LoginFlowTest {
    private val dispatcher = StandardTestDispatcher()
    private val repo = ControllableAuthorizationRepository()

    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    private fun viewModel() = AuthorizationViewModel(repo, dispatcher)

    @Test fun `successful login opens the session`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.loginUser("user@example.com", "SecurePass1")
        advanceUntilIdle()

        assertTrue(vm.isSessionOpen.value)
        assertNull(vm.errorState.value)
        assertEquals("user@example.com" to "SecurePass1", repo.lastLogin)
    }

    @Test fun `failed login shows the error and opens no session`() = runTest(dispatcher) {
        repo.loginError = "Неверное имя пользователя или пароль"
        val vm = viewModel()

        vm.loginUser("user@example.com", "wrong")
        advanceUntilIdle()

        assertEquals("Неверное имя пользователя или пароль", vm.errorState.value)
        assertFalse(vm.isSessionOpen.value)
    }

    @Test fun `loading flag is up while the request is in flight and down after`() = runTest(dispatcher) {
        repo.loginDelayMs = 1_000
        val vm = viewModel()
        assertFalse(vm.isLoginLoading.value)

        vm.loginUser("user@example.com", "SecurePass1")
        runCurrent()
        assertTrue(vm.isLoginLoading.value)

        advanceTimeBy(1_001)
        runCurrent()
        assertFalse(vm.isLoginLoading.value)
    }

    @Test fun `loading flag goes down after a failed login`() = runTest(dispatcher) {
        repo.loginError = "Нет подключения к интернету"
        val vm = viewModel()

        vm.loginUser("user@example.com", "SecurePass1")
        advanceUntilIdle()

        assertFalse(vm.isLoginLoading.value)
    }

    @Test fun `second login call while the first is in flight sends no second request`() = runTest(dispatcher) {
        repo.loginDelayMs = 1_000
        val vm = viewModel()

        vm.loginUser("user@example.com", "SecurePass1")
        vm.loginUser("user@example.com", "SecurePass1")
        advanceUntilIdle()

        assertEquals(1, repo.loginCalls)
    }

    @Test fun `login can be retried after the first attempt finished`() = runTest(dispatcher) {
        repo.loginError = "Неверное имя пользователя или пароль"
        val vm = viewModel()
        vm.loginUser("user@example.com", "wrong")
        advanceUntilIdle()

        repo.loginError = null
        vm.loginUser("user@example.com", "SecurePass1")
        advanceUntilIdle()

        assertEquals(2, repo.loginCalls)
        assertNull(vm.errorState.value, "the previous error must not outlive a new attempt")
        assertTrue(vm.isSessionOpen.value)
    }
}
