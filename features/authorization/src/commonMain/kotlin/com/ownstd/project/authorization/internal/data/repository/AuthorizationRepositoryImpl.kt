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
            .onSuccess {
                tokenStorage.saveToken(it)
                println("Login successful")
            }
            .onFailure { exception ->
                println("Login failed: ${exception.message}")
                tokenStorage.clearToken()
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
            .onSuccess {
                println("Registration successful")
            }
            .onFailure { exception ->
                println("Register failed: ${exception.message}")
                errorMessage = mapErrorMessage(exception.message)
            }
        return errorMessage
    }

    override fun logout() {
        tokenStorage.clearToken()
        println("Logout successful - token cleared")
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