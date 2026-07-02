package com.ownstd.project.pincard.internal.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.ownstd.project.pincard.internal.presentation.WardrobeMainScreen
import com.ownstd.project.pincard.internal.presentation.compose.ClothingDetailScreen
import com.ownstd.project.pincard.internal.presentation.compose.LookConstructor
import com.ownstd.project.pincard.internal.presentation.compose.LookDetailsScreen
import com.ownstd.project.pincard.internal.presentation.compose.TinderOutfitScreen

fun NavGraphBuilder.wardrobeNavGraph(navController: NavHostController) {
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
            }
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
            onNavigateToClotheDetail = { clotheId ->
                navController.navigate(WardrobeNavScreens.ClothingDetail(clotheId = clotheId))
            }
        )
    }
    composable<WardrobeNavScreens.TinderOutfit> {
        TinderOutfitScreen(
            onBackClick = { navController.popBackStack() }
        )
    }
    composable<WardrobeNavScreens.ClothingDetail> { backStackEntry ->
        val route: WardrobeNavScreens.ClothingDetail = backStackEntry.toRoute()
        ClothingDetailScreen(
            clotheId = route.clotheId,
            onBackClick = { navController.popBackStack() }
        )
    }
}
