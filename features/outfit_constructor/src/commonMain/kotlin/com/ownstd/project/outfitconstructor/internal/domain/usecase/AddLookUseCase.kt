package com.ownstd.project.outfitconstructor.internal.domain.usecase

import com.ownstd.project.outfitconstructor.internal.domain.model.DraftLookModel
import com.ownstd.project.outfitconstructor.internal.domain.repository.OutfitConstructorRepository

class AddLookUseCase(private val repository: OutfitConstructorRepository) {
    suspend operator fun invoke(look: DraftLookModel, image: ByteArray): Result<Unit> =
        runCatching { repository.addLook(look, image) }
}
