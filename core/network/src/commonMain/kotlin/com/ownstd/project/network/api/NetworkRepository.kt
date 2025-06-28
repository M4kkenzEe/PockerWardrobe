package com.ownstd.project.network.api

import io.ktor.client.HttpClient

interface NetworkRepository {
    val baseUrl: String
    fun getClient(): HttpClient
}