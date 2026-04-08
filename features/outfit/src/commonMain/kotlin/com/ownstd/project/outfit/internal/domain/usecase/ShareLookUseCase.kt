package com.ownstd.project.outfit.internal.domain.usecase

import com.ownstd.project.outfit.internal.domain.repository.OutfitRepository

class ShareLookUseCase(private val repository: OutfitRepository) {
    suspend operator fun invoke(id: Int): Result<String> =
        runCatching { repository.shareLook(id) }
}
