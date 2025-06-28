package com.ownstd.project.network.internal

import com.ownstd.project.network.api.NetworkRepository
import io.ktor.client.HttpClient

internal class NetworkRepositoryImpl(override val baseUrl: String) : NetworkRepository {
    override fun getClient(): HttpClient {
        return NetworkClient.getClient()
    }
}