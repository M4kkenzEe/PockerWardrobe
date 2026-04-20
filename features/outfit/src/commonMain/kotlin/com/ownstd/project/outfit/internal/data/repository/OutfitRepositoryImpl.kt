package com.ownstd.project.outfit.internal.data.repository

import com.ownstd.project.outfit.internal.data.api.OutfitApi
import com.ownstd.project.outfit.internal.data.mapper.toLook
import com.ownstd.project.outfit.internal.domain.model.DraftLookModel
import com.ownstd.project.outfit.internal.domain.model.LookModel
import com.ownstd.project.outfit.internal.domain.repository.OutfitRepository

class OutfitRepositoryImpl(
    private val api: OutfitApi,
) : OutfitRepository {

    override suspend fun getLooks(): List<LookModel> =
        api.getLooks().map { it.toLook() }

    override suspend fun getLookById(id: Int): LookModel =
        api.getLookById(id).toLook()

    override suspend fun deleteLook(id: Int) {
        api.deleteLook(id)
    }

    override suspend fun shareLook(id: Int): String =
        api.shareLook(id).url

    override suspend fun addLook(look: DraftLookModel, image: ByteArray): LookModel {
        // TODO: реализуется в OutfitConstructor (шаг [10])
        throw UnsupportedOperationException("addLook реализуется в features/outfit_constructor")
    }
}
