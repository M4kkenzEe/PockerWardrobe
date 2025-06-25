package com.ownstd.project.wardrobe.internal.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders

val client = HttpClient(CIO) {
    install(Logging) {
        level = LogLevel.ALL
    }
}

val url = "http://localhost:8080/clothes"

suspend fun ff(
    client: HttpClient,
    url: String,
    photoBytes: ByteArray,
    fileName: String
) {

    val response = client.submitFormWithBinaryData(
        url = url,
        formData = formData {
            append("name", "ExampleName") // Значение поля name
            append("storeUrl", "http://examplestore.com") // Значение поля storeUrl
            append("image", photoBytes, Headers.build {
                append(HttpHeaders.ContentType, "image/png")
                append(
                    HttpHeaders.ContentDisposition,
                    "form-data; name=\"image\"; filename=\"$fileName\""
                )
            })
        }
    )
}


