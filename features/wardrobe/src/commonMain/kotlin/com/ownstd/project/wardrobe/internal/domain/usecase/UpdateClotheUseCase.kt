package com.ownstd.project.wardrobe.internal.domain.usecase

import com.ownstd.project.wardrobe.internal.domain.model.ClotheDetail
import com.ownstd.project.wardrobe.internal.domain.repository.WardrobeRepository

class UpdateClotheUseCase(private val repository: WardrobeRepository) {
    suspend operator fun invoke(clotheId: Int, clothe: ClotheDetail): Result<ClotheDetail> =
        runCatching { repository.updateClothe(clotheId, clothe) }
}
