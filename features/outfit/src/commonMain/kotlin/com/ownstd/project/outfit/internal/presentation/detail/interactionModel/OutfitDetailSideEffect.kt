package com.ownstd.project.outfit.internal.presentation.detail.interactionModel

sealed class OutfitDetailSideEffect {
    data class NavigateToConstructor(val lookId: Int) : OutfitDetailSideEffect()
    data class NavigateToItem(val clotheId: Int) : OutfitDetailSideEffect()
    data object NavigateBack : OutfitDetailSideEffect()
    data class ShowError(val message: String) : OutfitDetailSideEffect()
}
