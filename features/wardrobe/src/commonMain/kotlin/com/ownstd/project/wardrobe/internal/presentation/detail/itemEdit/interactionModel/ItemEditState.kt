package com.ownstd.project.wardrobe.internal.presentation.detail.itemEdit.interactionModel

import com.ownstd.project.wardrobe.internal.domain.model.ClotheDetail
import pro.respawn.flowmvi.api.MVIState

data class ItemEditState(
    val clothe: ClotheDetail? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
) : MVIState
