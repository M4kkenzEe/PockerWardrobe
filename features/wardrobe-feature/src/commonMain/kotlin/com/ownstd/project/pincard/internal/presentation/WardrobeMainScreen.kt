package com.ownstd.project.pincard.internal.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ownstd.project.designsystem.components.WardrobeSegmentedControl
import com.ownstd.project.designsystem.theme.ClothisTheme
import com.ownstd.project.pincard.internal.presentation.compose.Looks
import com.ownstd.project.pincard.internal.presentation.compose.Wardrobe
import com.ownstd.project.pincard.internal.presentation.viewmodel.LooksViewModel
import com.ownstd.project.pincard.internal.presentation.viewmodel.WardrobeViewModel
import kotlin.math.abs
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
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

    var pillVisible by remember { mutableStateOf(false) }

    LaunchedEffect(pagerState) {
        snapshotFlow { abs(pagerState.currentPageOffsetFraction) > 0.02f }
            .collectLatest { isSwiping ->
                if (isSwiping) {
                    pillVisible = true
                } else if (pillVisible) {
                    delay(3000)
                    pillVisible = false
                }
            }
    }

    Box(modifier = Modifier.fillMaxSize().background(colors.canvas)) {
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

        // Auto-hiding "Вещи / Образы" tab pill — fades in on swipe, hides 3s after last interaction
        AnimatedVisibility(
            visible = pillVisible,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 8.dp),
            enter = fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)) +
                scaleIn(initialScale = 0.88f, animationSpec = tween(300, easing = FastOutSlowInEasing)),
            exit = fadeOut(animationSpec = tween(220, easing = FastOutLinearInEasing)) +
                scaleOut(targetScale = 0.88f, animationSpec = tween(220, easing = FastOutLinearInEasing)),
        ) {
            WardrobeSegmentedControl(
                tabs = listOf("Вещи", "Образы"),
                selectedIndex = pagerState.currentPage,
                onTabSelected = { page ->
                    scope.launch { pagerState.animateScrollToPage(page) }
                },
            )
        }
    }
}
