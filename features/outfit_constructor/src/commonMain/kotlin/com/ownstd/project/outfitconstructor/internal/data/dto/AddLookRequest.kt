package com.ownstd.project.outfitconstructor.internal.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddLookRequest(
    @SerialName("name") val name: String,
    @SerialName("clothe_ids") val clotheIds: List<Int>,
)
