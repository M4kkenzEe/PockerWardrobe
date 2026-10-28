package com.ownstd.project.network.internal

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class NetworkClientRefreshTest {

    private val storage = InMemoryTokenStorage(accessToken = "old-access", refreshToken = "old-refresh")
    private val sessionManager = CountingSessionManager()
    private val refreshCalls = AtomicInteger()
    private val refreshedRefreshTokens = mutableListOf<String>()

    private var refreshAnswer: MockRequestHandleScope.() -> HttpResponseData = { respondRefresh(HttpStatusCode.OK) }

    @Test fun `refresh 200 saves the new tokens and retries the request`() = runTest {
        val response = client().get("${BASE_URL}data")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("new-access", storage.getAccessToken())
        assertEquals("new-refresh", storage.getRefreshToken())
        assertEquals(0, storage.clearCalls)
        assertEquals(0, sessionManager.expiredEvents)
    }

    @Test fun `refresh 401 clears the session and notifies once`() = runTest {
        refreshAnswer = { respondRefresh(HttpStatusCode.Unauthorized) }

        val response = client().get("${BASE_URL}data")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertSessionCleared()
    }

    @Test fun `refresh 403 keeps the session`() = runTest {
        refreshAnswer = { respondRefresh(HttpStatusCode.Forbidden) }

        client().get("${BASE_URL}data")

        assertSessionKept()
    }

    @Test fun `refresh 400 keeps the session`() = runTest {
        refreshAnswer = { respondRefresh(HttpStatusCode.BadRequest) }

        client().get("${BASE_URL}data")

        assertSessionKept()
    }

    @Test fun `request after a failed refresh refreshes again and succeeds`() = runTest {
        val client = client()
        refreshAnswer = { respondRefresh(HttpStatusCode.ServiceUnavailable) }
        assertEquals(HttpStatusCode.Unauthorized, client.get("${BASE_URL}data").status)

        refreshAnswer = { respondRefresh(HttpStatusCode.OK) }
        val response = client.get("${BASE_URL}data")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(2, refreshCalls.get())
        assertEquals("new-access", storage.getAccessToken())
        assertEquals(0, storage.clearCalls)
        assertEquals(0, sessionManager.expiredEvents)
    }

    @Test fun `refresh 429 keeps the session`() = runTest {
        refreshAnswer = { respondRefresh(HttpStatusCode.TooManyRequests) }

        val response = client().get("${BASE_URL}data")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertSessionKept()
    }

    @Test fun `refresh 500 keeps the session`() = runTest {
        refreshAnswer = { respondRefresh(HttpStatusCode.InternalServerError) }

        client().get("${BASE_URL}data")

        assertSessionKept()
    }

    @Test fun `refresh 502 keeps the session`() = runTest {
        refreshAnswer = { respondRefresh(HttpStatusCode.BadGateway) }

        client().get("${BASE_URL}data")

        assertSessionKept()
    }

    @Test fun `refresh 503 keeps the session`() = runTest {
        refreshAnswer = { respondRefresh(HttpStatusCode.ServiceUnavailable) }

        client().get("${BASE_URL}data")

        assertSessionKept()
    }

    @Test fun `network failure during refresh keeps the session and does not throw`() = runTest {
        refreshAnswer = { throw IOException("no route to host") }

        val response = client().get("${BASE_URL}data")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertSessionKept()
    }

    @Test fun `refresh 200 with an unreadable body keeps the session`() = runTest {
        refreshAnswer = { respondRefresh(HttpStatusCode.OK, body = "not json") }

        client().get("${BASE_URL}data")

        assertSessionKept()
    }

    @Test fun `missing refresh token clears the session and notifies`() = runTest {
        storage.dropRefreshToken()

        val response = client().get("${BASE_URL}data")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals(0, refreshCalls.get())
        assertEquals(1, storage.clearCalls)
        assertEquals(1, sessionManager.expiredEvents)
    }

    @Test fun `parallel requests that all get 401 trigger a single refresh`() = runTest {
        val unauthorizedAnswers = AtomicInteger()
        val bothRejected = CompletableDeferred<Unit>()
        val client = client(onDataRejected = {
            if (unauthorizedAnswers.incrementAndGet() == 2) bothRejected.complete(Unit)
        }, beforeRefreshAnswer = { bothRejected.await() })

        val responses: List<HttpResponse> = listOf(
            async { client.get("${BASE_URL}data") },
            async { client.get("${BASE_URL}data") }
        ).awaitAll()

        assertEquals(listOf(HttpStatusCode.OK, HttpStatusCode.OK), responses.map { it.status })
        assertEquals(1, refreshCalls.get())
        assertEquals(listOf("old-refresh"), refreshedRefreshTokens)
        assertEquals(0, storage.clearCalls)
        assertEquals(0, sessionManager.expiredEvents)
    }

    private fun assertSessionCleared() {
        assertEquals(1, refreshCalls.get())
        assertEquals(1, storage.clearCalls)
        assertEquals(1, sessionManager.expiredEvents)
        assertNull(storage.getAccessToken())
        assertNull(storage.getRefreshToken())
    }

    private fun assertSessionKept() {
        assertEquals(1, refreshCalls.get())
        assertEquals(0, storage.clearCalls)
        assertEquals(0, sessionManager.expiredEvents)
        assertEquals("old-access", storage.getAccessToken())
        assertEquals("old-refresh", storage.getRefreshToken())
        assertNotNull(storage.getExpiresAt())
    }

    private fun client(
        onDataRejected: () -> Unit = {},
        beforeRefreshAnswer: suspend () -> Unit = {}
    ): HttpClient {
        val engine = MockEngine { request ->
            when {
                request.url.encodedPath.endsWith("/refresh") -> {
                    refreshCalls.incrementAndGet()
                    refreshedRefreshTokens += request.refreshTokenFromBody()
                    beforeRefreshAnswer()
                    refreshAnswer()
                }
                request.headers[HttpHeaders.Authorization] == "Bearer new-access" -> respond("ok", HttpStatusCode.OK)
                else -> {
                    onDataRejected()
                    respond("", HttpStatusCode.Unauthorized, headersOf(HttpHeaders.WWWAuthenticate, "Bearer realm=\"test\""))
                }
            }
        }
        return NetworkClient.getClient(storage, BASE_URL, sessionManager, engine)
    }

    private fun MockRequestHandleScope.respondRefresh(status: HttpStatusCode, body: String = REFRESH_BODY) =
        respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))

    private fun HttpRequestData.refreshTokenFromBody(): String {
        val json = (body as TextContent).text
        return Regex("\"refreshToken\"\\s*:\\s*\"([^\"]*)\"").find(json)!!.groupValues[1]
    }

    private companion object {
        const val BASE_URL = "http://localhost/api/v1/"
        const val EXPIRES_AT = 4_000_000_000_000L
        const val REFRESH_BODY = """{"accessToken":"new-access","refreshToken":"new-refresh","expiresAt":$EXPIRES_AT,"userId":1}"""
    }
}
