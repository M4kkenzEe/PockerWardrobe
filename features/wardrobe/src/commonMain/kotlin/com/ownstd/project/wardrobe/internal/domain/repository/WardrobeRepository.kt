package com.ownstd.project.wardrobe.internal.domain.repository

import com.ownstd.project.wardrobe.internal.domain.model.ClotheModel
import com.ownstd.project.wardrobe.internal.domain.model.ClotheDetailModel
import com.ownstd.project.wardrobe.internal.domain.model.LookModel

interface WardrobeRepository {
    suspend fun getClothes(): List<ClotheModel>
    suspend fun getClotheById(clotheId: Int): ClotheDetailModel
    suspend fun updateClothe(clotheId: Int, clothe: ClotheDetailModel): ClotheDetailModel
    suspend fun deleteClothe(id: Int)
    suspend fun getClotheOutfits(clotheId: Int): List<LookModel>
    suspend fun uploadClothe(imageBytes: ByteArray): ClotheModel
    suspend fun uploadFromUrl(url: String): ClotheModel
}
