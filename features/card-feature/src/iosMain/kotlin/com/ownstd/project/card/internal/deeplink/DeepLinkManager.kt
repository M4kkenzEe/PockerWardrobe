package com.ownstd.project.card.internal.deeplink

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Get the singleton instance of DeepLinkManager for iOS
 */
actual fun getDeepLinkManager(): DeepLinkManager = DeepLinkManager.getInstance()

/**
 * iOS implementation of DeepLinkManager
 * Singleton instance for managing deep links
 */
actual class DeepLinkManager {
    private val _deepLinkFlow = MutableStateFlow<DeepLink?>(null)
    actual val deepLinkFlow: StateFlow<DeepLink?> = _deepLinkFlow.asStateFlow()

    private val _pendingDeepLink = MutableStateFlow<DeepLink?>(null)
    actual val pendingDeepLink: StateFlow<DeepLink?> = _pendingDeepLink.asStateFlow()

    companion object {
        @Volatile
        private var instance: DeepLinkManager? = null

        fun getInstance(): DeepLinkManager {
            return instance ?: synchronized(this) {
                instance ?: DeepLinkManager().also { instance = it }
            }
        }
    }

    /**
     * Parse and handle a deep link URL
     * Supports URLs like: http://pocketwardrobe/share/{uuid}
     * TODO: Implement iOS URL parsing when iOS deep links are configured
     */
    actual fun handleDeepLinkUrl(url: String): Boolean {
        // iOS implementation will be added when iOS deep link handling is set up
        println("iOS DeepLinkManager: handleDeepLinkUrl called with $url")
        return false
    }

    actual fun clearDeepLink() {
        println("iOS DeepLinkManager: clearDeepLink called")
        _deepLinkFlow.value = null
    }

    actual fun setPendingDeepLink(deepLink: DeepLink?) {
        println("iOS DeepLinkManager: Setting pending deep link: $deepLink")
        _pendingDeepLink.value = deepLink
    }

    actual fun applyPendingDeepLink() {
        val pending = _pendingDeepLink.value
        if (pending != null) {
            println("iOS DeepLinkManager: Applying pending deep link: $pending")
            _deepLinkFlow.value = pending
            _pendingDeepLink.value = null
        } else {
            println("iOS DeepLinkManager: No pending deep link to apply")
        }
    }
}
