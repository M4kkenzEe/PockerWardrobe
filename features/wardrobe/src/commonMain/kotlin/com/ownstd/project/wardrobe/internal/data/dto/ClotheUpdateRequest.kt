package com.ownstd.project.wardrobe.internal.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClotheUpdateRequest(
    @SerialName("name") val name: String? = null,
    @SerialName("category") val category: String? = null,
    @SerialName("material") val material: String? = null,
    @SerialName("fit") val fit: String? = null,
    @SerialName("styles") val styles: List<String>? = null,
    @SerialName("season") val season: List<String>? = null,
    @SerialName("color") val color: String? = null,
    @SerialName("brand") val brand: String? = null,
    @SerialName("size") val size: String? = null,
    @SerialName("marketplace_links") val marketplaceLinks: List<String>? = null,
)
