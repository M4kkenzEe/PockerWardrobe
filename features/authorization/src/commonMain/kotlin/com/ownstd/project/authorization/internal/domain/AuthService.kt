package com.ownstd.project.authorization.internal.domain

import com.ownstd.project.authorization.internal.data.model.AuthTokenResponse
import com.ownstd.project.authorization.internal.data.model.ForgotPasswordResponse

internal const val INVALID_OR_EXPIRED_CODE_MESSAGE = "Invalid or expired code"

internal const val RESEND_RATE_LIMIT_MESSAGE = "Слишком много запросов. Подождите немного"

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
    suspend fun logout(refreshToken: String): Result<Unit>

    suspend fun verifyEmail(code: String): Result<Unit>
    suspend fun resendVerification(): Result<Unit>
}
