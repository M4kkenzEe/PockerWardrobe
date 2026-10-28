package com.ownstd.project.authorization.internal.domain

import com.ownstd.project.authorization.internal.data.model.AuthTokenResponse
import com.ownstd.project.authorization.internal.data.model.ForgotPasswordResponse

internal const val INVALID_OR_EXPIRED_CODE_MESSAGE = "Invalid or expired code"

internal const val RESEND_RATE_LIMIT_MESSAGE = "Слишком много запросов. Подождите немного"

internal const val EMAIL_ALREADY_REGISTERED_MESSAGE = "Email already registered"

internal const val INVALID_EMAIL_MESSAGE = "Invalid email"

internal const val REGISTRATION_REJECTED_MESSAGE = "Registration rejected"

internal const val RESET_INVALID_CODE_MESSAGE = "Reset code invalid"

internal const val RESET_CODE_EXPIRED_MESSAGE = "Reset code expired"

internal const val RESET_INVALID_PASSWORD_MESSAGE = "New password rejected"

/** 400 with an error code this app version does not know, e.g. from an older backend. */
internal const val RESET_REJECTED_MESSAGE = "Password reset rejected"

internal const val INVALID_CREDENTIALS_MESSAGE = "Invalid credentials"

internal const val USER_ALREADY_EXISTS_MESSAGE = "User with this username or email already exists"

internal const val BAD_REQUEST_MESSAGE = "Bad request"

internal const val SERVER_ERROR_MESSAGE = "Server error"

internal const val NETWORK_ERROR_MESSAGE = "Network error"

interface AuthService {
    suspend fun register(
        username: String?,
        email: String,
        password: String,
        gender: String,
        verificationToken: String
    ): Result<AuthTokenResponse>

    suspend fun login(
        username: String,
        password: String
    ): Result<AuthTokenResponse>

    suspend fun forgotPassword(email: String): Result<ForgotPasswordResponse>
    /** Succeeds on 200 OK. The password is changed but no session is opened: the user logs in again. */
    suspend fun resetPassword(email: String, code: String, newPassword: String): Result<Unit>
    suspend fun logout(refreshToken: String): Result<Unit>

    suspend fun verifyEmail(code: String): Result<Unit>
    suspend fun resendVerification(): Result<Unit>

    suspend fun requestRegistrationCode(email: String): Result<Unit>

    /** Succeeds with the verification token that /register requires. */
    suspend fun verifyRegistrationCode(email: String, code: String): Result<String>
}
