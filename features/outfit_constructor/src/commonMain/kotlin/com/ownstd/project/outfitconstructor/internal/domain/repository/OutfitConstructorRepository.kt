package com.ownstd.project.outfitconstructor.internal.domain.repository

import com.ownstd.project.outfitconstructor.internal.domain.model.ClotheModel
import com.ownstd.project.outfitconstructor.internal.domain.model.DraftLookModel

interface OutfitConstructorRepository {
    suspend fun getClothes(): List<ClotheModel>
    suspend fun addLook(look: DraftLookModel, image: ByteArray)
}
