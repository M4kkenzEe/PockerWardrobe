package com.ownstd.project.card.internal.deeplink

import kotlinx.coroutines.flow.StateFlow

/**
 * Get the singleton instance of DeepLinkManager
 */
expect fun getDeepLinkManager(): DeepLinkManager

/**
 * Manager for handling deep links across platforms
 *
 * This is an expect/actual implementation:
 * - Android: Parses Intent data
 * - iOS: Parses URL from onOpenURL
 */
expect class DeepLinkManager() {
    /**
     * Current deep link state
     * Observers can subscribe to this flow to react to deep link changes
     */
    val deepLinkFlow: StateFlow<DeepLink?>

    /**
     * Pending deep link that will be applied after authorization
     */
    val pendingDeepLink: StateFlow<DeepLink?>

    /**
     * Handle a deep link URL
     * @param url The deep link URL string (e.g., "http://pocketwardrobe/share/2aeb7515-d11d-4180-ab3c-3e73a13b99bb")
     * @return true if the URL was successfully parsed and handled
     */
    fun handleDeepLinkUrl(url: String): Boolean

    /**
     * Clear the current deep link
     */
    fun clearDeepLink()

    /**
     * Set a pending deep link to be applied later (e.g., after authorization)
     */
    fun setPendingDeepLink(deepLink: DeepLink?)

    /**
     * Apply the pending deep link (move it to active deep link)
     */
    fun applyPendingDeepLink()
}
