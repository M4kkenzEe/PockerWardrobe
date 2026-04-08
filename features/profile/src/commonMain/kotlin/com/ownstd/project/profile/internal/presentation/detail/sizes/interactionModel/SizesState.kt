package com.ownstd.project.profile.internal.presentation.detail.sizes.interactionModel

import com.ownstd.project.profile.internal.domain.model.UserSizes
import pro.respawn.flowmvi.api.MVIState

enum class SizeRegion { RU, EU, US, JP, KR, CN }

enum class SizeField { HEIGHT, WEIGHT, CHEST, WAIST, HIPS }

data class SizesState(
    val sizes: UserSizes? = null,
    val activeRegion: SizeRegion = SizeRegion.RU,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val editingField: SizeField? = null,
) : MVIState
