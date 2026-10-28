package com.ownstd.project.authorization.internal.data.repository

import com.ownstd.project.authorization.internal.domain.AuthService
import com.ownstd.project.authorization.internal.domain.EMAIL_ALREADY_REGISTERED_MESSAGE
import com.ownstd.project.authorization.internal.domain.INVALID_EMAIL_MESSAGE
import com.ownstd.project.authorization.internal.domain.INVALID_OR_EXPIRED_CODE_MESSAGE
import com.ownstd.project.authorization.internal.domain.NETWORK_ERROR_MESSAGE
import com.ownstd.project.authorization.internal.domain.REGISTRATION_REJECTED_MESSAGE
import com.ownstd.project.authorization.internal.domain.RESEND_RATE_LIMIT_MESSAGE
import com.ownstd.project.authorization.internal.domain.RESET_CODE_EXPIRED_MESSAGE
import com.ownstd.project.authorization.internal.domain.RESET_INVALID_CODE_MESSAGE
import com.ownstd.project.authorization.internal.domain.RESET_INVALID_PASSWORD_MESSAGE
import com.ownstd.project.authorization.internal.domain.SERVER_ERROR_MESSAGE
import com.ownstd.project.authorization.internal.domain.AuthorizationRepository
import com.ownstd.project.authorization.internal.presentation.Gender
import com.ownstd.project.authorization.internal.presentation.PASSWORD_RULE_ERROR
import com.ownstd.project.storage.TokenStorage

