package com.ownstd.project.wardrobe.internal.domain.repository

import com.ownstd.project.wardrobe.internal.domain.model.Clothe
import com.ownstd.project.wardrobe.internal.domain.model.ClotheDetail
import com.ownstd.project.wardrobe.internal.domain.model.Look

interface WardrobeRepository {
    suspend fun getClothes(): List<Clothe>
    suspend fun getClotheById(clotheId: Int): ClotheDetail
    suspend fun updateClothe(clotheId: Int, clothe: ClotheDetail): ClotheDetail
    suspend fun deleteClothe(id: Int)
    suspend fun getClotheOutfits(clotheId: Int): List<Look>
    suspend fun uploadClothe(imageBytes: ByteArray): Clothe
    suspend fun uploadFromUrl(url: String): Clothe
}
