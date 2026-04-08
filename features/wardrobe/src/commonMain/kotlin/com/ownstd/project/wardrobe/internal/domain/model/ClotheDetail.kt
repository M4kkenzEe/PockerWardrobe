package com.ownstd.project.wardrobe.internal.domain.model

data class ClotheDetail(
    val id: Int?,
    val name: String,
    val imageUrl: String,
    val category: String? = null,
    val material: String? = null,
    val fit: String? = null,
    val styles: List<String>? = null,
    val season: List<String>? = null,
    val color: String? = null,
    val brand: String? = null,
    val size: String? = null,
    val marketplaceLinks: List<String> = emptyList(),
    val tags: List<String>? = null,
    val createdAt: String? = null,
)
