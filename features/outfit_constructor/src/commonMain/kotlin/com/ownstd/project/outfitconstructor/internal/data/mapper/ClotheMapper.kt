package com.ownstd.project.outfitconstructor.internal.data.mapper

import com.ownstd.project.outfitconstructor.internal.data.dto.ClotheDto
import com.ownstd.project.outfitconstructor.internal.domain.model.ClotheModel

fun ClotheDto.toClothe(): ClotheModel = ClotheModel(
    id = id,
    name = name,
    imageUrl = imageUrl.orEmpty(),
    category = category,
)
