package com.ownstd.project.authorization.internal.data.repository

import com.ownstd.project.authorization.internal.domain.AuthService
import com.ownstd.project.authorization.internal.domain.AuthorizationRepository
import com.ownstd.project.authorization.internal.presentation.Gender
import com.ownstd.project.storage.TokenStorage
import io.ktor.util.rootCause

class AuthorizationRepositoryImpl(
    private val authService: AuthService,
    private val tokenStorage: TokenStorage
) : AuthorizationRepository {
    override suspend fun loginUser(username: String, password: String): Boolean {
        var result = false
        authService.login(username, password)
            .onSuccess {
                tokenStorage.saveToken(it)
                result = true
                println("Login successful")
            }
            .onFailure { exception ->
                println("Login failed: ${exception.message}")
                tokenStorage.clearToken()
                result = false
            }
        return result
    }

    override suspend fun registerUser(
        username: String,
        email: String,
        password: String,
        gender: Gender
    ): Boolean {
        var result = false
        authService.register(username, email, password, gender.name)
            .onSuccess {
                result = true
                println("Registration successful")
            }
            .onFailure { exception ->
                println("Register failed: ${exception.message}")
                result = false
            }
        return result
    }

    override fun logout() {
        tokenStorage.clearToken()
        println("Logout successful - token cleared")
    }
}