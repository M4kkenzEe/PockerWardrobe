package com.ownstd.project.authorization.internal.domain

import com.ownstd.project.authorization.internal.presentation.Gender

interface AuthorizationRepository {
    suspend fun loginUser(username: String, password: String): String?

    /** Creates the account and saves the session; the only registration call that touches token storage. */
    suspend fun registerUser(
        username: String?,
        email: String,
        password: String,
        gender: Gender,
        verificationToken: String
    ): String?
    suspend fun requestPasswordReset(email: String): String?
    suspend fun resetPassword(email: String, code: String, newPassword: String): String?
    suspend fun logout()
    suspend fun verifyEmail(code: String): String?
    suspend fun resendVerification(): String?

    /** Sends the registration OTP to [email]; no account exists yet. Returns a user-facing error or null. */
    suspend fun requestRegistrationCode(email: String): String?

    /** Succeeds with the verification token for [email]; a failure message is user-facing. */
    suspend fun verifyRegistrationCode(email: String, code: String): Result<String>
}
