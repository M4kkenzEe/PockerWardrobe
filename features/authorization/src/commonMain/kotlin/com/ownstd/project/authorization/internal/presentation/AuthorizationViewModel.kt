package com.ownstd.project.authorization.internal.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ownstd.project.authorization.internal.domain.AuthService
import com.ownstd.project.authorization.internal.domain.AuthorizationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TELEGRAM_POLL_INTERVAL_MS = 2_000L
private const val TELEGRAM_POLL_TIMEOUT_MS = 10 * 60 * 1_000L

internal class AuthorizationViewModel(
    private val authorizationRepository: AuthorizationRepository,
    private val authService: AuthService
) : ViewModel() {
    val viewState = MutableStateFlow(ViewState.LOGIN)
    val errorState = MutableStateFlow<String?>(null)
    val isSessionOpen = MutableStateFlow(false)
    val prefillEmail = MutableStateFlow("")
    val prefillPassword = MutableStateFlow("")
    val isTelegramLoading = MutableStateFlow(false)
    val forgotPasswordEmail = MutableStateFlow("")
    val isForgotPasswordLoading = MutableStateFlow(false)

    private val _openUrlEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val openUrlEvent: SharedFlow<String> = _openUrlEvent

    fun loginUser(username: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = authorizationRepository.loginUser(username, password)
            if (result != null) {
                errorState.value = result
            } else {
                isSessionOpen.value = true
            }
        }
    }

    fun registerUser(username: String, email: String, password: String, gender: Gender) {
        viewModelScope.launch(Dispatchers.IO) {
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
        viewModelScope.launch(Dispatchers.IO) {
            isForgotPasswordLoading.value = true
            errorState.value = null
            val error = authorizationRepository.requestPasswordReset(email)
            isForgotPasswordLoading.value = false
            if (error != null) {
                errorState.value = error
            } else {
                forgotPasswordEmail.value = email
                viewState.value = ViewState.RESET_PASSWORD
            }
        }
    }

    fun resetPassword(email: String, code: String, newPassword: String) {
        viewModelScope.launch(Dispatchers.IO) {
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

    fun startTelegramAuth() {
        if (isTelegramLoading.value) return
        viewModelScope.launch(Dispatchers.IO) {
            isTelegramLoading.value = true
            errorState.value = null

            val initResult = authService.getTelegramInit()
            if (initResult.isFailure) {
                errorState.value = "Не удалось запустить Telegram авторизацию"
                isTelegramLoading.value = false
                return@launch
            }

            val init = initResult.getOrThrow()
            _openUrlEvent.emit(init.botUrl)

            val maxPolls = (TELEGRAM_POLL_TIMEOUT_MS / TELEGRAM_POLL_INTERVAL_MS).toInt()
            var pollCount = 0
            while (isActive && pollCount < maxPolls) {
                pollCount++
                delay(TELEGRAM_POLL_INTERVAL_MS)

                val statusResult = authService.getTelegramStatus(init.stateToken)
                if (statusResult.isFailure) continue

                val status = statusResult.getOrThrow()
                when (status.status) {
                    "done" -> {
                        withContext(Dispatchers.Main) {
                            authorizationRepository.loginByTelegram(
                                accessToken = status.accessToken!!,
                                refreshToken = status.refreshToken!!,
                                expiresAt = status.expiresAt!!
                            )
                            isTelegramLoading.value = false
                            isSessionOpen.value = true
                        }
                        return@launch
                    }
                    "expired" -> {
                        errorState.value = "Время вышло, попробуй снова"
                        isTelegramLoading.value = false
                        return@launch
                    }
                    // "pending" — continue polling
                }
            }
            // loop exhausted without done/expired → timeout
            errorState.value = "Время вышло, попробуй снова"
            isTelegramLoading.value = false
        }
    }
}