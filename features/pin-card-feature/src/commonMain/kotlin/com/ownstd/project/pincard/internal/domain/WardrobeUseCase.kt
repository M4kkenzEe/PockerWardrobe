package com.ownstd.project.pincard.internal.domain

import androidx.compose.ui.graphics.ImageBitmap
import com.ownstd.project.pincard.internal.data.model.Clothe

internal class WardrobeUseCase(private val wardrobeRepository: WardrobeRepository) {
    suspend fun getClothes(): List<Clothe> {
        return wardrobeRepository.getClothes()
    }

    suspend fun loadClothe(bitmap: ImageBitmap) {
        wardrobeRepository.loadClothe(bitmap)
    }
}