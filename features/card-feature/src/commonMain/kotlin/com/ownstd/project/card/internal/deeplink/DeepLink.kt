package com.ownstd.project.card.internal.deeplink

/**
 * Represents different types of deep links in the application
 */
sealed class DeepLink {
    /**
     * Deep link to a shared Look (wardrobe item)
     * @param shareToken The share token UUID of the look
     */
    data class Look(val shareToken: String) : DeepLink()

    // Future deep link types can be added here:
    // data class Profile(val userId: String) : DeepLink()
    // data class Settings(val section: String) : DeepLink()
}
