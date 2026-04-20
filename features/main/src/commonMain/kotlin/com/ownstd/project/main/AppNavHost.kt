package com.ownstd.project.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.ownstd.project.authorization.internal.presentation.AuthorizationScreen
import com.ownstd.project.core.deeplink.DeepLinkManager
import com.ownstd.project.core.deeplink.DeeplinkPathParser
import com.ownstd.project.core.deeplink.DeeplinkRoute
import com.ownstd.project.outfit.external.OutfitRoutes
import com.ownstd.project.outfit.internal.presentation.detail.OutfitDetailScreen
import com.ownstd.project.outfitconstructor.external.OutfitConstructorRoute
import com.ownstd.project.outfitconstructor.internal.presentation.root.OutfitConstructorScreen
import com.ownstd.project.profile.external.EditProfile
import com.ownstd.project.profile.external.ProfileMain
import com.ownstd.project.profile.external.Sizes
import com.ownstd.project.profile.internal.presentation.detail.editProfile.EditProfileScreen
import com.ownstd.project.profile.internal.presentation.detail.sizes.SizesScreen
import com.ownstd.project.profile.internal.presentation.root.ProfileScreen
import com.ownstd.project.wardrobe.external.WardrobeRoutes
import com.ownstd.project.wardrobe.internal.presentation.detail.itemDetail.ItemDetailScreen
import com.ownstd.project.wardrobe.internal.presentation.detail.itemEdit.ItemEditScreen
import kotlinx.coroutines.flow.filterNotNull

@Composable
fun AppNavHost() {
    val backStack = rememberNavBackStack(AppRoutes.Auth)

    LaunchedEffect(Unit) {
        DeepLinkManager.pendingDeeplink.filterNotNull().collect { uri ->
            val path = uri.removePrefix("clothis://")
            when (val route = DeeplinkPathParser(path).parse()) {
                is DeeplinkRoute.ItemDetail ->
                    backStack.add(WardrobeRoutes.ItemDetail(route.clotheId))
                is DeeplinkRoute.LookDetail ->
                    backStack.add(OutfitRoutes.OutfitDetail(route.lookId))
                is DeeplinkRoute.Profile ->
                    backStack.add(ProfileMain)
                is DeeplinkRoute.Unknown -> Unit
            }
            DeepLinkManager.onHandled()
        }
    }

    NavDisplay(
        backStack = backStack,
        onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) },
        entryProvider = entryProvider {
            entry<AppRoutes.Auth> {
                AuthorizationScreen(
                    openSession = {
                        if (backStack.isNotEmpty()) backStack.removeAt(0)
                        backStack.add(AppRoutes.Main)
                    }
                )
            }
            entry<AppRoutes.Main> {
                MainClothisScreen(backStack = backStack)
            }

            // Wardrobe
            entry<WardrobeRoutes.ItemDetail> { key ->
                ItemDetailScreen(
                    clotheId = key.clotheId,
                    onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) },
                    onNavigateToEdit = { backStack.add(WardrobeRoutes.ItemEdit(it)) },
                    onNavigateToLook = { backStack.add(OutfitRoutes.OutfitDetail(it)) },
                    onOpenUrl = { },
                )
            }
            entry<WardrobeRoutes.ItemEdit> { key ->
                ItemEditScreen(
                    clotheId = key.clotheId,
                    onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) },
                )
            }

            // Outfit
            entry<OutfitRoutes.OutfitDetail> { key ->
                OutfitDetailScreen(
                    lookId = key.lookId,
                    onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) },
                    onNavigateToConstructor = { backStack.add(OutfitConstructorRoute(it)) },
                    onNavigateToItem = { backStack.add(WardrobeRoutes.ItemDetail(it)) },
                )
            }
            entry<OutfitConstructorRoute> {
                OutfitConstructorScreen(
                    onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) },
                )
            }

            // Profile
            entry<ProfileMain> {
                ProfileScreen(
                    onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) },
                    onNavigateToEdit = { backStack.add(EditProfile) },
                    onNavigateToSizes = { backStack.add(Sizes) },
                    onNavigateToAuth = {
                        if (backStack.isNotEmpty()) backStack.removeAt(0)
                        backStack.add(AppRoutes.Auth)
                    },
                )
            }
            entry<EditProfile> {
                EditProfileScreen(onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) })
            }
            entry<Sizes> {
                SizesScreen(onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) })
            }
        }
    )
}
