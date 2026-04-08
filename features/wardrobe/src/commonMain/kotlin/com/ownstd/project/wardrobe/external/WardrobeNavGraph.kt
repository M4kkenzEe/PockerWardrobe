package com.ownstd.project.wardrobe.external

import androidx.compose.runtime.Composable
import com.ownstd.project.wardrobe.internal.presentation.detail.itemDetail.ItemDetailScreen
import com.ownstd.project.wardrobe.internal.presentation.detail.itemEdit.ItemEditScreen

/**
 * Composable для регистрации WardrobeRoutes в AppNavHost (Navigation 3).
 *
 * Вызывается внутри контент-лямбды NavDisplay:
 * ```
 * NavDisplay(backStack = backStack) { key ->
 *     WardrobeNavGraph(
 *         key = key,
 *         onBack = { backStack.removeLast() },
 *         onNavigateToEdit = { backStack.add(WardrobeRoutes.ItemEdit(it)) },
 *         onNavigateToLook = { backStack.add(OutfitRoutes.Detail(it)) },
 *         onOpenUrl = { platformOpenUrl(it) },
 *     )
 *     // ... другие фичи
 * }
 * ```
 */
@Composable
fun WardrobeNavGraph(
    key: Any,
    onBack: () -> Unit,
    onNavigateToEdit: (clotheId: Int) -> Unit,
    onNavigateToLook: (lookId: Int) -> Unit,
    onOpenUrl: (url: String) -> Unit,
) {
    when (key) {
        is WardrobeRoutes.ItemDetail -> ItemDetailScreen(
            clotheId = key.clotheId,
            onBack = onBack,
            onNavigateToEdit = onNavigateToEdit,
            onNavigateToLook = onNavigateToLook,
            onOpenUrl = onOpenUrl,
        )
        is WardrobeRoutes.ItemEdit -> ItemEditScreen(
            clotheId = key.clotheId,
            onBack = onBack,
        )
    }
}
