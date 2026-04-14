package com.ownstd.project.core.mockdata

import com.ownstd.project.wardrobe.internal.domain.model.ClotheModel
import com.ownstd.project.wardrobe.internal.domain.model.ClotheDetailModel
import com.ownstd.project.wardrobe.internal.domain.model.LookModel
import com.ownstd.project.wardrobe.internal.domain.repository.WardrobeRepository

internal class MockWardrobeRepository : WardrobeRepository {

    private val clothes = listOf(
        ClotheModel(
            id = 1, name = "Белая рубашка",
            imageUrl = "https://images.unsplash.com/photo-1658863492105-362ff4198a1a?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=400&w=400",
            category = "Верх", styles = listOf("Casual", "Минимализм"),
            season = listOf("Весна", "Лето"), color = "Белый", size = "S",
            tags = listOf("базовая", "офис"),
            marketplaceLinks = listOf("https://wildberries.ru/catalog/123"),
        ),
        ClotheModel(
            id = 2, name = "Чёрные брюки",
            imageUrl = "https://images.unsplash.com/photo-1718252540558-7b383b52642e?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=400&w=400",
            category = "Низ", styles = listOf("Деловой", "Классика"),
            season = listOf("Всесезонный"), color = "Чёрный", size = "S",
            tags = listOf("офис"),
            marketplaceLinks = emptyList(),
        ),
        ClotheModel(
            id = 3, name = "Бежевый тренч",
            imageUrl = "https://images.unsplash.com/photo-1684841565198-41e4887476f4?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=400&w=400",
            category = "Верхняя одежда", styles = listOf("Casual", "Элегантный"),
            season = listOf("Весна", "Осень"), color = "Бежевый", size = "S",
            tags = listOf("базовый"),
            marketplaceLinks = emptyList(),
        ),
        ClotheModel(
            id = 4, name = "Белые кеды",
            imageUrl = "https://images.unsplash.com/photo-1608380272894-b3617f04b463?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=400&w=400",
            category = "Обувь", styles = listOf("Casual", "Стритвир"),
            season = listOf("Весна", "Лето", "Осень"), color = "Белый", size = "37",
            tags = emptyList(),
            marketplaceLinks = emptyList(),
        ),
        ClotheModel(
            id = 5, name = "Серый свитер",
            imageUrl = "https://images.unsplash.com/photo-1762151796842-74bea162c6c0?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=400&w=400",
            category = "Верх", styles = listOf("Casual", "Минимализм"),
            season = listOf("Осень", "Зима"), color = "Серый", size = "S",
            tags = listOf("базовый", "тёплый"),
            marketplaceLinks = emptyList(),
        ),
        ClotheModel(
            id = 6, name = "Синие джинсы",
            imageUrl = "https://images.unsplash.com/photo-1708523842501-1619478cea1f?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=400&w=400",
            category = "Низ", styles = listOf("Casual", "Стритвир"),
            season = listOf("Всесезонный"), color = "Синий", size = "S",
            tags = listOf("базовые"),
            marketplaceLinks = emptyList(),
        ),
    )

    private val mockOutfits = listOf(
        LookModel(id = 1, name = "Осенний образ", imageUrl = "https://images.unsplash.com/photo-1654875124783-923e58c7bd3f?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=600&w=400"),
        LookModel(id = 2, name = "Офисный образ", imageUrl = "https://images.unsplash.com/photo-1701318227054-daa5d60a38fb?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=600&w=400"),
    )

    override suspend fun getClothes(): List<ClotheModel> = clothes

    override suspend fun getClotheById(clotheId: Int): ClotheDetailModel {
        val clothe = clothes.firstOrNull { it.id == clotheId } ?: clothes.first()
        return ClotheDetailModel(
            id = clothe.id,
            name = clothe.name,
            imageUrl = clothe.imageUrl,
            category = clothe.category,
            material = "Хлопок",
            fit = "Regular",
            styles = clothe.styles,
            season = clothe.season,
            color = clothe.color,
            brand = null,
            size = clothe.size,
            marketplaceLinks = clothe.marketplaceLinks,
            tags = clothe.tags,
            createdAt = "2026-03-12T10:00:00Z",
        )
    }

    override suspend fun updateClothe(clotheId: Int, clothe: ClotheDetailModel): ClotheDetailModel = clothe

    override suspend fun deleteClothe(id: Int) = Unit

    override suspend fun getClotheOutfits(clotheId: Int): List<LookModel> = mockOutfits

    override suspend fun uploadClothe(imageBytes: ByteArray): ClotheModel = clothes.first()

    override suspend fun uploadFromUrl(url: String): ClotheModel = clothes.first()
}
