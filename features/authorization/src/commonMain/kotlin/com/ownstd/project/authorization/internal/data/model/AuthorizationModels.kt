package com.ownstd.project.authorization.internal.data.model

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val gender: String
)

@Serializable
data class LoginRequest(
    val login: String,
    val password: String
)

@Serializable
data class AuthTokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Long,
    val userId: Long
)

@Serializable
data class LogoutRequest(
    val refreshToken: String
)

@Serializable
data class TelegramInitResponse(
    val stateToken: String,
    val botUrl: String
)

@Serializable
data class TelegramStatusResponse(
    val status: String,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val expiresAt: Long? = null,
    val userId: Int? = null
)
