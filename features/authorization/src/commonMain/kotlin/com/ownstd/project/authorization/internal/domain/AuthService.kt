package com.ownstd.project.authorization.internal.domain

import com.ownstd.project.authorization.internal.data.model.AuthTokenResponse
import com.ownstd.project.authorization.internal.data.model.ForgotPasswordResponse
import com.ownstd.project.authorization.internal.data.model.TelegramInitResponse
import com.ownstd.project.authorization.internal.data.model.TelegramStatusResponse

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

    suspend fun forgotPassword(email: String): Result<ForgotPasswordResponse>
    suspend fun resetPassword(email: String, code: String, newPassword: String): Result<AuthTokenResponse>

    suspend fun getTelegramInit(): Result<TelegramInitResponse>
    suspend fun getTelegramStatus(state: String): Result<TelegramStatusResponse>
}
