package com.ownstd.project.wardrobe.internal.presentation.detail.itemDetail.interactionModel

import com.ownstd.project.wardrobe.internal.domain.model.ClotheDetail
import com.ownstd.project.wardrobe.internal.domain.model.Look
import pro.respawn.flowmvi.api.MVIState

data class ItemDetailState(
    val clothe: ClotheDetail? = null,
    val relatedLooks: List<Look> = emptyList(),
    val isLoading: Boolean = true,
    val showDeleteConfirm: Boolean = false,
) : MVIState
