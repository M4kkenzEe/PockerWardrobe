package com.ownstd.project.wardrobe.internal.presentation.detail.itemDetail.interactionModel

sealed class ItemDetailSideEffect {
    data class NavigateToEdit(val clotheId: Int) : ItemDetailSideEffect()
    data class NavigateToLook(val lookId: Int) : ItemDetailSideEffect()
    data class OpenUrl(val url: String) : ItemDetailSideEffect()
    data object NavigateBack : ItemDetailSideEffect()
    data class ShowError(val message: String) : ItemDetailSideEffect()
}
