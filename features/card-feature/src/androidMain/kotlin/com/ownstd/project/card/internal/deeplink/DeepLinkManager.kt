package com.ownstd.project.card.internal.deeplink

import android.net.Uri
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Get the singleton instance of DeepLinkManager for Android
 */
actual fun getDeepLinkManager(): DeepLinkManager = DeepLinkManager.getInstance()

/**
 * Android implementation of DeepLinkManager
 * Singleton instance for managing deep links
 */
actual class DeepLinkManager {
    private val _deepLinkFlow = MutableStateFlow<DeepLink?>(null)
    actual val deepLinkFlow: StateFlow<DeepLink?> = _deepLinkFlow.asStateFlow()

    private val _pendingDeepLink = MutableStateFlow<DeepLink?>(null)
    actual val pendingDeepLink: StateFlow<DeepLink?> = _pendingDeepLink.asStateFlow()

    companion object {
        private const val TAG = "DeepLinkManager"
        private const val SCHEME = "http"
        private const val HOST = "pocketwardrobe"
        private const val SHARE_SEGMENT = "share"

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
     */
    actual fun handleDeepLinkUrl(url: String): Boolean {
        Log.d(TAG, "handleDeepLinkUrl: $url")

        return try {
            val uri = Uri.parse(url)

            // Validate scheme and host
            if (uri.scheme != SCHEME || uri.host != HOST) {
                Log.w(TAG, "Invalid scheme or host. Expected: $SCHEME://$HOST, Got: ${uri.scheme}://${uri.host}")
                return false
            }

            // Parse path segments
            val pathSegments = uri.pathSegments
            if (pathSegments.size < 2) {
                Log.w(TAG, "Invalid path format. Expected: /share/{uuid}, got: ${pathSegments.joinToString("/")}")
                return false
            }

            // Validate "share" segment
            if (pathSegments[0] != SHARE_SEGMENT) {
                Log.w(TAG, "Invalid segment. Expected '$SHARE_SEGMENT', got: ${pathSegments[0]}")
                return false
            }

            // Extract UUID from second path segment
            val uuid = pathSegments[1]

            // Create and emit deep link
            val deepLink = DeepLink.Look(uuid)
            Log.d(TAG, "Successfully parsed deep link: $deepLink")
            _deepLinkFlow.value = deepLink
            true

        } catch (e: Exception) {
            Log.e(TAG, "Error parsing deep link URL: $url", e)
            false
        }
    }

    actual fun clearDeepLink() {
        Log.d(TAG, "Clearing deep link")
        _deepLinkFlow.value = null
    }

    actual fun setPendingDeepLink(deepLink: DeepLink?) {
        Log.d(TAG, "Setting pending deep link: $deepLink")
        _pendingDeepLink.value = deepLink
    }

    actual fun applyPendingDeepLink() {
        val pending = _pendingDeepLink.value
        if (pending != null) {
            Log.d(TAG, "Applying pending deep link: $pending")
            _deepLinkFlow.value = pending
            _pendingDeepLink.value = null
        } else {
            Log.d(TAG, "No pending deep link to apply")
        }
    }
}
