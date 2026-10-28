package com.ownstd.project.network.internal

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NetworkClientAuthCacheTest {

    private val storage = InMemoryTokenStorage(accessToken = null, refreshToken = null)
    private val sessionManager = CountingSessionManager()

    private val engine = MockEngine { request ->
        val path = request.url.encodedPath
        when {
            path.endsWith("/refresh") -> respond("", HttpStatusCode.Unauthorized)
            path.endsWith("/data") && request.headers[HttpHeaders.Authorization] == "Bearer fresh-access" ->
                respond("ok", HttpStatusCode.OK)
            else -> respond("", HttpStatusCode.Unauthorized, headersOf(HttpHeaders.WWWAuthenticate, "Bearer realm=\"test\""))
        }
    }

    private val client = NetworkClient.getClient(storage, BASE_URL, sessionManager, engine)

    @Test fun `wrong password sends exactly one login request`() = runTest {
        val response = client.post("${BASE_URL}login")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals(1, requestsTo("/login"))
        assertEquals(0, requestsTo("/refresh"))
        assertEquals(0, sessionManager.expiredEvents)
    }

    @Test fun `a 401 from any pre-session endpoint is not retried`() = runTest {
        val paths = listOf(
            "register", "auth/registration/start", "auth/registration/verify",
            "auth/forgot-password", "auth/reset-password"
        )
        paths.forEach { path ->
            client.post("$BASE_URL$path")

            assertEquals(1, requestsTo("/$path"), path)
        }
        assertEquals(0, requestsTo("/refresh"))
        assertEquals(0, sessionManager.expiredEvents)
    }

    @Test fun `a query string or trailing slash does not bring the retry back`() = runTest {
        client.post("${BASE_URL}login?device=android")
        client.post("${BASE_URL}login/")

        assertEquals(2, requestsTo("/login") + requestsTo("/login/"))
        assertEquals(0, requestsTo("/refresh"))
    }

    @Test fun `a protected route that only ends like a pre-session one still gets the bearer and the retry`() = runTest {
        storage.saveSession("fresh-access", "fresh-refresh", 1L)

        client.get("${BASE_URL}events/register")

        val sent = engine.requestHistory.filter { it.url.encodedPath.endsWith("/events/register") }
        assertEquals(listOf("Bearer fresh-access"), sent.map { it.headers[HttpHeaders.Authorization] }.distinct())
        assertEquals(1, requestsTo("/refresh"), "401 on a protected route must go through the refresh")
    }

    @Test fun `pre-session endpoints are sent without the bearer header even when a session exists`() = runTest {
        storage.saveSession("stale-access", "stale-refresh", 1L)

        client.post("${BASE_URL}login")

        assertNull(engine.requestHistory.single().headers[HttpHeaders.Authorization])
    }

    @Test fun `first protected request after login carries the new access token and needs no refresh`() = runTest {
        client.get("${BASE_URL}data") // before login: caches "no tokens" inside the Auth plugin
        storage.saveSession("fresh-access", "fresh-refresh", 1L)
        client.authProvider<BearerAuthProvider>()?.clearToken() // what NetworkRepository.clearAuthCache() does
        val mark = engine.requestHistory.size

        val response = client.get("${BASE_URL}data")

        assertEquals(HttpStatusCode.OK, response.status)
        val sent = engine.requestHistory.drop(mark)
        assertEquals(listOf("Bearer fresh-access"), sent.map { it.headers[HttpHeaders.Authorization] })
        assertEquals(0, requestsTo("/refresh"))
    }

    @Test fun `without dropping the cache the plugin keeps using the tokens it loaded before login`() = runTest {
        client.get("${BASE_URL}data")
        storage.saveSession("fresh-access", "fresh-refresh", 1L)
        val mark = engine.requestHistory.size

        client.get("${BASE_URL}data")

        assertNull(engine.requestHistory[mark].headers[HttpHeaders.Authorization])
    }

    @Test fun `after logout a protected request does not go out with the old token`() = runTest {
        storage.saveSession("fresh-access", "fresh-refresh", 1L)
        assertEquals(HttpStatusCode.OK, client.get("${BASE_URL}data").status)

        storage.clearSession()
        client.authProvider<BearerAuthProvider>()?.clearToken()
        val mark = engine.requestHistory.size
        client.get("${BASE_URL}data")

        assertNull(engine.requestHistory[mark].headers[HttpHeaders.Authorization])
    }

    private fun requestsTo(path: String) = engine.requestHistory.count { it.url.encodedPath.endsWith(path) }

    private companion object {
        const val BASE_URL = "http://localhost/api/v1/"
    }
}
