package com.ownstd.project.outfitconstructor.internal.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ClotheDto(
    val id: Int? = null,
    val name: String,
    val imageUrl: String? = null,
    val category: String? = null,
)
