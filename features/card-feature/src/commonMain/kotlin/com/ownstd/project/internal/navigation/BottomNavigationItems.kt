package com.ownstd.project.internal.navigation

import com.ownstd.project.card.resources.Res
import com.ownstd.project.card.resources.shopping_cart_disabled
import com.ownstd.project.card.resources.shopping_cart_enabled
import org.jetbrains.compose.resources.DrawableResource

internal enum class BottomNavigationItems(
    val route: String,
    val label: String,
    val enabledIcon: DrawableResource,
    val disabledIcon: DrawableResource,
) {
    HOME(
        route = "home_screen",
        label = "Лента",
        enabledIcon = Res.drawable.shopping_cart_enabled,
        disabledIcon = Res.drawable.shopping_cart_disabled
    ),
    SHOP(
        route = "shop_screen",
        label = "Гардероб",
        enabledIcon = Res.drawable.shopping_cart_enabled,
        disabledIcon = Res.drawable.shopping_cart_disabled
    ),
    OUTFITS(
        route = "outfits_screen",
        label = "Образы",
        enabledIcon = Res.drawable.shopping_cart_enabled,
        disabledIcon = Res.drawable.shopping_cart_disabled
    ),
    PROFILE(
        route = "profile_screen",
        label = "Профиль",
        enabledIcon = Res.drawable.shopping_cart_enabled,
        disabledIcon = Res.drawable.shopping_cart_disabled
    ),
}