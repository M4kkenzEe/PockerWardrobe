package com.ownstd.project.authorization

import com.ownstd.project.authorization.internal.data.model.AuthTokenResponse
import com.ownstd.project.authorization.internal.data.model.ForgotPasswordResponse
import com.ownstd.project.authorization.internal.data.repository.AuthorizationRepositoryImpl
import com.ownstd.project.authorization.internal.domain.AuthService
import com.ownstd.project.authorization.internal.domain.INVALID_OR_EXPIRED_CODE_MESSAGE
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class VerifyEmailErrorMappingTest {

    @Test fun `successful verification returns no error`() = runTest {
        assertNull(repository(Result.success(Unit)).verifyEmail("123456"))
    }

    @Test fun `invalid or expired code shows invalid or expired message`() = runTest {
        val error = repository(Result.failure(Exception(INVALID_OR_EXPIRED_CODE_MESSAGE))).verifyEmail("123456")
        assertEquals("Неверный или истёкший код. Запроси новый", error)
    }

    @Test fun `other failures show generic message`() = runTest {
        val error = repository(Result.failure(Exception("timeout"))).verifyEmail("123456")
        assertEquals("Не удалось подтвердить email. Попробуй снова", error)
    }

    private fun repository(verifyResult: Result<Unit>) =
        AuthorizationRepositoryImpl(FakeAuthService(verifyResult), InMemoryTokenStorage(), RecordingNetworkRepository())
}

private class FakeAuthService(private val verifyResult: Result<Unit>) : AuthService {
    override suspend fun verifyEmail(code: String): Result<Unit> = verifyResult
    override suspend fun register(username: String?, email: String, password: String, gender: String, verificationToken: String): Result<AuthTokenResponse> = unsupported()
    override suspend fun login(username: String, password: String): Result<AuthTokenResponse> = unsupported()
    override suspend fun forgotPassword(email: String): Result<ForgotPasswordResponse> = unsupported()
    override suspend fun resetPassword(email: String, code: String, newPassword: String): Result<Unit> = unsupported()
    override suspend fun logout(refreshToken: String): Result<Unit> = unsupported()
    override suspend fun resendVerification(): Result<Unit> = unsupported()
    override suspend fun requestRegistrationCode(email: String): Result<Unit> = unsupported()
    override suspend fun verifyRegistrationCode(email: String, code: String): Result<String> = unsupported()

    private fun unsupported(): Nothing = error("not used in this test")
}
