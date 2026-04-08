package com.ownstd.project.outfit.internal.data.mapper

import com.ownstd.project.outfit.internal.data.dto.LookItemDto
import com.ownstd.project.outfit.internal.data.dto.LookDto
import com.ownstd.project.outfit.internal.domain.model.Look
import com.ownstd.project.outfit.internal.domain.model.LookItem

fun LookDto.toLook(): Look = Look(
    id = id,
    name = name,
    url = url,
    lookItems = lookItems?.map { it.toLookItem() },
    style = style,
    tags = tags,
)

fun LookItemDto.toLookItem(): LookItem = LookItem(
    id = id,
    clotheId = clotheId,
    imageUrl = imageUrl,
    name = name.orEmpty(),
    category = category,
)
