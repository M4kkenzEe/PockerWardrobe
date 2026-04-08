package com.ownstd.project.wardrobe.internal.domain.model

data class Clothe(
    val id: Int?,
    val name: String,
    val imageUrl: String,
    val category: String?,
    val styles: List<String>?,
    val season: List<String>?,
    val color: String?,
    val size: String?,
    val tags: List<String>?,
    val marketplaceLinks: List<String>,
)
