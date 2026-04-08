package com.ownstd.project.wardrobe.internal.data.mapper

import com.ownstd.project.wardrobe.internal.data.dto.ClotheDto
import com.ownstd.project.wardrobe.internal.data.dto.ClotheUpdateRequest
import com.ownstd.project.wardrobe.internal.data.dto.LookDto
import com.ownstd.project.wardrobe.internal.domain.model.Clothe
import com.ownstd.project.wardrobe.internal.domain.model.ClotheDetail
import com.ownstd.project.wardrobe.internal.domain.model.Look

fun ClotheDto.toClothe(): Clothe = Clothe(
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

fun ClotheDto.toClotheDetail(): ClotheDetail = ClotheDetail(
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

fun ClotheDetail.toUpdateRequest(): ClotheUpdateRequest = ClotheUpdateRequest(
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

fun LookDto.toLook(): Look = Look(
    id = id,
    name = name,
    imageUrl = imageUrl,
)
