package com.ownstd.project.outfit.internal.domain.repository

import com.ownstd.project.outfit.internal.domain.model.DraftLook
import com.ownstd.project.outfit.internal.domain.model.Look

interface OutfitRepository {
    suspend fun getLooks(): List<Look>
    suspend fun getLookById(id: Int): Look
    suspend fun deleteLook(id: Int)
    suspend fun shareLook(id: Int): String
    suspend fun addLook(look: DraftLook, image: ByteArray): Look
}
