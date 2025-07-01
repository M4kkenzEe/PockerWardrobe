package com.ownstd.project.profile.data

import com.ownstd.project.network.api.NetworkRepository
import com.ownstd.project.profile.domain.ProfileRepository
import com.ownstd.project.storage.TokenStorage
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType

class ProfileRepositoryImpl(
    private val networkRepository: NetworkRepository,
    private val tokenStorage: TokenStorage,
) : ProfileRepository {

    private val baseUrl = networkRepository.baseUrl
    private val client = networkRepository.getClient()

    override suspend fun getProfile(): ProfileResponse {
        return try {
            val response = client.get(baseUrl + ENDPOINT) {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer ${tokenStorage.getToken()}")
            }
            response.body<ProfileResponse>()
        } catch (e: Exception) {
            println("getProfile Failed: ${e.message}")
            ProfileResponse.emptyBody()
        }
    }

    companion object {
        const val ENDPOINT = "profile"
    }
}