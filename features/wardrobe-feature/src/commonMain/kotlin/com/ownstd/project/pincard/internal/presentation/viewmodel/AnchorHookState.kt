package com.ownstd.project.pincard.internal.presentation.viewmodel

import com.ownstd.project.pincard.internal.data.model.MissingCategory

internal sealed class AnchorHookState {
    data object Hidden : AnchorHookState()
    data class Celebrate(val clotheId: Int, val imageUrl: String? = null) : AnchorHookState()
    data class Generating(val clotheId: Int) : AnchorHookState()
    data class NeedMore(val missing: List<MissingCategory>) : AnchorHookState()
    data object AllSaved : AnchorHookState()
    data object Error : AnchorHookState()
}
