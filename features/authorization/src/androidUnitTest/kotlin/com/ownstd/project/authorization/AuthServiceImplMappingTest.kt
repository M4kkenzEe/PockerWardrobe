package com.ownstd.project.authorization

import com.ownstd.project.authorization.internal.data.repository.AuthServiceImpl
import com.ownstd.project.authorization.internal.domain.INVALID_OR_EXPIRED_CODE_MESSAGE
import com.ownstd.project.authorization.internal.domain.RESEND_RATE_LIMIT_MESSAGE
import com.ownstd.project.network.api.NetworkRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpStatusCode
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

    private fun service(status: HttpStatusCode): AuthServiceImpl {
        val client = HttpClient(MockEngine { respond(content = "", status = status) }) {
            install(ContentNegotiation) { json() }
        }
        return AuthServiceImpl(object : NetworkRepository {
            override val baseUrl = "http://localhost/"
            override fun getClient() = client
            override fun clearAuthCache() = Unit
        })
    }
}
