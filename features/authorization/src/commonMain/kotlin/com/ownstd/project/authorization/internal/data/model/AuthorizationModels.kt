package com.ownstd.project.authorization.internal.data.model

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val username: String? = null,
    val email: String,
    val password: String,
    val gender: String,
    val verificationToken: String
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
    val userId: Long,
)

@Serializable
data class LogoutRequest(
    val refreshToken: String
)

@Serializable
data class ForgotPasswordRequest(val email: String)

@Serializable
data class ForgotPasswordResponse(val message: String)

@Serializable
data class ResetPasswordRequest(
    val email: String,
    val code: String,
    val newPassword: String
)

@Serializable
data class VerifyEmailRequest(val code: String)

@Serializable
data class StartRegistrationRequest(val email: String)

@Serializable
data class VerifyRegistrationRequest(val email: String, val code: String)

@Serializable
data class VerifyRegistrationResponse(val verificationToken: String)

/** Error body of the backend; [error] is a stable machine-readable code. */
@Serializable
data class ErrorResponse(val error: String, val message: String = "")
