package com.ownstd.project.pincard.internal.data.repository

import androidx.compose.ui.graphics.ImageBitmap
import com.ownstd.project.network.api.NetworkRepository
import com.ownstd.project.pincard.internal.data.model.Clothe
import com.ownstd.project.pincard.internal.domain.repository.WardrobeRepository
import com.ownstd.project.storage.TokenStorage
import io.github.suwasto.capturablecompose.CompressionFormat
import io.github.suwasto.capturablecompose.toByteArray
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.append
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.utils.io.core.writeFully

class WardrobeRepositoryImpl(
    private val networkRepository: NetworkRepository,
    private val storage: TokenStorage
) : WardrobeRepository {

    val client = networkRepository.getClient()
    val baseUrl = networkRepository.baseUrl

    private fun getToken(): String? = storage.getToken()

    override suspend fun getClothes(): List<Clothe> {
        return client.get(baseUrl + ENDPOINT) {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer ${getToken()}")
        }.body()
    }

    override suspend fun loadClothe(bitmap: ImageBitmap) {
        val imageBytes = bitmap.toByteArray(
            CompressionFormat.PNG, 100
        )

        client.post(baseUrl + ENDPOINT) {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer ${getToken()}")
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("name", "")
                        append("storeUrl", "")
                        append(
                            key = "image",
                            filename = "clothing_image.png",
                            contentType = ContentType.Image.PNG
                        ) {
                            writeFully(imageBytes)
                        }
                    }
                )
            )

        }
    }

    override suspend fun uploadFromUrl(pageUrl: String): Clothe {
        val response = client.get(baseUrl + ENDPOINT + FROM_URL) {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer ${getToken()}")
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
            client.delete("$baseUrl/$ENDPOINT/$clotheId") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer ${getToken()}")
            }.body()
        } catch (e: Exception) {
            println("ERR: ${e.message}")
        }
    }

    companion object {
        private const val ENDPOINT = "clothes"
        private const val FROM_URL = "/from_url"
    }
}

