package com.ownstd.project.profile.internal.data.mapper

import com.ownstd.project.profile.internal.data.dto.UserSizesDto
import com.ownstd.project.profile.internal.domain.model.UserSizes

internal fun UserSizesDto.toUserSizes(): UserSizes = UserSizes(
    primarySize = primarySize,
    height = height,
    weight = weight,
    chest = chest,
    waist = waist,
    hips = hips,
)

internal fun UserSizes.toDto(): UserSizesDto = UserSizesDto(
    primarySize = primarySize,
    height = height,
    weight = weight,
    chest = chest,
    waist = waist,
    hips = hips,
)
