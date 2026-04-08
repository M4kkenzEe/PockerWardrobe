package com.ownstd.project.outfitconstructor.internal.data.mapper

import com.ownstd.project.outfitconstructor.internal.data.dto.AddLookRequest
import com.ownstd.project.outfitconstructor.internal.domain.model.DraftLook

fun DraftLook.toRequest(): AddLookRequest = AddLookRequest(
    name = name,
    clotheIds = clotheIds,
)
