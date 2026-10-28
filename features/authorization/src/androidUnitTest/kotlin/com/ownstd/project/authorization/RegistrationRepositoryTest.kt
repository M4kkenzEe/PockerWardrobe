package com.ownstd.project.authorization

import com.ownstd.project.authorization.internal.data.model.AuthTokenResponse
import com.ownstd.project.authorization.internal.data.model.ForgotPasswordResponse
import com.ownstd.project.authorization.internal.data.repository.AuthorizationRepositoryImpl
import com.ownstd.project.authorization.internal.domain.AuthService
import com.ownstd.project.authorization.internal.domain.EMAIL_ALREADY_REGISTERED_MESSAGE
import com.ownstd.project.authorization.internal.domain.INVALID_EMAIL_MESSAGE
import com.ownstd.project.authorization.internal.domain.INVALID_OR_EXPIRED_CODE_MESSAGE
import com.ownstd.project.authorization.internal.domain.REGISTRATION_REJECTED_MESSAGE
import com.ownstd.project.authorization.internal.domain.RESEND_RATE_LIMIT_MESSAGE
import com.ownstd.project.authorization.internal.presentation.Gender
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RegistrationRepositoryTest {

    private val storage = InMemoryTokenStorage()
    private val service = ScriptedAuthService()
    private val repository = AuthorizationRepositoryImpl(service, storage, RecordingNetworkRepository())

    @Test fun `requesting a code does not save a session`() = runTest {
        assertNull(repository.requestRegistrationCode("a@b.com"))

        assertEquals(0, storage.saveSessionCalls)
    }

    @Test fun `verifying a code returns the token and does not save a session`() = runTest {
        service.verifyResult = Result.success("verification-token")

        val result = repository.verifyRegistrationCode("a@b.com", "123456")

        assertEquals("verification-token", result.getOrNull())
        assertEquals(0, storage.saveSessionCalls)
    }

    @Test fun `failed registration saves no session`() = runTest {
        service.registerResult = Result.failure(Exception(REGISTRATION_REJECTED_MESSAGE))

        val error = repository.registerUser(null, "a@b.com", "SecurePass1", Gender.OTHER, "expired-token")

        assertEquals("Не удалось создать аккаунт. Проверь пароль или вернись назад и запроси код заново", error)
        assertEquals(0, storage.saveSessionCalls)
    }

    @Test fun `successful registration saves the session exactly once`() = runTest {
        val error = repository.registerUser(null, "a@b.com", "SecurePass1", Gender.OTHER, "verification-token")

        assertNull(error)
        assertEquals(1, storage.saveSessionCalls)
        assertEquals("access", storage.getAccessToken())
        assertEquals("verification-token", service.lastRegisterToken)
        assertNull(service.lastRegisterUsername, "first name must not be sent as username")
    }

    @Test fun `already registered email on code request shows a clear message`() = runTest {
        service.requestResult = Result.failure(Exception(EMAIL_ALREADY_REGISTERED_MESSAGE))

        assertEquals("Пользователь с таким email уже существует", repository.requestRegistrationCode("a@b.com"))
    }

    @Test fun `rejected email on code request is not blamed on the connection`() = runTest {
        service.requestResult = Result.failure(Exception(INVALID_EMAIL_MESSAGE))

        assertEquals("Некорректный email", repository.requestRegistrationCode("a@b"))
    }

    @Test fun `rate limit on code request is passed through`() = runTest {
        service.requestResult = Result.failure(Exception(RESEND_RATE_LIMIT_MESSAGE))

        assertEquals(RESEND_RATE_LIMIT_MESSAGE, repository.requestRegistrationCode("a@b.com"))
    }

    @Test fun `wrong code shows invalid or expired message`() = runTest {
        service.verifyResult = Result.failure(Exception(INVALID_OR_EXPIRED_CODE_MESSAGE))

        val result = repository.verifyRegistrationCode("a@b.com", "000000")

        assertEquals("Неверный или истёкший код. Запроси новый", result.exceptionOrNull()?.message)
        assertTrue(result.isFailure)
    }
}

private class ScriptedAuthService : AuthService {
    var requestResult: Result<Unit> = Result.success(Unit)
    var verifyResult: Result<String> = Result.success("token")
    var registerResult: Result<AuthTokenResponse> =
        Result.success(AuthTokenResponse(accessToken = "access", refreshToken = "refresh", expiresAt = 1L, userId = 1L))
    var lastRegisterToken: String? = null
    var lastRegisterUsername: String? = "unset"

    override suspend fun requestRegistrationCode(email: String) = requestResult
    override suspend fun verifyRegistrationCode(email: String, code: String) = verifyResult

    override suspend fun register(
        username: String?,
        email: String,
        password: String,
        gender: String,
        verificationToken: String
    ): Result<AuthTokenResponse> {
        lastRegisterUsername = username
        lastRegisterToken = verificationToken
        return registerResult
    }

    override suspend fun login(username: String, password: String): Result<AuthTokenResponse> = unsupported()
    override suspend fun forgotPassword(email: String): Result<ForgotPasswordResponse> = unsupported()
    override suspend fun resetPassword(email: String, code: String, newPassword: String): Result<AuthTokenResponse> = unsupported()
    override suspend fun logout(refreshToken: String): Result<Unit> = unsupported()
    override suspend fun verifyEmail(code: String): Result<Unit> = unsupported()
    override suspend fun resendVerification(): Result<Unit> = unsupported()

    private fun unsupported(): Nothing = error("not used in this test")
}
