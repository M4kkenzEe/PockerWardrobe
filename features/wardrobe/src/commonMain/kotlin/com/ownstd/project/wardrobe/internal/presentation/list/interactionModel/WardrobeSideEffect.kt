package com.ownstd.project.wardrobe.internal.presentation.list.interactionModel

sealed class WardrobeSideEffect {
    data class NavigateToDetail(val clotheId: Int) : WardrobeSideEffect()
    data object ShowAddBottomSheet : WardrobeSideEffect()
    data object ShowFilterBottomSheet : WardrobeSideEffect()
    data class ShowItemMenu(val clotheId: Int) : WardrobeSideEffect()
    data class ShowError(val message: String) : WardrobeSideEffect()
}
