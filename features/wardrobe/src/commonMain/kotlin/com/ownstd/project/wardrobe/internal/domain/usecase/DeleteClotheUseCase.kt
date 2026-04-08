package com.ownstd.project.wardrobe.internal.domain.usecase

import com.ownstd.project.wardrobe.internal.domain.repository.WardrobeRepository

class DeleteClotheUseCase(private val repository: WardrobeRepository) {
    suspend operator fun invoke(id: Int): Result<Unit> =
        runCatching { repository.deleteClothe(id) }
}
