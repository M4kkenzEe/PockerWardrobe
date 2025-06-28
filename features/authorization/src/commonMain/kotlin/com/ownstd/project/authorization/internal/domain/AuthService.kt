package com.ownstd.project.authorization.internal.domain

import com.ownstd.project.authorization.internal.data.model.User

interface AuthService {
    suspend fun register(
        username: String,
        email: String,
        password: String,
        gender: String
    ): Result<User>

    suspend fun login(
        username: String,
        password: String
    ): Result<String> // Возвращает JWT-токен
}