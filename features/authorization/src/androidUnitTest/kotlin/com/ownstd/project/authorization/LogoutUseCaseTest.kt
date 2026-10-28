package com.ownstd.project.authorization

import com.ownstd.project.authorization.internal.domain.AuthorizationRepository
import com.ownstd.project.authorization.internal.domain.LogoutUseCase
import com.ownstd.project.authorization.internal.presentation.Gender
import com.ownstd.project.network.api.NetworkRepository
import io.ktor.client.HttpClient
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

// ---------------------------------------------------------------------------
// Fake AuthorizationRepository — minimal stub tracking contract calls only.
// Does NOT replicate AuthorizationRepositoryImpl internals.
// Best-effort server call + session clear logic belongs to impl tests.
// ---------------------------------------------------------------------------
private class FakeAuthorizationRepository(
    private val tokenStorage: InMemoryTokenStorage
) : AuthorizationRepository {

    var logoutCalled = false

    override suspend fun loginUser(username: String, password: String): String? = null

    override suspend fun registerUser(
        username: String?,
        email: String,
        password: String,
        gender: Gender,
        verificationToken: String
    ): String? = null

    override suspend fun requestPasswordReset(email: String): String? = null

    override suspend fun resetPassword(email: String, code: String, newPassword: String): String? = null

    override suspend fun logout() {
        logoutCalled = true
        tokenStorage.clearSession()
    }

    override suspend fun verifyEmail(code: String): String? = null
    override suspend fun resendVerification(): String? = null
    override suspend fun requestRegistrationCode(email: String): String? = null
    override suspend fun verifyRegistrationCode(email: String, code: String): Result<String> = Result.success("token")
}

// ---------------------------------------------------------------------------
// Tests: LogoutUseCase contract only (not AuthorizationRepositoryImpl logic)
// ---------------------------------------------------------------------------
class LogoutUseCaseTest {

    private lateinit var tokenStorage: InMemoryTokenStorage
    private lateinit var authorizationRepository: FakeAuthorizationRepository
    private lateinit var networkRepository: RecordingNetworkRepository
    private lateinit var useCase: LogoutUseCase

    @Before
    fun setUp() {
        tokenStorage = InMemoryTokenStorage(accessToken = "access-token", refreshToken = "refresh-token-abc")
        authorizationRepository = FakeAuthorizationRepository(tokenStorage)
        networkRepository = RecordingNetworkRepository()
        useCase = LogoutUseCase(
            authorizationRepository = authorizationRepository,
            networkRepository = networkRepository
        )
        println("\n" + "=".repeat(60))
        println("  LOGOUT USE CASE TEST")
        println("=".repeat(60))
    }

    // -----------------------------------------------------------------------
    // 1. execute() вызывает authorizationRepository.logout()
    // -----------------------------------------------------------------------
    @Test
    fun `execute calls authorizationRepository logout`() = runTest {
        println("\n[TEST 1] execute() делегирует logout в authorizationRepository")

        useCase.execute()

        assertTrue("authorizationRepository.logout() должен быть вызван", authorizationRepository.logoutCalled)
        println("  PASSED")
    }

    // -----------------------------------------------------------------------
    // 2. execute() вызывает networkRepository.clearAuthCache()
    // -----------------------------------------------------------------------
    @Test
    fun `execute calls networkRepository clearAuthCache`() = runTest {
        println("\n[TEST 2] execute() вызывает networkRepository.clearAuthCache()")

        useCase.execute()

        assertTrue("networkRepository.clearAuthCache() должен быть вызван", networkRepository.authCacheClears > 0)
        println("  PASSED")
    }

    // -----------------------------------------------------------------------
    // 3. После execute() локальная сессия очищена (через контракт репозитория)
    // -----------------------------------------------------------------------
    @Test
    fun `execute results in session being cleared`() = runTest {
        println("\n[TEST 3] После execute() сессия очищена")

        useCase.execute()

        assertTrue("tokenStorage.clearSession() должен быть вызван", tokenStorage.clearSessionCalls > 0)
        assertNull("Access token должен быть null", tokenStorage.getAccessToken())
        assertNull("Refresh token должен быть null", tokenStorage.getRefreshToken())
        println("  PASSED")
    }

    // -----------------------------------------------------------------------
    // 4. execute() вызывает оба метода в правильном порядке:
    //    logout() перед clearAuthCache()
    // -----------------------------------------------------------------------
    @Test
    fun `execute calls logout before clearAuthCache`() = runTest {
        println("\n[TEST 4] logout() вызывается до clearAuthCache()")

        val order = mutableListOf<String>()
        val orderedRepo = object : AuthorizationRepository {
            override suspend fun loginUser(username: String, password: String): String? = null
            override suspend fun registerUser(username: String?, email: String, password: String, gender: Gender, verificationToken: String): String? = null
            override suspend fun requestPasswordReset(email: String): String? = null
            override suspend fun resetPassword(email: String, code: String, newPassword: String): String? = null
            override suspend fun logout() { order.add("logout") }
            override suspend fun verifyEmail(code: String): String? = null
            override suspend fun resendVerification(): String? = null
            override suspend fun requestRegistrationCode(email: String): String? = null
            override suspend fun verifyRegistrationCode(email: String, code: String): Result<String> = Result.success("token")
        }
        val orderedNet = object : NetworkRepository {
            override fun getClient(): HttpClient = throw UnsupportedOperationException()
            override val baseUrl: String get() = ""
            override fun clearAuthCache() { order.add("clearAuthCache") }
        }
        LogoutUseCase(orderedRepo, orderedNet).execute()

        println("  Order: $order")
        org.junit.Assert.assertEquals(listOf("logout", "clearAuthCache"), order)
        println("  PASSED")
    }

    // -----------------------------------------------------------------------
    // 5. E2E: авторизован → logout → refresh token gone → клиент не может
    //    выполнить POST /refresh (эквивалент 401)
    // -----------------------------------------------------------------------
    @Test
    fun `after execute refresh token is gone so refresh would return 401`() = runTest {
        println("\n[TEST 5] E2E: авторизован → logout → refresh token gone → 401")

        println("  До logout: refreshToken=${tokenStorage.getRefreshToken()}")
        assertTrue("Перед logout refresh token присутствует", tokenStorage.getRefreshToken() != null)

        useCase.execute()

        println("  После logout: refreshToken=${tokenStorage.getRefreshToken()}")
        assertNull(
            "После logout refresh token null → POST /refresh невозможен (401)",
            tokenStorage.getRefreshToken()
        )
        println("  Refresh token = null → POST /refresh вернёт 401")
        println("  PASSED")
    }
}
