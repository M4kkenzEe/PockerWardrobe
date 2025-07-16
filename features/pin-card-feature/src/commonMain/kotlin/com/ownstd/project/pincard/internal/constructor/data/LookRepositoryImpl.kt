package com.ownstd.project.pincard.internal.constructor.data

import com.ownstd.project.network.api.NetworkRepository
import com.ownstd.project.pincard.internal.constructor.data.model.Look
import com.ownstd.project.pincard.internal.constructor.domain.LookRepository
import com.ownstd.project.storage.TokenStorage
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

class LookRepositoryImpl(
    private val networkRepository: NetworkRepository,
    private val storage: TokenStorage
) : LookRepository {

    val client = networkRepository.getClient()
    val baseUrl = networkRepository.baseUrl
    val token = storage.getToken()

    override suspend fun addLookItem() {

    }

    override suspend fun addLook(look: Look) {
        try {
            val response = client.post(baseUrl + LOOK_ENDPOINT) {
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(look)
            }

            when (response.status) {
//                HttpStatusCode.Created -> Result.success(response.body<User>())
//                HttpStatusCode.Conflict -> Result.failure(Exception("User with this username or email already exists"))
//                else -> Result.failure(Exception("Unexpected response: ${response.status}"))
            }

        } catch (e: Exception) {
            println("Error: ${e.message}")
        }
    }

    companion object {
        private const val LOOK_ENDPOINT = "looks"
    }
}