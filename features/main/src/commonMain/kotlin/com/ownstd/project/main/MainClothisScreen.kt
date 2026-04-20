package com.ownstd.project.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavBackStack
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.ownstd.project.core.compose.components.AppTopBar
import com.ownstd.project.core.compose.components.TabSwitcher
import com.ownstd.project.core.resources.MR
import com.ownstd.project.outfit.external.OutfitRoutes
import com.ownstd.project.outfit.internal.presentation.list.OutfitScreen
import com.ownstd.project.outfitconstructor.external.OutfitConstructorRoute
import com.ownstd.project.profile.external.ProfileMain
import com.ownstd.project.wardrobe.external.WardrobeRoutes
import com.ownstd.project.wardrobe.internal.presentation.list.WardrobeScreen
import dev.icerock.moko.resources.compose.painterResource

private enum class MainTab { CLOTHING, OUTFITS }

@Composable
fun MainClothisScreen(backStack: NavBackStack<NavKey>) {
    var activeTab by rememberSaveable { mutableStateOf(MainTab.CLOTHING) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Clothis",
                trailingContent = {
                    IconButton(onClick = { backStack.add(ProfileMain) }) {
                        Icon(
                            painter = painterResource(MR.images.user),
                            contentDescription = "Профиль",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            TabSwitcher(
                tabs = listOf("Одежда", "Образы"),
                activeIndex = activeTab.ordinal,
                onTabSelected = { activeTab = MainTab.entries[it] },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )

            when (activeTab) {
                MainTab.CLOTHING -> WardrobeScreen(
                    onNavigateToDetail = { backStack.add(WardrobeRoutes.ItemDetail(it)) },
                    onShowConstructor = { backStack.add(OutfitConstructorRoute()) },
                )
                MainTab.OUTFITS -> OutfitScreen(
                    onNavigateToDetail = { backStack.add(OutfitRoutes.OutfitDetail(it)) },
                    onNavigateToConstructor = { backStack.add(OutfitConstructorRoute()) },
                )
            }
        }
    }
}
