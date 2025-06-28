package com.ownstd.project.network

import com.ownstd.project.network.model.User

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