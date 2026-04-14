package com.ownstd.project.wardrobe.internal.domain.usecase

import com.ownstd.project.wardrobe.internal.domain.model.ClotheDetailModel
import com.ownstd.project.wardrobe.internal.domain.repository.WardrobeRepository

class UpdateClotheUseCase(private val repository: WardrobeRepository) {
    suspend operator fun invoke(clotheId: Int, clothe: ClotheDetailModel): Result<ClotheDetailModel> =
        runCatching { repository.updateClothe(clotheId, clothe) }
}
