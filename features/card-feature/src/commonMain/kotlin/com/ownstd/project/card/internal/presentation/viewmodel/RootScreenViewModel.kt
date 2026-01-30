package com.ownstd.project.card.internal.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.ownstd.project.card.internal.presentation.ViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class RootScreenViewModel : ViewModel() {
    private val _viewState = MutableStateFlow(ViewState.SIGNING)
    val viewState: StateFlow<ViewState> = _viewState.asStateFlow()

    fun openSession() {
        _viewState.value = ViewState.SESSION
    }
}