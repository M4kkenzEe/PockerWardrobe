package com.ownstd.project.wardrobe.internal.presentation.list.interactionModel

import com.ownstd.project.wardrobe.internal.domain.model.ClotheModel
import com.ownstd.project.wardrobe.internal.domain.model.FilterOptionsModel
import pro.respawn.flowmvi.api.MVIState

data class WardrobeState(
    val clothes: List<ClotheModel> = emptyList(),
    val filteredClothes: List<ClotheModel> = emptyList(),
    val activeCategory: String? = null,
    val filterOptions: FilterOptionsModel = FilterOptionsModel(),
    val isLoading: Boolean = true,
    val isEmpty: Boolean = false,
    val isFilterActive: Boolean = false,
) : MVIState
