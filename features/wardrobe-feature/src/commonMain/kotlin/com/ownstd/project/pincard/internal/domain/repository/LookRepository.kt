package com.ownstd.project.pincard.internal.domain.repository

import com.ownstd.project.pincard.internal.data.model.Look
import com.ownstd.project.pincard.internal.data.model.LookResponse
import com.ownstd.project.pincard.internal.data.model.LookRepositoryResult

internal interface LookRepository {
    suspend fun getLooks(): List<LookResponse>
    suspend fun addLook(look: Look, image: ByteArray): LookRepositoryResult<Unit>
}