package com.ownstd.project.wardrobe.internal.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ClotheDto(
    val id: Int? = null,
    val imageUrl: String? = null,
    val name: String,
    val storeUrl: String? = null,
    val season: String? = null,
    val fit: String? = null,
    val material: String? = null,
    val category: String? = null,
    val styleTags: String? = null,
    val brand: String? = null,
    val colors: List<String>? = null,
)
