package com.ownstd.project.pincard.internal.domain.usecase

import com.ownstd.project.pincard.internal.data.model.Look
import com.ownstd.project.pincard.internal.data.model.LookResponse
import com.ownstd.project.pincard.internal.domain.repository.LookRepository

internal class LookUseCase(private val lookRepository: LookRepository) {
    suspend fun getLooks(): List<LookResponse> {
        return lookRepository.getLooks()
    }

    suspend fun addLook(look : Look, image: ByteArray) {
        lookRepository.addLook(look, image)
    }
}