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

internal const val PASSWORD_CHANGED_NOTICE = "Пароль изменён. Войдите с новым паролем"

internal class AuthorizationViewModel(
    private val authorizationRepository: AuthorizationRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    val viewState = MutableStateFlow(ViewState.LOGIN)
    val errorState = MutableStateFlow<String?>(null)
    val isSessionOpen = MutableStateFlow(false)
    val forgotPasswordEmail = MutableStateFlow("")
    val isForgotPasswordLoading = MutableStateFlow(false)
    val isLoginLoading = MutableStateFlow(false)

    /** Shown on the login screen right after a successful password reset; dropped by the next login attempt. */
    val passwordResetNotice = MutableStateFlow<String?>(null)

    /** Email the login form starts with after a password reset. */
    val loginPrefillEmail = MutableStateFlow("")

    private val _resendCountdown = MutableStateFlow(0)
    val resendCountdown: StateFlow<Int> = _resendCountdown.asStateFlow()

    private var resendTimerJob: Job? = null

    private val _isResetCodeResent = MutableStateFlow(false)

    /** True once a new reset code was sent from the code screen, so the previous email no longer works. */
    val isResetCodeResent: StateFlow<Boolean> = _isResetCodeResent.asStateFlow()

    val registrationFirstName = MutableStateFlow("")
    val registrationEmail = MutableStateFlow("")
    val isRegistrationLoading = MutableStateFlow(false)

    private val _otpError = MutableStateFlow<String?>(null)
    val otpError: StateFlow<String?> = _otpError.asStateFlow()

    private val _isEmailVerified = MutableStateFlow(false)
    val isEmailVerified: StateFlow<Boolean> = _isEmailVerified.asStateFlow()

    private val _registrationResendCountdown = MutableStateFlow(0)
    val registrationResendCountdown: StateFlow<Int> = _registrationResendCountdown.asStateFlow()

    private var registrationResendTimerJob: Job? = null

    // Registration data lives in memory only: no account or session exists until /register succeeds.
    private var verificationToken: String? = null
    private var codeSentToEmail: String? = null

    // The flag is raised before launching so a double tap cannot send a second login request.
    fun loginUser(username: String, password: String) {
        if (isLoginLoading.value) return
        isLoginLoading.value = true
        errorState.value = null
        passwordResetNotice.value = null
        viewModelScope.launch(dispatcher) {
            try {
                val error = authorizationRepository.loginUser(username, password)
                if (error != null) {
                    errorState.value = error
                } else {
                    isSessionOpen.value = true
                }
            } finally {
                isLoginLoading.value = false
            }
        }
    }

    fun requestPasswordReset(email: String) {
        val trimmedEmail = email.trim()
        launchPasswordRecoveryRequest {
            val error = authorizationRepository.requestPasswordReset(trimmedEmail)
            if (error != null) {
                errorState.value = error
            } else {
                forgotPasswordEmail.value = trimmedEmail
                _isResetCodeResent.value = false
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
        launchPasswordRecoveryRequest {
            val error = authorizationRepository.requestPasswordReset(email)
            if (error != null) {
                errorState.value = error
            } else {
                _isResetCodeResent.value = true
                startResendTimer()
            }
        }
    }

    fun resetPassword(email: String, code: String, newPassword: String) {
        launchPasswordRecoveryRequest {
            val error = authorizationRepository.resetPassword(email, code, newPassword)
            if (error != null) {
                errorState.value = error
            } else {
                finishPasswordReset(email)
            }
        }
    }

    // The session stays closed: the user signs in with the new password. The recovery screens are left for good.
    private fun finishPasswordReset(email: String) {
        resendTimerJob?.cancel()
        _resendCountdown.value = 0
        _isResetCodeResent.value = false
        forgotPasswordEmail.value = ""
        errorState.value = null
        loginPrefillEmail.value = email
        passwordResetNotice.value = PASSWORD_CHANGED_NOTICE
        viewState.value = ViewState.LOGIN
    }

    // The flag is raised before launching: every extra /forgot-password call silently invalidates
    // the code already sent, so a double tap must not reach the server twice.
    private fun launchPasswordRecoveryRequest(request: suspend () -> Unit) {
        if (isForgotPasswordLoading.value) return
        isForgotPasswordLoading.value = true
        errorState.value = null
        viewModelScope.launch(dispatcher) {
            try {
                request()
            } finally {
                isForgotPasswordLoading.value = false
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
        passwordResetNotice.value = null
        registrationFirstName.value = ""
        registrationEmail.value = ""
        _otpError.value = null
        verificationToken = null
        codeSentToEmail = null
        _isEmailVerified.value = false
        registrationResendTimerJob?.cancel()
        _registrationResendCountdown.value = 0
        viewState.value = ViewState.ONBOARDING_NAME
    }

    fun onNameChange(firstName: String) {
        registrationFirstName.value = firstName
    }

    fun onEmailChange(email: String) {
        registrationEmail.value = email
    }

    fun onNameNext(firstName: String) {
        registrationFirstName.value = firstName
        errorState.value = null
        viewState.value = ViewState.ONBOARDING_EMAIL
    }

    fun onEmailNext(email: String) {
        if (isRegistrationLoading.value) return
        registrationEmail.value = email
        errorState.value = null
        // A code for this email is already on its way (user stepped back from OTP): the server would answer 429.
        if (email == codeSentToEmail) {
            viewState.value = ViewState.ONBOARDING_OTP
            return
        }
        isRegistrationLoading.value = true
        viewModelScope.launch(dispatcher) {
            val error = authorizationRepository.requestRegistrationCode(email)
            isRegistrationLoading.value = false
            if (error != null) {
                errorState.value = error
                return@launch
            }
            codeSentToEmail = email
            verificationToken = null
            _isEmailVerified.value = false
            startRegistrationResendTimer()
            viewState.value = ViewState.ONBOARDING_OTP
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
                authorizationRepository.verifyRegistrationCode(registrationEmail.value, code)
                    .onSuccess { token ->
                        verificationToken = token
                        _isEmailVerified.value = true
                        // The server deletes the code on success, so a later "Next" must request a fresh one.
                        codeSentToEmail = null
                        viewState.value = ViewState.ONBOARDING_PASSWORD
                    }
                    .onFailure { exception ->
                        _otpError.value = exception.message ?: "Не удалось подтвердить email. Попробуй снова"
                    }
            } finally {
                isRegistrationLoading.value = false
            }
        }
    }

    fun continueToPassword() {
        if (verificationToken == null) return
        viewState.value = ViewState.ONBOARDING_PASSWORD
    }

    fun resendRegistrationVerification() {
        if (isRegistrationLoading.value) return
        isRegistrationLoading.value = true
        errorState.value = null
        viewModelScope.launch(dispatcher) {
            val error = authorizationRepository.requestRegistrationCode(registrationEmail.value)
            isRegistrationLoading.value = false
            if (error != null) {
                errorState.value = error
            } else {
                startRegistrationResendTimer()
            }
        }
    }

    fun submitRegistration(password: String) {
        if (isRegistrationLoading.value) return
        val token = verificationToken
        if (token == null) {
            errorState.value = "Сначала подтверди email"
            return
        }
        isRegistrationLoading.value = true
        errorState.value = null
        viewModelScope.launch(dispatcher) {
            val error = authorizationRepository.registerUser(
                username = null,
                email = registrationEmail.value,
                password = password,
                gender = Gender.OTHER,
                verificationToken = token
            )
            isRegistrationLoading.value = false
            if (error != null) {
                errorState.value = error
            } else {
                isSessionOpen.value = true
            }
        }
    }

    fun navigateBackFromOnboarding() {
        errorState.value = null
        _otpError.value = null
        viewState.value = when (viewState.value) {
            ViewState.ONBOARDING_EMAIL -> ViewState.ONBOARDING_NAME
            ViewState.ONBOARDING_OTP -> ViewState.ONBOARDING_EMAIL
            ViewState.ONBOARDING_PASSWORD -> ViewState.ONBOARDING_OTP
            else -> ViewState.LOGIN
        }
    }

    private fun startRegistrationResendTimer() {
        registrationResendTimerJob?.cancel()
        registrationResendTimerJob = startResendTimerFor(_registrationResendCountdown)
    }
}
