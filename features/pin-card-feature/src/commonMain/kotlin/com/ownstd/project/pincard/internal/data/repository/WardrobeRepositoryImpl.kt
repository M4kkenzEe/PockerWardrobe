package com.ownstd.project.pincard.internal.data.repository

import androidx.compose.ui.graphics.ImageBitmap
import coil3.Bitmap
import com.ownstd.project.network.api.NetworkRepository
import com.ownstd.project.pincard.internal.data.model.Clothe
import com.ownstd.project.pincard.internal.domain.WardrobeRepository
import com.ownstd.project.storage.TokenStorage
import io.github.suwasto.capturablecompose.CompressionFormat
import io.github.suwasto.capturablecompose.toByteArray
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.append
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.utils.io.core.writeFully
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class WardrobeRepositoryImpl(
    private val networkRepository: NetworkRepository,
    private val storage: TokenStorage
) : WardrobeRepository {

    val client = networkRepository.getClient()
    val baseUrl = networkRepository.baseUrl
    val token = storage.getToken()
    override suspend fun getClothes(): List<Clothe> {
        return client.get(baseUrl + ENDPOINT) {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }.body()
    }

    override suspend fun loadClothe(bitmap: ImageBitmap) {
        val imageBytes = bitmap.toByteArray(
            CompressionFormat.PNG, 100
        )

        client.post(baseUrl + ENDPOINT) {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
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

    companion object {
        private const val ENDPOINT = "clothes"
    }
}

