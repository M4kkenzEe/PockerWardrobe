package com.ownstd.project.outfit.internal.domain.model

data class Look(
    val id: Int? = null,
    val name: String,
    val url: String,
    val lookItems: List<LookItem>? = null,
    val style: String? = null,
    val tags: List<String>? = null,
    val isLocked: Boolean = false,
)

data class LookItem(
    val id: Int,
    val clotheId: Int,
    val imageUrl: String,
    val name: String,
    val category: String? = null,
)

data class DraftLook(
    val name: String,
    val clotheIds: List<Int>,
    val style: String? = null,
)
