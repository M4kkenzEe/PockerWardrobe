package com.ownstd.project.wardrobe.internal.data.repository

import com.ownstd.project.wardrobe.internal.data.api.WardrobeApi
import com.ownstd.project.wardrobe.internal.data.mapper.toClothe
import com.ownstd.project.wardrobe.internal.data.mapper.toClotheDetail
import com.ownstd.project.wardrobe.internal.data.mapper.toLook
import com.ownstd.project.wardrobe.internal.data.mapper.toUpdateRequest
import com.ownstd.project.wardrobe.internal.domain.model.Clothe
import com.ownstd.project.wardrobe.internal.domain.model.ClotheDetail
import com.ownstd.project.wardrobe.internal.domain.model.Look
import com.ownstd.project.wardrobe.internal.domain.repository.WardrobeRepository

class WardrobeRepositoryImpl(
    private val api: WardrobeApi,
) : WardrobeRepository {

    override suspend fun getClothes(): List<Clothe> =
        api.getClothes().map { it.toClothe() }

    override suspend fun getClotheById(clotheId: Int): ClotheDetail =
        api.getClotheById(clotheId).toClotheDetail()

    override suspend fun updateClothe(clotheId: Int, clothe: ClotheDetail): ClotheDetail =
        api.updateClothe(clotheId, clothe.toUpdateRequest()).toClotheDetail()

    override suspend fun deleteClothe(id: Int) {
        api.deleteClothe(id)
    }

    override suspend fun getClotheOutfits(clotheId: Int): List<Look> =
        api.getClotheOutfits(clotheId).map { it.toLook() }

    override suspend fun uploadClothe(imageBytes: ByteArray): Clothe =
        api.uploadClothe(imageBytes).toClothe()

    override suspend fun uploadFromUrl(url: String): Clothe =
        api.uploadFromUrl(url).toClothe()
}
