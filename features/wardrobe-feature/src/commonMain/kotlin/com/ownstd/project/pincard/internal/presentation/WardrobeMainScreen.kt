package com.ownstd.project.pincard.internal.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ownstd.project.pincard.internal.presentation.compose.Looks
import com.ownstd.project.pincard.internal.presentation.compose.Wardrobe
import com.ownstd.project.pincard.internal.presentation.viewmodel.LooksViewModel
import com.ownstd.project.pincard.internal.presentation.viewmodel.WardrobeViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun WardrobeMainScreen(
    openConstructor: () -> Unit = {},
    openDetails: (url: Int) -> Unit = {},
) {
    val looksViewModel: LooksViewModel = koinViewModel()
    val wardrobeViewModel: WardrobeViewModel = koinViewModel()
    val pagerState = rememberPagerState(pageCount = { 2 })
    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> Wardrobe(wardrobeViewModel)
                else -> Looks(
                    viewModel = looksViewModel,
                    onNavigateToConstructor = openConstructor,
                    navigateToDetails = openDetails
                )
            }
        }
        Row(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pagerState.pageCount) { iteration ->
                val color =
                    if (pagerState.currentPage == iteration) Color.DarkGray else Color.LightGray
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(16.dp)
                )
            }
        }
    }
}