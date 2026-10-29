package com.ownstd.project.pincard

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import com.ownstd.project.pincard.internal.presentation.compose.ScrollActivePageToTopEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val ITEM_COUNT = 100
private const val WARDROBE_PAGE = 0
private const val LOOKS_PAGE = 1
private const val FAR_ITEM_INDEX = 60
private const val NEAR_ITEM_INDEX = 20
private val ITEM_HEIGHT = 100.dp

// Реальные Wardrobe/Looks тянут ViewModel и Koin, поэтому страницы пейджера - голые гриды того же типа и с теми же
// state, а подписка на события - настоящий ScrollActivePageToTopEffect, которым пользуется WardrobeMainScreen.
@RunWith(RobolectricTestRunner::class)
internal class ScrollToTopUiTest {

    @get:Rule
    val compose = createComposeRule()

    private val reselectEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private lateinit var pagerState: PagerState
    private lateinit var wardrobeGridState: LazyStaggeredGridState
    private lateinit var looksGridState: LazyGridState
    private lateinit var compositionScope: CoroutineScope

    @Test
    fun `reselect scrolls the wardrobe grid to the top`() {
        showScreen()
        runInComposition { wardrobeGridState.scrollToItem(NEAR_ITEM_INDEX) }

        reselectTab()

        assertWardrobeAtTop()
    }

    @Test
    fun `reselect scrolls the looks grid to the top`() {
        showScreen()
        openPage(LOOKS_PAGE)
        runInComposition { looksGridState.scrollToItem(NEAR_ITEM_INDEX) }

        reselectTab()

        assertLooksAtTop()
    }

    @Test
    fun `reselect scrolls a far scrolled wardrobe grid to the top`() {
        showScreen()
        runInComposition { wardrobeGridState.scrollToItem(FAR_ITEM_INDEX) }

        reselectTab()

        assertWardrobeAtTop()
    }

    @Test
    fun `reselect scrolls a far scrolled looks grid to the top`() {
        showScreen()
        openPage(LOOKS_PAGE)
        runInComposition { looksGridState.scrollToItem(FAR_ITEM_INDEX) }

        reselectTab()

        assertLooksAtTop()
    }

    @Test
    fun `reselect removes the leftover offset of the first item`() {
        showScreen()
        runInComposition { wardrobeGridState.scrollBy(LEFTOVER_OFFSET_PX) }
        assertEquals(0, wardrobeGridState.firstVisibleItemIndex)
        assertTrue(wardrobeGridState.firstVisibleItemScrollOffset > 0)

        reselectTab()

        assertWardrobeAtTop()
    }

    @Test
    fun `reselect on a grid that is already at the top changes nothing and keeps the subscription alive`() {
        showScreen()
        assertWardrobeAtTop()

        reselectTab()
        assertWardrobeAtTop()

        runInComposition { wardrobeGridState.scrollToItem(NEAR_ITEM_INDEX) }
        reselectTab()
        assertWardrobeAtTop()
    }

    @Test
    fun `reselect on the wardrobe page leaves the looks grid where it was`() {
        showScreen()
        openPage(LOOKS_PAGE)
        runInComposition { looksGridState.scrollToItem(NEAR_ITEM_INDEX) }
        openPage(WARDROBE_PAGE)
        runInComposition { wardrobeGridState.scrollToItem(FAR_ITEM_INDEX) }

        reselectTab()

        assertWardrobeAtTop()
        assertEquals(NEAR_ITEM_INDEX, looksGridState.firstVisibleItemIndex)
    }

    @Test
    fun `reselect on the looks page leaves the wardrobe grid where it was`() {
        showScreen()
        runInComposition { wardrobeGridState.scrollToItem(NEAR_ITEM_INDEX) }
        openPage(LOOKS_PAGE)
        runInComposition { looksGridState.scrollToItem(FAR_ITEM_INDEX) }

        reselectTab()

        assertLooksAtTop()
        assertEquals(NEAR_ITEM_INDEX, wardrobeGridState.firstVisibleItemIndex)
    }

    @Test
    fun `reselect after a user gesture interrupted the previous scroll scrolls to the top again`() {
        showScreen()
        runInComposition { wardrobeGridState.scrollToItem(NEAR_ITEM_INDEX) }

        compose.mainClock.autoAdvance = false
        reselectTab(waitForIdle = false)
        compose.mainClock.advanceTimeBy(SCROLL_PROGRESS_MILLIS)
        val indexBeforeInterruption = wardrobeGridState.firstVisibleItemIndex
        assertTrue(indexBeforeInterruption in 1 until NEAR_ITEM_INDEX, "scroll must be in flight, was at $indexBeforeInterruption")

        compositionScope.launch { wardrobeGridState.scroll(MutatePriority.UserInput) {} }
        compose.mainClock.advanceTimeBy(SCROLL_PROGRESS_MILLIS)
        assertTrue(wardrobeGridState.firstVisibleItemIndex > 0, "the gesture must stop the scroll before the top")
        compose.mainClock.autoAdvance = true

        reselectTab()

        assertWardrobeAtTop()
    }

    private fun showScreen() {
        compose.setContent {
            pagerState = rememberPagerState(pageCount = { 2 })
            wardrobeGridState = rememberLazyStaggeredGridState()
            looksGridState = rememberLazyGridState()
            compositionScope = rememberCoroutineScope()

            ScrollActivePageToTopEffect(reselectEvents, pagerState, wardrobeGridState, looksGridState)
            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 1,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                when (page) {
                    WARDROBE_PAGE -> LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        state = wardrobeGridState,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(ITEM_COUNT) { Box(Modifier.fillMaxWidth().height(ITEM_HEIGHT)) }
                    }
                    else -> LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        state = looksGridState,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(ITEM_COUNT) { Box(Modifier.fillMaxWidth().height(ITEM_HEIGHT)) }
                    }
                }
            }
        }
        compose.waitForIdle()
    }

    private fun openPage(page: Int) {
        runInComposition { pagerState.scrollToPage(page) }
    }

    private fun runInComposition(action: suspend () -> Unit) {
        compose.runOnUiThread { compositionScope.launch { action() } }
        compose.waitForIdle()
    }

    private fun reselectTab(waitForIdle: Boolean = true) {
        reselectEvents.tryEmit(Unit)
        if (waitForIdle) compose.waitForIdle()
    }

    private fun assertWardrobeAtTop() {
        assertEquals(0, wardrobeGridState.firstVisibleItemIndex)
        assertEquals(0, wardrobeGridState.firstVisibleItemScrollOffset)
    }

    private fun assertLooksAtTop() {
        assertEquals(0, looksGridState.firstVisibleItemIndex)
        assertEquals(0, looksGridState.firstVisibleItemScrollOffset)
    }

    private companion object {
        const val LEFTOVER_OFFSET_PX = 50f
        const val SCROLL_PROGRESS_MILLIS = 48L
    }
}
