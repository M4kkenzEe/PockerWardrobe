package com.ownstd.project.pincard.internal.presentation.compose

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState

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
