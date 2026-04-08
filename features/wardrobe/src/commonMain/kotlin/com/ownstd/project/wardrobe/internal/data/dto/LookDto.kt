package com.ownstd.project.wardrobe.internal.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LookDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("image_url") val imageUrl: String? = null,
)
