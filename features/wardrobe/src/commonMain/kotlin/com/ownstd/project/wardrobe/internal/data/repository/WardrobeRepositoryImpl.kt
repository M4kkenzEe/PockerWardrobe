package com.ownstd.project.wardrobe.internal.data.repository

import com.ownstd.project.wardrobe.internal.data.api.WardrobeApi
import com.ownstd.project.wardrobe.internal.data.mapper.toClothe
import com.ownstd.project.wardrobe.internal.data.mapper.toClotheDetail
import com.ownstd.project.wardrobe.internal.data.mapper.toLook
import com.ownstd.project.wardrobe.internal.data.mapper.toUpdateRequest
import com.ownstd.project.wardrobe.internal.domain.model.ClotheModel
import com.ownstd.project.wardrobe.internal.domain.model.ClotheDetailModel
import com.ownstd.project.wardrobe.internal.domain.model.LookModel
import com.ownstd.project.wardrobe.internal.domain.repository.WardrobeRepository

class WardrobeRepositoryImpl(
    private val api: WardrobeApi,
) : WardrobeRepository {

    override suspend fun getClothes(): List<ClotheModel> =
        api.getClothes().map { it.toClothe() }

    override suspend fun getClotheById(clotheId: Int): ClotheDetailModel =
        api.getClotheById(clotheId).toClotheDetail()

    override suspend fun updateClothe(clotheId: Int, clothe: ClotheDetailModel): ClotheDetailModel =
        api.updateClothe(clotheId, clothe.toUpdateRequest()).toClotheDetail()

    override suspend fun deleteClothe(id: Int) {
        api.deleteClothe(id)
    }

    override suspend fun getClotheOutfits(clotheId: Int): List<LookModel> =
        api.getClotheOutfits(clotheId).map { it.toLook() }

    override suspend fun uploadClothe(imageBytes: ByteArray): ClotheModel =
        api.uploadClothe(imageBytes).toClothe()

    override suspend fun uploadFromUrl(url: String): ClotheModel =
        api.uploadFromUrl(url).toClothe()
}
