package com.ownstd.project.pincard.internal.domain.usecase

import androidx.compose.ui.graphics.ImageBitmap
import com.ownstd.project.pincard.internal.data.model.Clothe
import com.ownstd.project.pincard.internal.domain.repository.WardrobeRepository

internal class WardrobeUseCase(private val wardrobeRepository: WardrobeRepository) {
    suspend fun getClothes(): List<Clothe> {
        return wardrobeRepository.getClothes()
    }

    suspend fun loadClothe(bitmap: ImageBitmap) {
        wardrobeRepository.loadClothe(bitmap)
    }

    suspend fun uploadFromUrl(pageUrl: String): Clothe {
        return wardrobeRepository.uploadFromUrl(pageUrl)
    }

    suspend fun delete(clotheId: Int) {
        wardrobeRepository.deleteClothe(clotheId)
    }
}