class AuthorizationRepositoryImpl(
    private val authService: AuthService,
    private val tokenStorage: TokenStorage
) : AuthorizationRepository {
    override suspend fun loginUser(username: String, password: String): String? {
        var errorMessage: String? = null
        authService.login(username, password)
            .onSuccess { tokenResponse ->
                tokenStorage.saveSession(
                    accessToken = tokenResponse.accessToken,
                    refreshToken = tokenResponse.refreshToken,
                    expiresAt = tokenResponse.expiresAt
                )
                println("[AuthRepository] login success, userId=${tokenResponse.userId}")
            }
            .onFailure { exception ->
                println("[AuthRepository] login failed: ${exception.message}")
                tokenStorage.clearSession()
                errorMessage = mapErrorMessage(exception.message)
            }
        return errorMessage
    }

    override suspend fun registerUser(
        username: String?,
        email: String,
        password: String,
        gender: Gender,
        verificationToken: String
    ): String? {
        var errorMessage: String? = null
        authService.register(username, email, password, gender.name, verificationToken)
            .onSuccess { tokenResponse ->
                tokenStorage.saveSession(
                    accessToken = tokenResponse.accessToken,
                    refreshToken = tokenResponse.refreshToken,
                    expiresAt = tokenResponse.expiresAt
                )
                println("[AuthRepository] register success, userId=${tokenResponse.userId}")
            }
            .onFailure { exception ->
                println("[AuthRepository] register failed: ${exception.message}")
                errorMessage = mapErrorMessage(exception.message)
            }
        return errorMessage
    }

    override suspend fun requestPasswordReset(email: String): String? {
        return authService.forgotPassword(email)
            .fold(
                onSuccess = { null },
                onFailure = { exception ->
                    when (exception.message) {
                        RESEND_RATE_LIMIT_MESSAGE -> RESEND_RATE_LIMIT_MESSAGE
                        NETWORK_ERROR_MESSAGE -> "Не удалось отправить письмо. Проверь интернет-соединение"
                        else -> "Не удалось отправить письмо. Попробуй позже"
                    }
                }
            )
    }

    override suspend fun resetPassword(email: String, code: String, newPassword: String): String? {
        return authService.resetPassword(email, code, newPassword)
            .fold(
                onSuccess = { tokenResponse ->
                    tokenStorage.saveSession(
                        accessToken = tokenResponse.accessToken,
                        refreshToken = tokenResponse.refreshToken,
                        expiresAt = tokenResponse.expiresAt
                    )
                    null
                },
                onFailure = { exception -> resetPasswordErrorMessage(exception.message) }
            )
    }

    override suspend fun logout() {
        val refreshToken = tokenStorage.getRefreshToken()
        if (refreshToken != null) {
            authService.logout(refreshToken)
                .onSuccess { println("[AuthRepository] server logout success") }
                .onFailure { e -> println("[AuthRepository] server logout failed (best-effort): ${e.message}") }
        }
        tokenStorage.clearSession()
        println("[AuthRepository] logout - session cleared")
    }

    override suspend fun verifyEmail(code: String): String? {
        return authService.verifyEmail(code)
            .fold(
                onSuccess = { null },
                onFailure = { exception ->
                    if (exception.message == INVALID_OR_EXPIRED_CODE_MESSAGE) {
                        "Неверный или истёкший код. Запроси новый"
                    } else {
                        "Не удалось подтвердить email. Попробуй снова"
                    }
                }
            )
    }

    override suspend fun resendVerification(): String? {
        return authService.resendVerification()
            .fold(
                onSuccess = { null },
                onFailure = { exception ->
                    when {
                        exception.message == RESEND_RATE_LIMIT_MESSAGE -> RESEND_RATE_LIMIT_MESSAGE
                        else -> "Не удалось отправить код. Проверь интернет-соединение"
                    }
                }
            )
    }

    override suspend fun requestRegistrationCode(email: String): String? {
        return authService.requestRegistrationCode(email)
            .fold(
                onSuccess = { null },
                onFailure = { exception ->
                    when (exception.message) {
                        INVALID_EMAIL_MESSAGE -> "Некорректный email"
                        EMAIL_ALREADY_REGISTERED_MESSAGE -> "Пользователь с таким email уже существует"
                        RESEND_RATE_LIMIT_MESSAGE -> RESEND_RATE_LIMIT_MESSAGE
                        else -> "Не удалось отправить код. Проверь интернет-соединение"
                    }
                }
            )
    }

    override suspend fun verifyRegistrationCode(email: String, code: String): Result<String> {
        return authService.verifyRegistrationCode(email, code)
            .fold(
                onSuccess = { verificationToken -> Result.success(verificationToken) },
                onFailure = { exception ->
                    val message = if (exception.message == INVALID_OR_EXPIRED_CODE_MESSAGE) {
                        "Неверный или истёкший код. Запроси новый"
                    } else {
                        "Не удалось подтвердить email. Попробуй снова"
                    }
                    Result.failure(Exception(message))
                }
            )
    }

    private fun resetPasswordErrorMessage(message: String?): String = when (message) {
        RESET_INVALID_CODE_MESSAGE -> "Неверный код. Действует только код из последнего письма"
        RESET_CODE_EXPIRED_MESSAGE -> "Код истёк или уже использован. Запроси новый"
        RESET_INVALID_PASSWORD_MESSAGE -> "Пароль не подходит. $PASSWORD_RULE_ERROR"
        RESEND_RATE_LIMIT_MESSAGE -> RESEND_RATE_LIMIT_MESSAGE
        SERVER_ERROR_MESSAGE -> "Сервер временно недоступен. Попробуй позже"
        NETWORK_ERROR_MESSAGE -> "Нет связи с сервером. Проверь интернет-соединение"
        else -> "Не удалось сменить пароль. Проверь код из последнего письма или запроси новый"
    }

    private fun mapErrorMessage(message: String?): String {
        return when {
            message == null -> "Неизвестная ошибка"
            message.contains("already exists", ignoreCase = true) -> "Пользователь с таким именем или email уже существует"
            message.contains("Invalid credentials", ignoreCase = true) -> "Неверное имя пользователя или пароль"
            message.contains(REGISTRATION_REJECTED_MESSAGE) ->
                "Не удалось создать аккаунт. Проверь пароль или вернись назад и запроси код заново"
            message.contains("Unexpected response", ignoreCase = true) -> "Ошибка сервера: $message"
            else -> "Не удалось подключиться к серверу"
        }
    }
}
