package com.ownstd.project.authorization.internal.domain

import com.ownstd.project.authorization.internal.presentation.Gender

interface AuthorizationRepository {
    suspend fun loginUser(username: String, password: String): String?
    suspend fun registerUser(
        username: String,
        email: String,
        password: String,
        gender: Gender
    ): String?
    suspend fun requestPasswordReset(email: String): String?
    suspend fun resetPassword(email: String, code: String, newPassword: String): String?
    fun loginByTelegram(accessToken: String, refreshToken: String, expiresAt: Long)
    fun logout()
}
