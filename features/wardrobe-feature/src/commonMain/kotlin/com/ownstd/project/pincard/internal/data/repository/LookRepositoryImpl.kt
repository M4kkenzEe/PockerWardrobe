package com.ownstd.project.pincard.internal.data.repository

import com.ownstd.project.network.api.NetworkRepository
import com.ownstd.project.pincard.internal.data.model.DraftLook
import com.ownstd.project.pincard.internal.data.model.Look
import com.ownstd.project.pincard.internal.data.model.LookRepositoryResult
import com.ownstd.project.pincard.internal.data.model.ShareResponse
import com.ownstd.project.pincard.internal.data.model.SharedLookResponse
import com.ownstd.project.pincard.internal.domain.repository.LookRepository
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

class LookRepositoryImpl(
    private val networkRepository: NetworkRepository,
) : LookRepository {
    val client = networkRepository.getClient()
    val baseUrl = networkRepository.baseUrl

    override suspend fun getLooks(): List<Look> {
        var response = emptyList<Look>()
        try {
            val fullUrl = baseUrl + ENDPOINT
            println("🌐 [LOOKS_REQUEST] GET $fullUrl")

            val httpResponse = client.get(fullUrl) {
                contentType(ContentType.Application.Json)
            }

            println("✅ [LOOKS_RESPONSE] Status: ${httpResponse.status.value}")

            response = httpResponse.body()

            println("📊 [LOOKS_DATA] Received ${response.size} looks")
        } catch (e: Exception) {
            println("❌ [LOOKS_ERROR] ${e::class.simpleName}: ${e.message}")
            e.printStackTrace()
        }
        return response
    }

    override suspend fun addLook(
        look: DraftLook,
        image: ByteArray
    ): LookRepositoryResult<Unit> {
        return try {
            val uploadResponse = client.post(baseUrl + ENDPOINT + ADD_IMAGE) {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("image", image, Headers.build {
                                append(HttpHeaders.ContentType, "image/jpeg")
                                append(HttpHeaders.ContentDisposition, "filename=\"image.jpg\"")
                            })
                        }
                    ))
            }

            val uploadResult = uploadResponse.body<Map<String, String>>()
            val imageUrl = uploadResult["imageUrl"] ?: return LookRepositoryResult.NetworkError(
                IllegalStateException("Image URL missing in response")
            )

            val lookWithImage = Look(
                name = look.name,
                lookItems = look.lookItems,
                url = imageUrl
            )

            client.post(baseUrl + ENDPOINT) {
                contentType(ContentType.Application.Json)
                setBody(lookWithImage)
            }

            LookRepositoryResult.Success(Unit)
        } catch (e: Exception) {
            println("ERR: ${e.message}")
            LookRepositoryResult.NetworkError(e)
        }
    }

    override suspend fun getLookById(lookId: Int): Look? {
        var response: Look? = null
        try {
            response = client.get(baseUrl + ENDPOINT + BY_ID + lookId.toString()) {
                contentType(ContentType.Application.Json)
            }.body()
        } catch (e: Exception) {
            println("ERR: ${e.message}")
        }
        return response
    }

    override suspend fun getLookByShareToken(token: String): Look? {
        return try {
            val sharedResponse: SharedLookResponse = client.get("$baseUrl$SHARED_ENDPOINT/$token") {
                contentType(ContentType.Application.Json)
            }.body()
            sharedResponse.look
        } catch (e: Exception) {
            println("ERR getLookByShareToken: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    override suspend fun addLookByShareToken(sharedToken: String) {
        try {
            client.post("$baseUrl$SHARES_ENDPOINT/$sharedToken/import") {
                contentType(ContentType.Application.Json)
                setBody("""{"importType":"FULL_LOOK"}""")
            }.body()
        } catch (e: Exception) {
            println("ERR getLookByShareToken: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    override suspend fun deleteLook(lookId: Int) {
        try {
            client.delete("$baseUrl$ENDPOINT/$lookId") {
                contentType(ContentType.Application.Json)
            }.body()
        } catch (e: Exception) {
            println("ERR: ${e.message}")
        }
    }

    override suspend fun shareLook(lookId: Int): ShareResponse? {
        return try {
            client.post("$baseUrl$ENDPOINT/$lookId/share") {
                contentType(ContentType.Application.Json)
            }.body()
        } catch (e: Exception) {
            println("ERR shareLook: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    companion object {
        private const val ENDPOINT = "looks"
        private const val ADD_IMAGE = "/uploadImage"
        private const val BY_ID = "/byId/"
        private const val SHARED_ENDPOINT = "shared"
        private const val SHARES_ENDPOINT = "shares"
    }
}
