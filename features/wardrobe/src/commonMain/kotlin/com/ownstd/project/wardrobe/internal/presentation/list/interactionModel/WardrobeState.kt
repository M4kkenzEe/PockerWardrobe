package com.ownstd.project.wardrobe.internal.presentation.list.interactionModel

import com.ownstd.project.wardrobe.internal.domain.model.Clothe
import com.ownstd.project.wardrobe.internal.domain.model.FilterOptions
import pro.respawn.flowmvi.api.MVIState

data class WardrobeState(
    val clothes: List<Clothe> = emptyList(),
    val filteredClothes: List<Clothe> = emptyList(),
    val activeCategory: String? = null,
    val filterOptions: FilterOptions = FilterOptions(),
    val isLoading: Boolean = true,
    val isEmpty: Boolean = false,
    val isFilterActive: Boolean = false,
) : MVIState
