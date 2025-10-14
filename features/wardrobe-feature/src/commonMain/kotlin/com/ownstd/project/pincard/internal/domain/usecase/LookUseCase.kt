package com.ownstd.project.pincard.internal.domain.usecase

import com.ownstd.project.pincard.internal.data.model.DraftLook
import com.ownstd.project.pincard.internal.data.model.Look
import com.ownstd.project.pincard.internal.domain.repository.LookRepository

internal class LookUseCase(private val lookRepository: LookRepository) {
    suspend fun getLooks(): List<Look> {
        return lookRepository.getLooks()
    }

    suspend fun addLook(look: DraftLook, image: ByteArray) {
        lookRepository.addLook(look, image)
    }

    suspend fun getLookById(lookId: Int): Look? {
        return lookRepository.getLookById(lookId)
    }
}