package com.ownstd.project.core.deeplink

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object DeepLinkManager {
    private val _pendingDeeplink = MutableStateFlow<String?>(null)
    val pendingDeeplink: StateFlow<String?> = _pendingDeeplink

    fun emit(uri: String) {
        _pendingDeeplink.tryEmit(uri)
    }

    fun onHandled() {
        _pendingDeeplink.tryEmit(null)
    }
}
