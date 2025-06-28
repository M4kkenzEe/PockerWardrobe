package com.ownstd.project.card.internal.navigation

import kotlinx.serialization.Serializable

internal sealed class BottomNavigationScreens {
    abstract val label: String
    abstract val enabledIcon: String
    abstract val disabledIcon: String

    @Serializable
    data class Home(
        override val label: String = "Лента",
        override val enabledIcon: String = "shopping_cart_enabled",
        override val disabledIcon: String = "shopping_cart_disabled"
    ) : BottomNavigationScreens()

    @Serializable
    data class Shop(
        override val label: String = "Гардероб",
        override val enabledIcon: String = "shopping_cart_enabled",
        override val disabledIcon: String = "shopping_cart_disabled"
    ) : BottomNavigationScreens()

    @Serializable
    data class Outfits(
        override val label: String = "Образы",
        override val enabledIcon: String = "shopping_cart_enabled",
        override val disabledIcon: String = "shopping_cart_disabled"
    ) : BottomNavigationScreens()

    @Serializable
    data class Profile(
        override val label: String = "Профиль",
        override val enabledIcon: String = "shopping_cart_enabled",
        override val disabledIcon: String = "shopping_cart_disabled"
    ) : BottomNavigationScreens()

}