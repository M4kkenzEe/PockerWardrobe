package com.ownstd.project.core.mockdata

import com.ownstd.project.outfitconstructor.internal.domain.model.ClotheModel
import com.ownstd.project.outfitconstructor.internal.domain.model.DraftLookModel
import com.ownstd.project.outfitconstructor.internal.domain.repository.OutfitConstructorRepository

internal class MockOutfitConstructorRepository : OutfitConstructorRepository {

    private val clothes = listOf(
        ClotheModel(id = 1, name = "Белая рубашка",    imageUrl = "", category = "Верх"),
        ClotheModel(id = 2, name = "Чёрные брюки",     imageUrl = "", category = "Низ"),
        ClotheModel(id = 3, name = "Бежевый тренч",    imageUrl = "", category = "Верхняя одежда"),
        ClotheModel(id = 4, name = "Белые кеды",       imageUrl = "", category = "Обувь"),
        ClotheModel(id = 5, name = "Серый свитер",     imageUrl = "", category = "Верх"),
        ClotheModel(id = 6, name = "Синие джинсы",     imageUrl = "", category = "Низ"),
    )

    override suspend fun getClothes(): List<ClotheModel> = clothes

    override suspend fun addLook(look: DraftLookModel, image: ByteArray) = Unit
}
