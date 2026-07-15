package com.ownstd.project.pincard.internal.domain.usecase

import androidx.compose.ui.graphics.ImageBitmap
import com.ownstd.project.pincard.internal.data.model.Clothe
import com.ownstd.project.pincard.internal.domain.repository.WardrobeRepository

internal class WardrobeUseCase(private val wardrobeRepository: WardrobeRepository) {
    suspend fun getClothes(occasion: String? = null): List<Clothe> {
        return wardrobeRepository.getClothes(occasion)
    }

    suspend fun loadClothe(bitmap: ImageBitmap, occasion: String? = null) {
        wardrobeRepository.loadClothe(bitmap, occasion)
    }

    suspend fun uploadFromUrl(pageUrl: String): Clothe {
        return wardrobeRepository.uploadFromUrl(pageUrl)
    }

    suspend fun delete(clotheId: Int) {
        wardrobeRepository.deleteClothe(clotheId)
    }

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
    ): Clothe {
        return wardrobeRepository.updateClothe(clotheId, name, storeUrl, season, fit, material, brand, occasion, styleTags)
    }

    suspend fun saveToWardrobe(clotheId: Int): Clothe {
        return wardrobeRepository.saveToWardrobe(clotheId)
    }
}