package com.ownstd.project.pincard.internal.domain.repository

import androidx.compose.ui.graphics.ImageBitmap
import com.ownstd.project.pincard.internal.data.model.Clothe

internal interface WardrobeRepository {
    suspend fun getClothes(occasion: String? = null): List<Clothe>
    suspend fun loadClothe(bitmap: ImageBitmap, occasion: String? = null)
    suspend fun uploadFromUrl(pageUrl: String): Clothe
    suspend fun deleteClothe(clotheId: Int)
    suspend fun updateClothe(
        clotheId: Int,
        name: String? = null,
        storeUrl: String? = null,
        season: String? = null,
        fit: String? = null,
        material: String? = null,
        brand: String? = null,
        occasion: String? = null,
        styleTags: String? = null,
    ): Clothe
}