package com.ownstd.project.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
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
import com.ownstd.project.storage.TokenStorage
import com.ownstd.project.wardrobe.external.WardrobeRoutes
import com.ownstd.project.wardrobe.internal.presentation.detail.itemDetail.ItemDetailScreen
import com.ownstd.project.wardrobe.internal.presentation.detail.itemEdit.ItemEditScreen
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.koin.compose.koinInject

/**
 * Конфигурация сериализации для NavBackStack.
 *
 * В KMP-порте Navigation 3 reflection-based сериализация ключей работает только на Android.
 * Для iOS/JVM/native нужно явно зарегистрировать все подтипы NavKey через полиморфный
 * SerializersModule, иначе rememberNavBackStack(...) не компилируется в commonMain
 * (см. ошибку: "expected SavedStateConfiguration").
 *
 * Когда добавляете новый маршрут (новый NavKey), обязательно добавьте его сюда.
 */
private val navBackStackConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            // App-level
            subclass(AppRoutes.Auth::class, AppRoutes.Auth.serializer())
            subclass(AppRoutes.Main::class, AppRoutes.Main.serializer())
            // Wardrobe
            subclass(WardrobeRoutes.Main::class, WardrobeRoutes.Main.serializer())
            subclass(WardrobeRoutes.ItemDetail::class, WardrobeRoutes.ItemDetail.serializer())
            subclass(WardrobeRoutes.ItemEdit::class, WardrobeRoutes.ItemEdit.serializer())
            // Outfit
            subclass(OutfitRoutes.OutfitMain::class, OutfitRoutes.OutfitMain.serializer())
            subclass(OutfitRoutes.OutfitDetail::class, OutfitRoutes.OutfitDetail.serializer())
            // Outfit constructor
            subclass(OutfitConstructorRoute::class, OutfitConstructorRoute.serializer())
            // Profile
            subclass(ProfileMain::class, ProfileMain.serializer())
            subclass(EditProfile::class, EditProfile.serializer())
            subclass(Sizes::class, Sizes.serializer())
        }
    }
}

@Composable
fun AppNavHost() {
    val tokenStorage: TokenStorage = koinInject()
    val startDestination = remember {
        if (tokenStorage.hasSession()) AppRoutes.Main else AppRoutes.Auth
    }
    val backStack = rememberNavBackStack(navBackStackConfig, startDestination)

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
                        AppRouteManager.replaceWith(backStack, AppRoutes.Main)
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
                        AppRouteManager.replaceWith(backStack, AppRoutes.Auth)
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
