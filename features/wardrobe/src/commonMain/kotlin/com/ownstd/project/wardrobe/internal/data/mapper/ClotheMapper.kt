package com.ownstd.project.wardrobe.internal.data.mapper

import com.ownstd.project.wardrobe.internal.data.dto.ClotheDto
import com.ownstd.project.wardrobe.internal.data.dto.ClotheUpdateRequest
import com.ownstd.project.wardrobe.internal.data.dto.LookDto
import com.ownstd.project.wardrobe.internal.domain.model.ClotheModel
import com.ownstd.project.wardrobe.internal.domain.model.ClotheDetailModel
import com.ownstd.project.wardrobe.internal.domain.model.LookModel

fun ClotheDto.toClothe(): ClotheModel = ClotheModel(
    id = id,
    name = name,
    imageUrl = imageUrl,
    category = category,
    styles = styles,
    season = season,
    color = color,
    size = size,
    tags = tags,
    marketplaceLinks = marketplaceLinks
        ?: storeUrl?.let { listOf(it) }  // fallback для старого API
        ?: emptyList(),
)

fun ClotheDto.toClotheDetail(): ClotheDetailModel = ClotheDetailModel(
    id = id,
    name = name,
    imageUrl = imageUrl,
    category = category,
    material = material,
    fit = fit,
    styles = styles,
    season = season,
    color = color,
    brand = brand,
    size = size,
    marketplaceLinks = marketplaceLinks
        ?: storeUrl?.let { listOf(it) }
        ?: emptyList(),
    tags = tags,
    createdAt = createdAt,
)

fun ClotheDetailModel.toUpdateRequest(): ClotheUpdateRequest = ClotheUpdateRequest(
    name = name,
    category = category,
    material = material,
    fit = fit,
    styles = styles,
    season = season,
    color = color,
    brand = brand,
    size = size,
    marketplaceLinks = marketplaceLinks.ifEmpty { null },
)

fun LookDto.toLook(): LookModel = LookModel(
    id = id,
    name = name,
    imageUrl = imageUrl,
)
