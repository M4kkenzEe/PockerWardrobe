package com.ownstd.project.pincard.internal.domain

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

internal class WardrobeRefreshSignal {
    private val _flow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val flow: SharedFlow<Unit> = _flow.asSharedFlow()

    fun emit() {
        _flow.tryEmit(Unit)
    }
}
