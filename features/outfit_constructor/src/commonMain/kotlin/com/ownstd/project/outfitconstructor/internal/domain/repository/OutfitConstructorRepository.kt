package com.ownstd.project.outfitconstructor.internal.domain.repository

import com.ownstd.project.outfitconstructor.internal.domain.model.Clothe
import com.ownstd.project.outfitconstructor.internal.domain.model.DraftLook

interface OutfitConstructorRepository {
    suspend fun getClothes(): List<Clothe>
    suspend fun addLook(look: DraftLook, image: ByteArray)
}
