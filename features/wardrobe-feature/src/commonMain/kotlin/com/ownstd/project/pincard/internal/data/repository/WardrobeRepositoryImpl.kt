package com.ownstd.project.pincard.internal.data.repository

import androidx.compose.ui.graphics.ImageBitmap
import com.ownstd.project.network.api.NetworkRepository
import com.ownstd.project.pincard.internal.data.model.Clothe
import com.ownstd.project.pincard.internal.domain.repository.WardrobeRepository
import io.github.suwasto.capturablecompose.CompressionFormat
import io.github.suwasto.capturablecompose.toByteArray
import com.ownstd.project.pincard.internal.domain.FreemiumLimitException
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.append
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.serialization.Serializable
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.utils.io.core.writeFully

class WardrobeRepositoryImpl(
    private val networkRepository: NetworkRepository,
) : WardrobeRepository {

    val client = networkRepository.getClient()
    val baseUrl = networkRepository.baseUrl

    override suspend fun getClothes(occasion: String?): List<Clothe> {
        return try {
            val fullUrl = baseUrl + ENDPOINT
            println("🌐 [CLOTHES_REQUEST] GET $fullUrl occasion=$occasion")

            val httpResponse = client.get(fullUrl) {
                contentType(ContentType.Application.Json)
                if (!occasion.isNullOrBlank()) {
                    url { parameters.append("occasion", occasion) }
                }
            }

            println("✅ [CLOTHES_RESPONSE] Status: ${httpResponse.status.value}")

            val clothes: List<Clothe> = httpResponse.body()

            println("📊 [CLOTHES_DATA] Received ${clothes.size} items")

            clothes
        } catch (e: Exception) {
            println("❌ [CLOTHES_ERROR] ${e::class.simpleName}: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun loadClothe(bitmap: ImageBitmap, occasion: String?) {
        val imageBytes = bitmap.toByteArray(CompressionFormat.JPEG, IMAGE_QUALITY)

        println("📤 [UPLOAD_START] POST ${baseUrl + ENDPOINT} occasion=$occasion")
        println("📦 [UPLOAD_BYTES] ${imageBytes.size} bytes (~${imageBytes.size / 1024} KB)")

        try {
            val response = client.post(baseUrl + ENDPOINT) {
                contentType(ContentType.Application.Json)
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("name", "")
                            append("storeUrl", "")
                            if (!occasion.isNullOrBlank()) append("occasion", occasion)
                            append(
                                key = "image",
                                filename = "clothing_image.jpg",
                                contentType = ContentType.Image.JPEG
                            ) {
                                writeFully(imageBytes)
                            }
                        }
                    )
                )
            }

            println("📥 [UPLOAD_RESPONSE] HTTP ${response.status.value} ${response.status.description}")

            when {
                response.status == HttpStatusCode.PaymentRequired -> {
                    println("🚫 [UPLOAD_LIMIT] Freemium limit reached")
                    throw FreemiumLimitException()
                }
                !response.status.isSuccess() -> {
                    val errorBody = response.bodyAsText()
                    println("❌ [UPLOAD_ERROR] Server error body: $errorBody")
                    throw Exception("Upload failed: ${response.status.value}")
                }
                else -> println("✅ [UPLOAD_SUCCESS] Photo uploaded successfully")
            }
        } catch (e: FreemiumLimitException) {
            throw e
        } catch (e: Exception) {
            println("❌ [UPLOAD_EXCEPTION] ${e::class.simpleName}: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun uploadFromUrl(pageUrl: String): Clothe {
        val response = client.get(baseUrl + ENDPOINT + FROM_URL) {
            contentType(ContentType.Application.Json)
            url {
                parameters.append("url", pageUrl)
            }
        }
        return if (response.status.isSuccess()) {
            response.body()
        } else {
            Clothe.empty()
        }
    }

    override suspend fun deleteClothe(clotheId: Int) {
        try {
            client.delete("$baseUrl$ENDPOINT/$clotheId") {
                contentType(ContentType.Application.Json)
            }.body()
        } catch (e: Exception) {
            println("❌ [DELETE_ERROR] ${e::class.simpleName}: ${e.message}")
        }
    }

    override suspend fun updateClothe(
        clotheId: Int,
        name: String?,
        storeUrl: String?,
        season: String?,
        fit: String?,
        material: String?,
        brand: String?,
        occasion: String?,
        styleTags: String?,
    ): Clothe {
        val response = client.patch("$baseUrl$ENDPOINT/$clotheId") {
            contentType(ContentType.Application.Json)
            setBody(UpdateClotheRequest(name, storeUrl, season, fit, material, brand, occasion, styleTags))
        }
        return response.body()
    }

    companion object {
        private const val ENDPOINT = "clothes"
        private const val FROM_URL = "/from_url"
        private const val IMAGE_QUALITY = 80
    }
}

@Serializable
private data class UpdateClotheRequest(
    val name: String? = null,
    val storeUrl: String? = null,
    val season: String? = null,
    val fit: String? = null,
    val material: String? = null,
    val brand: String? = null,
    val occasion: String? = null,
    val styleTags: String? = null,
)
