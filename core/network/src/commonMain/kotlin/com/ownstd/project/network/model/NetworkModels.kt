package com.ownstd.project.network.model

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val gender: String
)

@Serializable
data class User(
    val userId: Long,
    val username: String,
    val email: String,
    val passwordHash: String,
    val gender: String
)

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val token: String
)