package com.ownstd.project.profile.internal.data.api

import com.ownstd.project.network.api.NetworkRepository
import com.ownstd.project.profile.internal.data.dto.UpdateProfileRequest
import com.ownstd.project.profile.internal.data.dto.UserDto
import com.ownstd.project.profile.internal.data.dto.UserSizesDto
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

internal class ProfileApi(private val networkRepository: NetworkRepository) {

    private val baseUrl get() = networkRepository.baseUrl
    private val client get() = networkRepository.getClient()

    suspend fun getProfile(): UserDto =
        client.get("${baseUrl}profile") {
            contentType(ContentType.Application.Json)
        }.body()

    suspend fun updateProfile(request: UpdateProfileRequest): UserDto =
        client.patch("${baseUrl}profile") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun getUserSizes(): UserSizesDto =
        client.get("${baseUrl}user/sizes") {
            contentType(ContentType.Application.Json)
        }.body()

    suspend fun updateUserSizes(dto: UserSizesDto): UserSizesDto =
        client.patch("${baseUrl}user/sizes") {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }.body()
}
