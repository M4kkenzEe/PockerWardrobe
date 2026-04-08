package com.ownstd.project.wardrobe.internal.presentation.detail.itemEdit.interactionModel

sealed class ItemEditSideEffect {
    data object NavigateBack : ItemEditSideEffect()
    data class ShowError(val message: String) : ItemEditSideEffect()
}
