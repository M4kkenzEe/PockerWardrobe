package com.ownstd.project.network.internal

import com.ownstd.project.network.api.NetworkRepository
import com.ownstd.project.storage.TokenStorage
import io.ktor.client.HttpClient

internal class NetworkRepositoryImpl(
    override val baseUrl: String,
    private val tokenStorage: TokenStorage
) : NetworkRepository {
    init {
        println("🔧 NetworkRepository initialized with baseUrl: $baseUrl")
    }

    override fun getClient(): HttpClient {
        return NetworkClient.getClient(tokenStorage, baseUrl)
    }
}
