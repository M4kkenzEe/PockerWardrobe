package com.ownstd.project.network.internal

import com.ownstd.project.network.api.NetworkRepository
import com.ownstd.project.storage.SessionManager
import com.ownstd.project.storage.TokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider

internal class NetworkRepositoryImpl(
    override val baseUrl: String,
    private val tokenStorage: TokenStorage,
    private val sessionManager: SessionManager
) : NetworkRepository {

    private val httpClient: HttpClient by lazy {
        NetworkClient.getClient(tokenStorage, baseUrl, sessionManager)
    }

    override fun getClient(): HttpClient = httpClient

    override fun clearAuthCache() {
        httpClient.authProvider<BearerAuthProvider>()?.clearToken()
    }
}
