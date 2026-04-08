package com.ownstd.project.profile.internal.presentation.detail.sizes.interactionModel

import pro.respawn.flowmvi.api.MVIIntent

sealed interface SizesIntent : MVIIntent {
    data object Load : SizesIntent
    data class SelectRegion(val region: SizeRegion) : SizesIntent
    data class EditField(val field: SizeField) : SizesIntent
    data class SaveField(val field: SizeField, val value: Float) : SizesIntent
    data object DismissEdit : SizesIntent
}
