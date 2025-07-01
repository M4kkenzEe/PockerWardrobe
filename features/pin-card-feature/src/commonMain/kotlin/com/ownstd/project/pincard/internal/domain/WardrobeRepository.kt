package com.ownstd.project.pincard.internal.domain

import androidx.compose.ui.graphics.ImageBitmap
import com.ownstd.project.pincard.internal.data.model.Clothe

internal interface WardrobeRepository {
    suspend fun getClothes(): List<Clothe>
    suspend fun loadClothe(bitmap: ImageBitmap)
}