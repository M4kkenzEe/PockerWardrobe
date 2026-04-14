package com.ownstd.project.profile.internal.data.mapper

import com.ownstd.project.profile.internal.data.dto.UserSizesDto
import com.ownstd.project.profile.internal.domain.model.UserSizesModel

internal fun UserSizesDto.toUserSizes(): UserSizesModel = UserSizesModel(
    primarySize = primarySize,
    height = height,
    weight = weight,
    chest = chest,
    waist = waist,
    hips = hips,
)

internal fun UserSizesModel.toDto(): UserSizesDto = UserSizesDto(
    primarySize = primarySize,
    height = height,
    weight = weight,
    chest = chest,
    waist = waist,
    hips = hips,
)
