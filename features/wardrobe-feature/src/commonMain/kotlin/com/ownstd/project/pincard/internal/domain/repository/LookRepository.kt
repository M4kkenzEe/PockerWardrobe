package com.ownstd.project.pincard.internal.domain.repository

import com.ownstd.project.pincard.internal.data.model.DraftLook
import com.ownstd.project.pincard.internal.data.model.Look
import com.ownstd.project.pincard.internal.data.model.LookRepositoryResult
import com.ownstd.project.pincard.internal.data.model.ShareResponse

internal interface LookRepository {
    suspend fun getLooks(): List<Look>
    suspend fun addLook(look: DraftLook, image: ByteArray): LookRepositoryResult<Unit>
    suspend fun getLookById(lookId: Int): Look?
    suspend fun getLookByShareToken(token: String): Look?
    suspend fun addLookByShareToken(sharedToken: String)
    suspend fun deleteLook(lookId: Int)
    suspend fun shareLook(lookId: Int): ShareResponse?
}