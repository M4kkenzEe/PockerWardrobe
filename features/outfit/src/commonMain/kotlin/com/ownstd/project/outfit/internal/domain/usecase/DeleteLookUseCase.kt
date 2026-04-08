package com.ownstd.project.outfit.internal.domain.usecase

import com.ownstd.project.outfit.internal.domain.repository.OutfitRepository

class DeleteLookUseCase(private val repository: OutfitRepository) {
    suspend operator fun invoke(id: Int): Result<Unit> =
        runCatching { repository.deleteLook(id) }
}
