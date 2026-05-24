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
    imageUrl = imageUrl.orEmpty(),
    category = category,
    styles = styleTags?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() },
    season = season?.let { listOf(it) },
    color = colors?.firstOrNull(),
    size = null,
    tags = null,
    marketplaceLinks = storeUrl?.let { listOf(it) } ?: emptyList(),
)

fun ClotheDto.toClotheDetail(): ClotheDetailModel = ClotheDetailModel(
    id = id,
    name = name,
    imageUrl = imageUrl.orEmpty(),
    category = category,
    material = material,
    fit = fit,
    styles = styleTags?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() },
    season = season?.let { listOf(it) },
    color = colors?.firstOrNull(),
    brand = brand,
    size = null,
    marketplaceLinks = storeUrl?.let { listOf(it) } ?: emptyList(),
    tags = null,
    createdAt = null,
)

fun ClotheDetailModel.toUpdateRequest(): ClotheUpdateRequest = ClotheUpdateRequest(
    name = name,
    category = category,
    material = material,
    fit = fit,
    styleTags = styles?.joinToString(","),
    season = season?.firstOrNull(),
    colors = color?.let { listOf(it) },
    brand = brand,
    storeUrl = marketplaceLinks.firstOrNull(),
)

fun LookDto.toLook(): LookModel = LookModel(
    id = id,
    name = name,
    imageUrl = url,
)
