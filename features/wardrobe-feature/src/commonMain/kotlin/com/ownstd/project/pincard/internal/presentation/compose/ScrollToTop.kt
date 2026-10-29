package com.ownstd.project.pincard.internal.presentation.compose

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

// Анимация через весь длинный список занимает секунду, поэтому сначала прыгаем близко к началу
internal const val FAR_SCROLL_THRESHOLD_INDEX = 30
internal const val FAR_SCROLL_JUMP_INDEX = 10

internal suspend fun LazyGridState.scrollToTop() {
    if (firstVisibleItemIndex == 0 && firstVisibleItemScrollOffset == 0) return
    if (firstVisibleItemIndex > FAR_SCROLL_THRESHOLD_INDEX) scrollToItem(FAR_SCROLL_JUMP_INDEX)
    animateScrollToItem(0)
}

internal suspend fun LazyStaggeredGridState.scrollToTop() {
    if (firstVisibleItemIndex == 0 && firstVisibleItemScrollOffset == 0) return
    if (firstVisibleItemIndex > FAR_SCROLL_THRESHOLD_INDEX) scrollToItem(FAR_SCROLL_JUMP_INDEX)
    animateScrollToItem(0)
}

@Composable
internal fun ScrollActivePageToTopEffect(
    events: Flow<Unit>,
    pagerState: PagerState,
    wardrobeGridState: LazyStaggeredGridState,
    looksGridState: LazyGridState,
) {
    LaunchedEffect(events) {
        // collectLatest: жест пользователя прерывает скролл CancellationException, collect бы на ней завершился
        events.collectLatest {
            when (pagerState.currentPage) {
                0 -> wardrobeGridState.scrollToTop()
                else -> looksGridState.scrollToTop()
            }
        }
    }
}
