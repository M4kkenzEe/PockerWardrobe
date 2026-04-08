package com.ownstd.project.outfitconstructor.internal.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClotheDto(
    @SerialName("id") val id: Int?,
    @SerialName("name") val name: String,
    @SerialName("image_url") val imageUrl: String,
    @SerialName("category") val category: String? = null,
)
