package com.ownstd.project.outfitconstructor.internal.data.mapper

import com.ownstd.project.outfitconstructor.internal.data.dto.ClotheDto
import com.ownstd.project.outfitconstructor.internal.domain.model.Clothe

fun ClotheDto.toClothe(): Clothe = Clothe(
    id = id,
    name = name,
    imageUrl = imageUrl,
    category = category,
)
