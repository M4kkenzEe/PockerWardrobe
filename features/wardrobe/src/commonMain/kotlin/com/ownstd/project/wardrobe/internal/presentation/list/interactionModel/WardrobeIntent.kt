package com.ownstd.project.wardrobe.internal.presentation.list.interactionModel

import com.ownstd.project.wardrobe.internal.domain.model.FilterOptions
import pro.respawn.flowmvi.api.MVIIntent

sealed interface WardrobeIntent : MVIIntent {
    data object LoadClothes : WardrobeIntent
    data class SelectCategory(val category: String?) : WardrobeIntent
    data class ApplyFilter(val options: FilterOptions) : WardrobeIntent
    data class DeleteClothe(val id: Int) : WardrobeIntent
    data class ItemClicked(val clotheId: Int) : WardrobeIntent
    data class ItemLongClicked(val clotheId: Int) : WardrobeIntent
    data object FabClicked : WardrobeIntent
    data object FilterClicked : WardrobeIntent
}
