package com.ownstd.project.pincard.internal.presentation.viewmodel

import androidx.compose.ui.graphics.Color
import com.ownstd.project.pincard.internal.data.model.Clothe
import com.ownstd.project.pincard.internal.replaceFragment
import io.ktor.http.Url

internal enum class ClothingField { Name, Season, Fit, Material, Brand, Occasion, Style, StoreUrl }
internal enum class ClothingDetailError { Load, NotFound, Ownership, Save, Add, NameRequired, InvalidUrl, OpenStore }
internal data class ClothingColor(val label: String, val color: Color?)
internal data class ClothingAttribute(val field: ClothingField, val value: String?)
internal data class ClothingDetailUiModel(
    val name: String,
    val category: String?,
    val imageUrl: String,
    val attributes: List<ClothingAttribute>,
    val colors: List<ClothingColor>,
    val tags: List<String>,
    val storeUrl: String?,
    val storeDomain: String?,
    val hasStoreValue: Boolean,
)

internal fun validatedStoreUrl(value: String): String? {
    val trimmed = value.trim()
    if (!trimmed.startsWith("https://", ignoreCase = true) &&
        !trimmed.startsWith("http://", ignoreCase = true)) return null
    if (trimmed.any { it.isWhitespace() }) return null
    return runCatching { Url(trimmed) }.getOrNull()
        ?.takeIf { it.host.isNotBlank() }?.let { trimmed }
}

internal fun Clothe.toDetailUiModel(): ClothingDetailUiModel {
    val validUrl = storeUrl?.let(::validatedStoreUrl)
    return ClothingDetailUiModel(
        name = name.trim(),
        category = category?.trim()?.takeIf { it.isNotEmpty() },
        imageUrl = imageUrl.replaceFragment(),
        attributes = listOf(
            ClothingAttribute(ClothingField.Season, season),
            ClothingAttribute(ClothingField.Fit, fit),
            ClothingAttribute(ClothingField.Material, material),
            ClothingAttribute(ClothingField.Brand, brand),
            ClothingAttribute(ClothingField.Occasion, occasion),
        ).map { it.copy(value = it.value?.trim()?.takeIf(String::isNotEmpty)) },
        colors = colors.orEmpty().filter(String::isNotBlank).map { value ->
            val hex = value.trim().removePrefix("#")
            val argb = when (hex.length) {
                6 -> hex.toLongOrNull(16)?.or(0xFF000000)
                8 -> hex.toLongOrNull(16)
                else -> null
            }
            ClothingColor(value.trim(), argb?.let { Color(it) })
        },
        tags = styleTags.orEmpty().split(",").map(String::trim).filter(String::isNotEmpty),
        storeUrl = validUrl,
        storeDomain = validUrl?.let { Url(it).host.removePrefix("www.") },
        hasStoreValue = !storeUrl.isNullOrBlank(),
    )
}

internal fun Clothe.toEditFields(): Map<ClothingField, String> = mapOf(
    ClothingField.Name to name,
    ClothingField.Season to season.orEmpty(),
    ClothingField.Fit to fit.orEmpty(),
    ClothingField.Material to material.orEmpty(),
    ClothingField.Brand to brand.orEmpty(),
    ClothingField.Occasion to occasion.orEmpty(),
    ClothingField.Style to styleTags.orEmpty(),
    ClothingField.StoreUrl to storeUrl.orEmpty(),
)
