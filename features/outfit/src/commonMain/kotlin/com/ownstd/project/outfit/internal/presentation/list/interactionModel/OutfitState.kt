package com.ownstd.project.outfit.internal.presentation.list.interactionModel

import com.ownstd.project.outfit.internal.domain.model.LookModel
import pro.respawn.flowmvi.api.MVIState

data class OutfitState(
    val looks: List<LookModel> = emptyList(),
    val activeStyle: String? = null,
    val isLoading: Boolean = true,
    val isEmpty: Boolean = false,
    val isGenerating: Boolean = false,
) : MVIState
