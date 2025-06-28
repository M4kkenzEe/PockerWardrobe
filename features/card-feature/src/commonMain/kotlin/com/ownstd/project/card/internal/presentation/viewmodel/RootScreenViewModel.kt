package com.ownstd.project.card.internal.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.ownstd.project.card.internal.presentation.ViewState
import kotlinx.coroutines.flow.MutableStateFlow

internal class RootScreenViewModel : ViewModel() {
    val viewState = MutableStateFlow(ViewState.SIGNING)

    fun openSession() {
        viewState.value = ViewState.SESSION
    }
}