package com.ownstd.project.wardrobe.internal.presentation.detail.itemDetail.interactionModel

import com.ownstd.project.wardrobe.internal.domain.model.ClotheDetailModel
import com.ownstd.project.wardrobe.internal.domain.model.LookModel
import pro.respawn.flowmvi.api.MVIState

data class ItemDetailState(
    val clothe: ClotheDetailModel? = null,
    val relatedLooks: List<LookModel> = emptyList(),
    val isLoading: Boolean = true,
    val showDeleteConfirm: Boolean = false,
) : MVIState
