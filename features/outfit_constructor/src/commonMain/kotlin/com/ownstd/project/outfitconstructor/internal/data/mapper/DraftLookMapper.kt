package com.ownstd.project.outfitconstructor.internal.data.mapper

import com.ownstd.project.outfitconstructor.internal.data.dto.AddLookRequest
import com.ownstd.project.outfitconstructor.internal.domain.model.DraftLookModel

fun DraftLookModel.toRequest(): AddLookRequest = AddLookRequest(
    name = name,
    clotheIds = clotheIds,
)
