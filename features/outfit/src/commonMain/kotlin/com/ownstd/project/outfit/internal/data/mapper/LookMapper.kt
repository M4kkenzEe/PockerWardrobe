package com.ownstd.project.outfit.internal.data.mapper

import com.ownstd.project.outfit.internal.data.dto.LookItemDto
import com.ownstd.project.outfit.internal.data.dto.LookDto
import com.ownstd.project.outfit.internal.domain.model.LookModel
import com.ownstd.project.outfit.internal.domain.model.LookItemModel

fun LookDto.toLook(): LookModel = LookModel(
    id = id,
    name = name,
    url = url,
    lookItems = lookItems?.map { it.toLookItemModel() },
    style = null,
    tags = null,
)

fun LookItemDto.toLookItemModel(): LookItemModel = LookItemModel(
    id = clothe.id ?: 0,
    clotheId = clothe.id ?: 0,
    imageUrl = clothe.imageUrl.orEmpty(),
    name = clothe.name,
    category = clothe.category,
)
