package com.ownstd.project.pincard.internal.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import com.ownstd.project.designsystem.components.WardrobeTopBar
import com.ownstd.project.designsystem.theme.ClothisTheme
import com.ownstd.project.pincard.internal.presentation.compose.Looks
import com.ownstd.project.pincard.internal.presentation.compose.Wardrobe
import com.ownstd.project.pincard.internal.presentation.viewmodel.LooksViewModel
import com.ownstd.project.pincard.internal.presentation.viewmodel.WardrobeViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun WardrobeMainScreen(
    openConstructor: () -> Unit = {},
    openDetails: (url: Int) -> Unit = {},
    openTinderOutfit: () -> Unit = {},
    openClotheDetail: (clotheId: Int) -> Unit = {},
) {
    val looksViewModel: LooksViewModel = koinViewModel()
    val wardrobeViewModel: WardrobeViewModel = koinViewModel()
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()
    val colors = ClothisTheme.colors

    Column(modifier = Modifier.fillMaxSize().background(colors.canvas)) {
        WardrobeTopBar(
            selectedTab = pagerState.currentPage,
            onTabSelected = { page ->
                scope.launch { pagerState.animateScrollToPage(page) }
            },
            onSearchClick = { /* TODO: открыть поиск */ },
            onFilterClick = { /* TODO: открыть фильтр */ },
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            when (page) {
                0 -> Wardrobe(wardrobeViewModel, onClotheClick = openClotheDetail)
                else -> Looks(
                    viewModel = looksViewModel,
                    onNavigateToConstructor = openConstructor,
                    navigateToDetails = openDetails,
                )
            }
        }
    }
}
