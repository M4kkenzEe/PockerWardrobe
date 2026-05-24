package com.ownstd.project.wardrobe.internal.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ClotheUpdateRequest(
    val name: String? = null,
    val category: String? = null,
    val material: String? = null,
    val fit: String? = null,
    val styleTags: String? = null,
    val season: String? = null,
    val colors: List<String>? = null,
    val brand: String? = null,
    val storeUrl: String? = null,
)
