package com.ownstd.project.pincard.internal.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.toRoute
import com.ownstd.project.pincard.internal.data.model.Clothe
import com.ownstd.project.pincard.internal.presentation.WardrobeMainScreen
import com.ownstd.project.pincard.internal.presentation.compose.ClothingDetailScreen
import com.ownstd.project.pincard.internal.presentation.compose.LookConstructor
import com.ownstd.project.pincard.internal.presentation.compose.LookDetailsScreen
import com.ownstd.project.pincard.internal.presentation.compose.TinderOutfitScreen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun NavGraphBuilder.wardrobeNavGraph(
    navController: NavHostController,
    scrollToTopEvents: Flow<Unit> = emptyFlow(),
) {
    composable<WardrobeNavScreens.Wardrobe> {
        WardrobeMainScreen(
            openConstructor = {
                navController.navigate(WardrobeNavScreens.LookConstructor)
            },
            openDetails = { lookId ->
                navController.navigate(WardrobeNavScreens.LookDetails(lookId = lookId))
            },
            openTinderOutfit = {
                navController.navigate(WardrobeNavScreens.TinderOutfit)
            },
            openClotheDetail = { clotheId ->
                navController.navigate(WardrobeNavScreens.ClothingDetail(clotheId = clotheId))
            },
            scrollToTopEvents = scrollToTopEvents,
        )
    }
    composable<WardrobeNavScreens.LookConstructor> {
        LookConstructor(
            backClick = { navController.popBackStack() },
            navigateToSavedLooks = {
                navController.navigate(WardrobeNavScreens.Wardrobe) {
                    popUpTo(WardrobeNavScreens.LookConstructor) { inclusive = true }
                }
            }
        )
    }
    composable<WardrobeNavScreens.LookDetails> { backStackEntry ->
        val lookDetails: WardrobeNavScreens.LookDetails = backStackEntry.toRoute()
        LookDetailsScreen(
            lookId = lookDetails.lookId,
            shareToken = lookDetails.shareToken,
            onBackClick = { navController.popBackStack() },
            onNavigateToClotheDetail = { clothe -> navController.openClotheFromLook(clothe) }
        )
    }
    composable<WardrobeNavScreens.TinderOutfit> {
        TinderOutfitScreen(
            onBackClick = { navController.popBackStack() }
        )
    }
    composable<WardrobeNavScreens.ClothingDetail> { backStackEntry ->
        val route: WardrobeNavScreens.ClothingDetail = backStackEntry.toRoute()
        val preloadedClothe: Clothe? = route.clotheJson?.let {
            runCatching { Json.decodeFromString<Clothe>(it) }.getOrNull()
        }
        ClothingDetailScreen(
            clotheId = route.clotheId,
            preloadedClothe = preloadedClothe,
            onBackClick = { navController.popBackStack() },
            onOpenLook = { lookId ->
                navController.navigate(WardrobeNavScreens.LookDetails(lookId = lookId))
            }
        )
    }
}

// Вещь, с экрана которой пришли на образ, уже лежит под ним в стеке: возвращаемся к ней, а не копим копии.
private fun NavHostController.openClotheFromLook(clothe: Clothe) {
    val previous = previousBackStackEntry
    val isOpenedFromThisClothe = previous?.destination?.hasRoute<WardrobeNavScreens.ClothingDetail>() == true &&
        previous.toRoute<WardrobeNavScreens.ClothingDetail>().clotheId == clothe.id
    if (isOpenedFromThisClothe) {
        popBackStack()
        return
    }
    navigate(
        WardrobeNavScreens.ClothingDetail(
            clotheId = clothe.id ?: 0,
            clotheJson = Json.encodeToString(clothe)
        )
    )
}
