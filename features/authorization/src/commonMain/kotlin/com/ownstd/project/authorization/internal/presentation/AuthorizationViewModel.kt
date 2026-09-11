package com.ownstd.project.authorization.internal.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ownstd.project.authorization.internal.domain.AuthorizationRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val RESEND_COOLDOWN_SECONDS = 60

internal class AuthorizationViewModel(
    private val authorizationRepository: AuthorizationRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    val viewState = MutableStateFlow(ViewState.LOGIN)
    val errorState = MutableStateFlow<String?>(null)
    val isSessionOpen = MutableStateFlow(false)
    val prefillEmail = MutableStateFlow("")
    val prefillPassword = MutableStateFlow("")
    val forgotPasswordEmail = MutableStateFlow("")
    val isForgotPasswordLoading = MutableStateFlow(false)

    private val _resendCountdown = MutableStateFlow(0)

    /** Seconds remaining on resend cooldown. 0 = button is active. */
    val resendCountdown: StateFlow<Int> = _resendCountdown.asStateFlow()

    private var resendTimerJob: Job? = null

    fun loginUser(username: String, password: String) {
        viewModelScope.launch(dispatcher) {
            val result = authorizationRepository.loginUser(username, password)
            if (result != null) {
                errorState.value = result
            } else {
                isSessionOpen.value = true
            }
        }
    }

    fun registerUser(username: String, email: String, password: String, gender: Gender) {
        viewModelScope.launch(dispatcher) {
            val result = authorizationRepository.registerUser(username, email, password, gender)
            if (result != null) {
                errorState.value = result
            } else {
                prefillEmail.value = email
                prefillPassword.value = password
                errorState.value = null
                viewState.value = ViewState.LOGIN
            }
        }
    }

    fun requestPasswordReset(email: String) {
        viewModelScope.launch(dispatcher) {
            isForgotPasswordLoading.value = true
            errorState.value = null
            val error = authorizationRepository.requestPasswordReset(email)
            isForgotPasswordLoading.value = false
            if (error != null) {
                errorState.value = error
            } else {
                forgotPasswordEmail.value = email
                startResendTimer()
                viewState.value = ViewState.RESET_PASSWORD
            }
        }
    }

    /**
     * Resends the OTP code without navigating away from the ResetPassword screen.
     * Restarts the 60-second cooldown timer.
     */
    fun resendPasswordResetCode(email: String) {
        viewModelScope.launch(dispatcher) {
            isForgotPasswordLoading.value = true
            errorState.value = null
            val error = authorizationRepository.requestPasswordReset(email)
            isForgotPasswordLoading.value = false
            if (error != null) {
                errorState.value = error
            } else {
                startResendTimer()
            }
        }
    }

    fun resetPassword(email: String, code: String, newPassword: String) {
        viewModelScope.launch(dispatcher) {
            isForgotPasswordLoading.value = true
            errorState.value = null
            val error = authorizationRepository.resetPassword(email, code, newPassword)
            isForgotPasswordLoading.value = false
            if (error != null) {
                errorState.value = error
            } else {
                isSessionOpen.value = true
            }
        }
    }

    private fun startResendTimer() {
        resendTimerJob?.cancel()
        resendTimerJob = viewModelScope.launch {
            var seconds = RESEND_COOLDOWN_SECONDS
            while (seconds > 0) {
                _resendCountdown.value = seconds
                delay(1_000L)
                seconds--
            }
            _resendCountdown.value = 0
        }
    }
}
