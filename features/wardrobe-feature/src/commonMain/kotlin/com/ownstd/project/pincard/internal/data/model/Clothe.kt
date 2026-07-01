package com.ownstd.project.pincard.internal.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Clothe(
    val id: Int?,
    val name: String,
    val storeUrl: String? = null,
    val imageUrl: String,
    val occasion: String? = null,
    val season: String? = null,
    val fit: String? = null,
    val material: String? = null,
    val category: String? = null,
    val styleTags: String? = null,
    val brand: String? = null,
    val colors: List<String>? = null,
) {
    companion object {
        fun empty(): Clothe {
            return Clothe(
                id = null,
                name = "",
                storeUrl = "",
                imageUrl = ""
            )
        }
    }
}