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
