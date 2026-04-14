package com.ownstd.project.outfit.internal.presentation.detail.interactionModel

import com.ownstd.project.outfit.internal.domain.model.LookModel
import pro.respawn.flowmvi.api.MVIState

data class OutfitDetailState(
    val look: LookModel? = null,
    val isLoading: Boolean = true,
    val showDeleteConfirm: Boolean = false,
) : MVIState
