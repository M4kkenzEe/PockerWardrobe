package com.ownstd.project.authorization

import com.ownstd.project.authorization.internal.domain.AuthorizationRepository
import com.ownstd.project.authorization.internal.presentation.Gender
import com.ownstd.project.network.api.NetworkRepository
import com.ownstd.project.storage.TokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.delay

/** Shared by the login tests and the UI tests that follow them. */
internal class InMemoryTokenStorage(
    private var accessToken: String? = null,
    private var refreshToken: String? = null
) : TokenStorage {
    private var expiresAt: Long? = null
    var saveSessionCalls = 0
    var clearSessionCalls = 0

    override fun saveSession(accessToken: String, refreshToken: String, expiresAt: Long) {
        saveSessionCalls++
        this.accessToken = accessToken
        this.refreshToken = refreshToken
        this.expiresAt = expiresAt
    }

    override fun getAccessToken() = accessToken
    override fun getRefreshToken() = refreshToken
    override fun getExpiresAt() = expiresAt

    override fun clearSession() {
        clearSessionCalls++
        accessToken = null
        refreshToken = null
        expiresAt = null
    }
}

/** Login answer and latency are controlled by the test; every other call succeeds. */
internal class ControllableAuthorizationRepository : AuthorizationRepository {
    var loginError: String? = null
    var loginDelayMs = 0L
    var loginCalls = 0
    var lastLogin: Pair<String, String>? = null

    override suspend fun loginUser(username: String, password: String): String? {
        loginCalls++
        lastLogin = username to password
        if (loginDelayMs > 0) delay(loginDelayMs)
        return loginError
    }

    override suspend fun registerUser(
        username: String?,
        email: String,
        password: String,
        gender: Gender,
        verificationToken: String
    ): String? = null

    override suspend fun requestPasswordReset(email: String): String? = null
    override suspend fun resetPassword(email: String, code: String, newPassword: String): String? = null
    override suspend fun logout() = Unit
    override suspend fun verifyEmail(code: String): String? = null
    override suspend fun resendVerification(): String? = null
    override suspend fun requestRegistrationCode(email: String): String? = null
    override suspend fun verifyRegistrationCode(email: String, code: String): Result<String> = Result.success("token")
}

/** Counts how often the HTTP client's cached tokens were dropped. */
internal class RecordingNetworkRepository(private val client: HttpClient? = null) : NetworkRepository {
    var authCacheClears = 0
    override val baseUrl = "http://localhost/"
    override fun getClient() = client ?: error("this test does not use the HTTP client")
    override fun clearAuthCache() { authCacheClears++ }
}

internal fun networkRepositoryOver(engine: MockEngine): RecordingNetworkRepository =
    RecordingNetworkRepository(HttpClient(engine) { install(ContentNegotiation) { json() } })
