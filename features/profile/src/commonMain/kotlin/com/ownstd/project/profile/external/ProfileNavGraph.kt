package com.ownstd.project.profile.external

import androidx.compose.runtime.Composable
import com.ownstd.project.profile.internal.presentation.detail.editProfile.EditProfileScreen
import com.ownstd.project.profile.internal.presentation.detail.sizes.SizesScreen
import com.ownstd.project.profile.internal.presentation.root.ProfileScreen

/**
 * Composable для регистрации ProfileRoutes в AppNavHost (Navigation 3).
 *
 * Вызывается внутри контент-лямбды NavDisplay:
 * ```
 * NavDisplay(backStack = backStack) { key ->
 *     ProfileNavGraph(
 *         key = key,
 *         onBack = { backStack.removeLast() },
 *         onNavigateToAuth = { backStack.clear(); backStack.add(AppRoutes.Auth) },
 *     )
 * }
 * ```
 */
@Composable
fun ProfileNavGraph(
    key: Any,
    onBack: () -> Unit,
    onNavigateToAuth: () -> Unit,
) {
    when (key) {
        is ProfileMain -> ProfileScreen(
            onBack = onBack,
            onNavigateToEdit = { /* handled via backStack in AppNavHost */ },
            onNavigateToSizes = { /* handled via backStack in AppNavHost */ },
            onNavigateToAuth = onNavigateToAuth,
        )
        is EditProfile -> EditProfileScreen(onBack = onBack)
        is Sizes -> SizesScreen(onBack = onBack)
    }
}
