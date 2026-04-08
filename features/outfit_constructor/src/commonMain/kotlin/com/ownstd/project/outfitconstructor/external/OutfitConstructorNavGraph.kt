package com.ownstd.project.outfitconstructor.external

import androidx.compose.runtime.Composable
import com.ownstd.project.outfitconstructor.internal.presentation.root.OutfitConstructorScreen

/**
 * Composable для регистрации OutfitConstructorRoute в AppNavHost (Navigation 3).
 *
 * Вызывается внутри контент-лямбды NavDisplay:
 * ```
 * NavDisplay(backStack = backStack) { key ->
 *     OutfitConstructorNavGraph(
 *         key = key,
 *         onBack = { backStack.removeLast() },
 *     )
 * }
 * ```
 */
@Composable
fun OutfitConstructorNavGraph(
    key: Any,
    onBack: () -> Unit,
) {
    when (key) {
        is OutfitConstructorRoute -> OutfitConstructorScreen(
            onBack = onBack,
        )
    }
}
