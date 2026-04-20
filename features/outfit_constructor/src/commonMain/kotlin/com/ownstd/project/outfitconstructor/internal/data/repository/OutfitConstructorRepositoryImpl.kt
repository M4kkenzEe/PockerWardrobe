package com.ownstd.project.outfitconstructor.internal.data.repository

import com.ownstd.project.outfitconstructor.internal.data.api.OutfitConstructorApi
import com.ownstd.project.outfitconstructor.internal.data.mapper.toClothe
import com.ownstd.project.outfitconstructor.internal.data.mapper.toRequest
import com.ownstd.project.outfitconstructor.internal.domain.model.ClotheModel
import com.ownstd.project.outfitconstructor.internal.domain.model.DraftLookModel
import com.ownstd.project.outfitconstructor.internal.domain.repository.OutfitConstructorRepository

class OutfitConstructorRepositoryImpl(
    private val api: OutfitConstructorApi,
) : OutfitConstructorRepository {

    override suspend fun getClothes(): List<ClotheModel> =
        api.getClothes().map { it.toClothe() }

    override suspend fun addLook(look: DraftLookModel, image: ByteArray) {
        api.addLook(look.toRequest(), image)
    }
}
