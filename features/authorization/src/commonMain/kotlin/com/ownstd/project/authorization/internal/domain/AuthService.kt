package com.ownstd.project.authorization.internal.domain

import com.ownstd.project.authorization.internal.data.model.AuthTokenResponse

interface AuthService {
    suspend fun register(
        username: String,
        email: String,
        password: String,
        gender: String
    ): Result<AuthTokenResponse>

    suspend fun login(
        username: String,
        password: String
    ): Result<AuthTokenResponse>
}
