package com.ownstd.project.authorization.internal.data.repository

import com.ownstd.project.authorization.internal.domain.AuthService
import com.ownstd.project.authorization.internal.domain.AuthorizationRepository
import com.ownstd.project.authorization.internal.presentation.Gender
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
        username: String,
        email: String,
        password: String,
        gender: Gender
    ): String? {
        var errorMessage: String? = null
        authService.register(username, email, password, gender.name)
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
                onFailure = { "Не удалось отправить письмо. Проверь интернет-соединение" }
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
                onFailure = { exception ->
                    when {
                        exception.message?.contains("Неверный код") == true -> "Неверный код или email"
                        else -> "Не удалось сбросить пароль. Проверь код и попробуй снова"
                    }
                }
            )
    }

    override fun loginByTelegram(accessToken: String, refreshToken: String, expiresAt: Long) {
        tokenStorage.saveSession(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresAt = expiresAt
        )
        println("[AuthRepository] telegram login success")
    }

    override fun logout() {
        tokenStorage.clearSession()
        println("[AuthRepository] logout - session cleared")
    }

    private fun mapErrorMessage(message: String?): String {
        return when {
            message == null -> "Неизвестная ошибка"
            message.contains("already exists", ignoreCase = true) -> "Пользователь с таким именем или email уже существует"
            message.contains("Invalid credentials", ignoreCase = true) -> "Неверное имя пользователя или пароль"
            message.contains("Unexpected response", ignoreCase = true) -> "Ошибка сервера: $message"
            else -> "Не удалось подключиться к серверу"
        }
    }
}
