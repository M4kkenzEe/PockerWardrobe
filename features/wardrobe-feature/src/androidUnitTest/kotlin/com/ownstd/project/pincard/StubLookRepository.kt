package com.ownstd.project.pincard

import com.ownstd.project.pincard.internal.data.model.DraftLook
import com.ownstd.project.pincard.internal.data.model.GenerateDraftsResult
import com.ownstd.project.pincard.internal.data.model.GenerateLooksResult
import com.ownstd.project.pincard.internal.data.model.Look
import com.ownstd.project.pincard.internal.data.model.LookRepositoryResult
import com.ownstd.project.pincard.internal.data.model.ShareResponse
import com.ownstd.project.pincard.internal.domain.repository.LookRepository

internal open class StubLookRepository : LookRepository {
    override suspend fun getLooks(): List<Look> = error("unused")
    override suspend fun addLook(look: DraftLook, image: ByteArray): LookRepositoryResult<Unit> = error("unused")
    override suspend fun getLookById(lookId: Int): Look? = error("unused")
    override suspend fun getLookByShareToken(token: String): Look? = error("unused")
    override suspend fun addLookByShareToken(sharedToken: String) = error("unused")
    override suspend fun addClotheByShareToken(shareToken: String, clotheId: Int) = error("unused")
    override suspend fun deleteLook(lookId: Int) = error("unused")
    override suspend fun saveDraft(lookId: Int) = error("unused")
    override suspend fun generateDrafts(clotheId: Int): GenerateDraftsResult = error("unused")
    override suspend fun shareLook(lookId: Int): ShareResponse? = error("unused")
    override suspend fun generateLooks(): GenerateLooksResult = error("unused")
    override suspend fun getAffiliateLink(storeUrl: String): String? = error("unused")
}
