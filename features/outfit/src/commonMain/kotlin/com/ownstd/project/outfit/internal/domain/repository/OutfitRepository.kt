package com.ownstd.project.outfit.internal.domain.repository

import com.ownstd.project.outfit.internal.domain.model.DraftLookModel
import com.ownstd.project.outfit.internal.domain.model.LookModel

interface OutfitRepository {
    suspend fun getLooks(): List<LookModel>
    suspend fun getLookById(id: Int): LookModel
    suspend fun deleteLook(id: Int)
    suspend fun shareLook(id: Int): String
    suspend fun addLook(look: DraftLookModel, image: ByteArray): LookModel
}
