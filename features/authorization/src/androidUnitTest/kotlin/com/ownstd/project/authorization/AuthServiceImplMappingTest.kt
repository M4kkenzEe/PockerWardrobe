package com.ownstd.project.authorization

import com.ownstd.project.authorization.internal.data.repository.AuthServiceImpl
import com.ownstd.project.authorization.internal.domain.EMAIL_ALREADY_REGISTERED_MESSAGE
import com.ownstd.project.authorization.internal.domain.INVALID_EMAIL_MESSAGE
import com.ownstd.project.authorization.internal.domain.INVALID_OR_EXPIRED_CODE_MESSAGE
import com.ownstd.project.authorization.internal.domain.REGISTRATION_REJECTED_MESSAGE
import com.ownstd.project.authorization.internal.domain.RESEND_RATE_LIMIT_MESSAGE
import com.ownstd.project.network.api.NetworkRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthServiceImplMappingTest {

    @Test fun `verifyEmail 400 maps to invalid or expired code`() = runTest {
        val result = service(HttpStatusCode.BadRequest).verifyEmail("123456")
        assertEquals(INVALID_OR_EXPIRED_CODE_MESSAGE, result.exceptionOrNull()?.message)
    }

    @Test fun `verifyEmail 410 maps to invalid or expired code`() = runTest {
        val result = service(HttpStatusCode.Gone).verifyEmail("123456")
        assertEquals(INVALID_OR_EXPIRED_CODE_MESSAGE, result.exceptionOrNull()?.message)
    }

    @Test fun `verifyEmail 200 succeeds`() = runTest {
        assertTrue(service(HttpStatusCode.OK).verifyEmail("123456").isSuccess)
    }

    @Test fun `resendVerification 429 maps to rate limit message`() = runTest {
        val result = service(HttpStatusCode.TooManyRequests).resendVerification()
        assertEquals(RESEND_RATE_LIMIT_MESSAGE, result.exceptionOrNull()?.message)
    }

    @Test fun `requestRegistrationCode 200 succeeds`() = runTest {
        assertTrue(service(HttpStatusCode.OK).requestRegistrationCode("a@b.com").isSuccess)
    }

    @Test fun `requestRegistrationCode 400 maps to invalid email`() = runTest {
        val result = service(HttpStatusCode.BadRequest).requestRegistrationCode("a@b")
        assertEquals(INVALID_EMAIL_MESSAGE, result.exceptionOrNull()?.message)
    }

    @Test fun `requestRegistrationCode 409 maps to email already registered`() = runTest {
        val result = service(HttpStatusCode.Conflict).requestRegistrationCode("a@b.com")
        assertEquals(EMAIL_ALREADY_REGISTERED_MESSAGE, result.exceptionOrNull()?.message)
    }

    @Test fun `requestRegistrationCode 429 maps to rate limit message`() = runTest {
        val result = service(HttpStatusCode.TooManyRequests).requestRegistrationCode("a@b.com")
        assertEquals(RESEND_RATE_LIMIT_MESSAGE, result.exceptionOrNull()?.message)
    }

    @Test fun `verifyRegistrationCode 200 returns the verification token`() = runTest {
        val result = service(HttpStatusCode.OK, """{"verificationToken":"tok-123"}""")
            .verifyRegistrationCode("a@b.com", "123456")
        assertEquals("tok-123", result.getOrNull())
    }

    @Test fun `verifyRegistrationCode 400 maps to invalid or expired code`() = runTest {
        val result = service(HttpStatusCode.BadRequest).verifyRegistrationCode("a@b.com", "000000")
        assertEquals(INVALID_OR_EXPIRED_CODE_MESSAGE, result.exceptionOrNull()?.message)
    }

    @Test fun `register 400 maps to registration rejected`() = runTest {
        val result = service(HttpStatusCode.BadRequest)
            .register(null, "a@b.com", "SecurePass1", "OTHER", "expired-token")
        assertEquals(REGISTRATION_REJECTED_MESSAGE, result.exceptionOrNull()?.message)
    }

    private fun service(status: HttpStatusCode, body: String = ""): AuthServiceImpl {
        val client = HttpClient(MockEngine {
            respond(content = body, status = status, headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }) {
            install(ContentNegotiation) { json() }
        }
        return AuthServiceImpl(object : NetworkRepository {
            override val baseUrl = "http://localhost/"
            override fun getClient() = client
            override fun clearAuthCache() = Unit
        })
    }
}
