package com.ownstd.project.pincard.internal.data.repository

import com.ownstd.project.network.api.NetworkRepository
import com.ownstd.project.pincard.internal.data.model.Look
import com.ownstd.project.pincard.internal.data.model.LookRepositoryResult
import com.ownstd.project.pincard.internal.data.model.LookResponse
import com.ownstd.project.pincard.internal.domain.repository.LookRepository
import com.ownstd.project.storage.TokenStorage
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

class LookRepositoryImpl(
    private val networkRepository: NetworkRepository,
    private val storage: TokenStorage
) : LookRepository {
    val client = networkRepository.getClient()
    val baseUrl = networkRepository.baseUrl
    val token = storage.getToken()
    override suspend fun getLooks(): List<LookResponse> {
        var response = emptyList<LookResponse>()
        try {
            response = client.get(baseUrl + ENDPOINT) {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $token")
            }.body()
        } catch (e: Exception) {
            println("ERR: ${e.message}")
        }
        return response
    }

    override suspend fun addLook(
        look: Look,
        image: ByteArray
    ): LookRepositoryResult<Unit> {
        return try {
            val uploadResponse = client.post(baseUrl + ENDPOINT + ADD_IMAGE) {
                header("Authorization", "Bearer $token")
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("image", image, Headers.build {
                                append(HttpHeaders.ContentType, "image/jpeg") // или image/png
                                append(HttpHeaders.ContentDisposition, "filename=\"image.jpg\"")
                            })
                        }
                    ))
            }

            val uploadResult = uploadResponse.body<Map<String, String>>()
            val imageUrl = uploadResult["imageUrl"] ?: return LookRepositoryResult.NetworkError(
                IllegalStateException("Image URL missing in response")
            )


            val lookWithImage = LookResponse(
                name = look.name,
                lookItems = look.lookItems,
                url = imageUrl
            )

            client.post(baseUrl + ENDPOINT) {
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(lookWithImage)
            }

            LookRepositoryResult.Success(Unit)
        } catch (e: Exception) {
            println("ERR: ${e.message}")
            LookRepositoryResult.NetworkError(e)
        }
    }


    companion object {
        private const val ENDPOINT = "looks"
        private const val ADD_IMAGE = "/uploadImage"
    }
}