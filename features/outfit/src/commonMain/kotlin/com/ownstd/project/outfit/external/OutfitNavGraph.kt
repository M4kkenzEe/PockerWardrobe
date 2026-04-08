package com.ownstd.project.outfit.external

import androidx.compose.runtime.Composable
import com.ownstd.project.outfit.internal.presentation.detail.OutfitDetailScreen
import com.ownstd.project.outfit.internal.presentation.list.OutfitScreen

/**
 * Composable для регистрации OutfitRoutes в AppNavHost (Navigation 3).
 *
 * Вызывается внутри контент-лямбды NavDisplay:
 * ```
 * NavDisplay(backStack = backStack) { key ->
 *     OutfitNavGraph(
 *         key = key,
 *         onBack = { backStack.removeLast() },
 *         onNavigateToDetail = { backStack.add(OutfitRoutes.OutfitDetail(it)) },
 *         onNavigateToConstructor = { backStack.add(OutfitConstructorRoute) },
 *         onNavigateToItem = { backStack.add(WardrobeRoutes.ItemDetail(it)) },
 *     )
 * }
 * ```
 */
@Composable
fun OutfitNavGraph(
    key: Any,
    onBack: () -> Unit,
    onNavigateToDetail: (lookId: Int) -> Unit,
    onNavigateToConstructor: () -> Unit,
    onNavigateToConstructorEdit: (lookId: Int) -> Unit,
    onNavigateToItem: (clotheId: Int) -> Unit,
) {
    when (key) {
        is OutfitRoutes.OutfitMain -> OutfitScreen(
            onNavigateToDetail = onNavigateToDetail,
            onNavigateToConstructor = onNavigateToConstructor,
        )
        is OutfitRoutes.OutfitDetail -> OutfitDetailScreen(
            lookId = key.lookId,
            onBack = onBack,
            onNavigateToConstructor = onNavigateToConstructorEdit,
            onNavigateToItem = onNavigateToItem,
        )
    }
}
