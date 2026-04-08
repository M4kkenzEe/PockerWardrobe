package com.ownstd.project.wardrobe.internal.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClotheDto(
    @SerialName("id") val id: Int?,
    @SerialName("name") val name: String,
    @SerialName("image_url") val imageUrl: String,
    @SerialName("category") val category: String? = null,
    @SerialName("material") val material: String? = null,
    @SerialName("fit") val fit: String? = null,
    @SerialName("styles") val styles: List<String>? = null,
    @SerialName("season") val season: List<String>? = null,
    @SerialName("color") val color: String? = null,
    @SerialName("brand") val brand: String? = null,
    @SerialName("size") val size: String? = null,
    @SerialName("marketplace_links") val marketplaceLinks: List<String>? = null,
    @SerialName("tags") val tags: List<String>? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("store_url") val storeUrl: String? = null,  // deprecated — обратная совместимость
)
