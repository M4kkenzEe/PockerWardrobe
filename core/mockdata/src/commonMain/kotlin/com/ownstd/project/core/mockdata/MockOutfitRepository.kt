package com.ownstd.project.core.mockdata

import com.ownstd.project.outfit.internal.domain.model.DraftLookModel
import com.ownstd.project.outfit.internal.domain.model.LookModel
import com.ownstd.project.outfit.internal.domain.repository.OutfitRepository

internal class MockOutfitRepository : OutfitRepository {

    private val looks = listOf(
        LookModel(
            id = 1, name = "Осенний образ",
            url = "https://images.unsplash.com/photo-1654875124783-923e58c7bd3f?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=600&w=400",
            lookItems = emptyList(),
            style = "Casual",
            tags = listOf("Пальто", "Брюки", "Кеды"),
        ),
        LookModel(
            id = 2, name = "Деловой образ",
            url = "https://images.unsplash.com/photo-1701318227054-daa5d60a38fb?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=600&w=400",
            lookItems = emptyList(),
            style = "Деловой",
            tags = listOf("Пиджак", "Брюки"),
        ),
        LookModel(
            id = 3, name = "Повседневный",
            url = "https://images.unsplash.com/photo-1598554889165-8139a49f2883?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=600&w=400",
            lookItems = emptyList(),
            style = "Минимализм",
            tags = listOf("Свитер", "Джинсы", "Кеды"),
        ),
    )

    override suspend fun getLooks(): List<LookModel> = looks

    override suspend fun getLookById(id: Int): LookModel =
        looks.firstOrNull { it.id == id } ?: looks.first()

    override suspend fun deleteLook(id: Int) = Unit

    override suspend fun shareLook(id: Int): String = "https://pocketwardrobe/share/mock-$id"

    override suspend fun addLook(look: DraftLookModel, image: ByteArray): LookModel = looks.first()
}
