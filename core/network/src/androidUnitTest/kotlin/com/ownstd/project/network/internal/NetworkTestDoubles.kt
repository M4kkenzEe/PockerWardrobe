package com.ownstd.project.network.internal

import com.ownstd.project.storage.SessionManager
import com.ownstd.project.storage.TokenStorage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

internal class InMemoryTokenStorage(accessToken: String?, refreshToken: String?) : TokenStorage {
    private var accessToken: String? = accessToken
    private var refreshToken: String? = refreshToken
    private var expiresAt: Long? = 4_000_000_000_000L
    var clearCalls = 0

    fun dropRefreshToken() { refreshToken = null }

    override fun saveSession(accessToken: String, refreshToken: String, expiresAt: Long) {
        this.accessToken = accessToken
        this.refreshToken = refreshToken
        this.expiresAt = expiresAt
    }

    override fun getAccessToken() = accessToken
    override fun getRefreshToken() = refreshToken
    override fun getExpiresAt() = expiresAt

    override fun clearSession() {
        clearCalls++
        accessToken = null
        refreshToken = null
        expiresAt = null
    }
}

internal class CountingSessionManager : SessionManager {
    var expiredEvents = 0
    override val sessionExpiredEvent: SharedFlow<Unit> = MutableSharedFlow()
    override fun notifySessionExpired() { expiredEvents++ }
}
