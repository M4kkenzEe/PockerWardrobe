package com.ownstd.project.pincard.internal.data.model

import kotlinx.serialization.Serializable

@Serializable
data class DraftLookPreview(val lookId: Int, val url: String)

@Serializable
data class GenerateDraftsResponse(
    val looks: List<DraftLookPreview> = emptyList(),
    val allSaved: Boolean = false,
)

@Serializable
data class GenerateDraftsErrorResponse(
    val error: String? = null,
    val missing: List<String> = emptyList(),
)

enum class MissingCategory { Top, Bottom, Shoes }

fun parseMissingCategories(raw: List<String>): List<MissingCategory> = raw.mapNotNull {
    when (it) {
        "top" -> MissingCategory.Top
        "bottom" -> MissingCategory.Bottom
        "shoes" -> MissingCategory.Shoes
        else -> null
    }
}

sealed class GenerateDraftsResult {
    data class Success(val looks: List<DraftLookPreview>) : GenerateDraftsResult()
    data object AllSaved : GenerateDraftsResult()
    data class NeedMore(val missing: List<MissingCategory>) : GenerateDraftsResult()
    data object Unsupported : GenerateDraftsResult()
    data object Error : GenerateDraftsResult()
}
