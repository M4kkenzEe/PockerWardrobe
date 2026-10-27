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
    val forgotPasswordEmail = MutableStateFlow("")
    val isForgotPasswordLoading = MutableStateFlow(false)

    private val _resendCountdown = MutableStateFlow(0)
    val resendCountdown: StateFlow<Int> = _resendCountdown.asStateFlow()

    private var resendTimerJob: Job? = null

    val registrationFirstName = MutableStateFlow("")
    val registrationEmail = MutableStateFlow("")
    val isRegistrationLoading = MutableStateFlow(false)

    private val _otpError = MutableStateFlow<String?>(null)
    val otpError: StateFlow<String?> = _otpError.asStateFlow()

    private val _registrationResendCountdown = MutableStateFlow(0)
    val registrationResendCountdown: StateFlow<Int> = _registrationResendCountdown.asStateFlow()

    private var registrationResendTimerJob: Job? = null

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

    private fun startResendTimerFor(target: MutableStateFlow<Int>): Job {
        return viewModelScope.launch {
            var seconds = RESEND_COOLDOWN_SECONDS
            while (seconds > 0) {
                target.value = seconds
                delay(1_000L)
                seconds--
            }
            target.value = 0
        }
    }

    private fun startResendTimer() {
        resendTimerJob?.cancel()
        resendTimerJob = startResendTimerFor(_resendCountdown)
    }

    fun startRegistration() {
        errorState.value = null
        registrationFirstName.value = ""
        registrationEmail.value = ""
        _otpError.value = null
        viewState.value = ViewState.ONBOARDING_NAME
    }

    fun onNameNext(firstName: String) {
        registrationFirstName.value = firstName
        errorState.value = null
        viewState.value = ViewState.ONBOARDING_EMAIL
    }

    fun onEmailNext(email: String) {
        registrationEmail.value = email
        errorState.value = null
        viewState.value = ViewState.ONBOARDING_PASSWORD
    }

    fun submitRegistration(password: String) {
        viewModelScope.launch(dispatcher) {
            isRegistrationLoading.value = true
            errorState.value = null
            val error = authorizationRepository.registerUser(
                username = registrationFirstName.value,
                email = registrationEmail.value,
                password = password,
                gender = Gender.OTHER
            )
            isRegistrationLoading.value = false
            if (error != null) {
                errorState.value = error
            } else {
                startRegistrationResendTimer()
                viewState.value = ViewState.ONBOARDING_OTP
            }
        }
    }

    fun clearOtpError() {
        _otpError.value = null
    }

    fun verifyEmail(code: String) {
        if (isRegistrationLoading.value) return
        isRegistrationLoading.value = true
        viewModelScope.launch(dispatcher) {
            try {
                _otpError.value = null
                val error = authorizationRepository.verifyEmail(code)
                if (error != null) {
                    _otpError.value = error
                } else {
                    isSessionOpen.value = true
                }
            } finally {
                isRegistrationLoading.value = false
            }
        }
    }

    fun resendRegistrationVerification() {
        viewModelScope.launch(dispatcher) {
            isRegistrationLoading.value = true
            val error = authorizationRepository.resendVerification()
            isRegistrationLoading.value = false
            if (error != null) {
                errorState.value = error
            } else {
                startRegistrationResendTimer()
            }
        }
    }

    fun skipOtpVerification() {
        isSessionOpen.value = true
    }

    fun navigateBackFromOnboarding() {
        errorState.value = null
        _otpError.value = null
        viewState.value = when (viewState.value) {
            ViewState.ONBOARDING_EMAIL -> ViewState.ONBOARDING_NAME
            ViewState.ONBOARDING_PASSWORD -> ViewState.ONBOARDING_EMAIL
            ViewState.ONBOARDING_OTP -> ViewState.LOGIN
            else -> ViewState.LOGIN
        }
    }

    private fun startRegistrationResendTimer() {
        registrationResendTimerJob?.cancel()
        registrationResendTimerJob = startResendTimerFor(_registrationResendCountdown)
    }
}
