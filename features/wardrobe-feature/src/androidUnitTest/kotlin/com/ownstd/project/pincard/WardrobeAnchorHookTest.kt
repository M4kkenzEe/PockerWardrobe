package com.ownstd.project.pincard

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.colorspace.ColorSpace
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import com.ownstd.project.pincard.internal.data.model.Clothe
import com.ownstd.project.pincard.internal.data.model.DraftLookPreview
import com.ownstd.project.pincard.internal.data.model.GenerateDraftsResult
import com.ownstd.project.pincard.internal.data.model.MissingCategory
import com.ownstd.project.pincard.internal.domain.FreemiumLimitException
import com.ownstd.project.pincard.internal.domain.WardrobeRefreshSignal
import com.ownstd.project.pincard.internal.domain.repository.WardrobeRepository
import com.ownstd.project.pincard.internal.domain.usecase.LookUseCase
import com.ownstd.project.pincard.internal.domain.usecase.WardrobeUseCase
import com.ownstd.project.pincard.internal.presentation.viewmodel.AnchorHookState
import com.ownstd.project.pincard.internal.presentation.viewmodel.WardrobeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class WardrobeAnchorHookTest {
    private val dispatcher = StandardTestDispatcher()
    private val clotheWithId = Clothe(id = 42, name = "Рубашка", imageUrl = "img")
    private val wardrobeRepo = AnchorHookWardrobeRepository(clotheWithId)
    private val lookRepo = AnchorHookLookRepository()

    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    private fun viewModel() = WardrobeViewModel(
        useCase = WardrobeUseCase(wardrobeRepo),
        lookUseCase = LookUseCase(lookRepo),
        wardrobeRefreshSignal = WardrobeRefreshSignal(),
        ioDispatcher = dispatcher,
    )

    @Test fun `loadClothe success sets Celebrate with clothe id and imageUrl`() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()
        vm.loadClothe(stubImageBitmap())
        advanceUntilIdle()
        assertEquals(AnchorHookState.Celebrate(clotheWithId.id!!, clotheWithId.imageUrl), vm.anchorHookState.value)
    }

    @Test fun `loadClothe FreemiumLimitException shows paywall and stays Hidden`() = runTest(dispatcher) {
        wardrobeRepo.throwOnLoad = FreemiumLimitException()
        val vm = viewModel()
        advanceUntilIdle()
        vm.loadClothe(stubImageBitmap())
        advanceUntilIdle()
        assertEquals(AnchorHookState.Hidden, vm.anchorHookState.value)
        assertTrue(vm.showPaywall.value)
    }

    @Test fun `loadClothe other error stays Hidden without paywall`() = runTest(dispatcher) {
        wardrobeRepo.throwOnLoad = RuntimeException("network error")
        val vm = viewModel()
        advanceUntilIdle()
        vm.loadClothe(stubImageBitmap())
        advanceUntilIdle()
        assertEquals(AnchorHookState.Hidden, vm.anchorHookState.value)
        assertFalse(vm.showPaywall.value)
    }

    @Test fun `uploadFromUrl success sets Celebrate with clothe id`() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()
        vm.uploadFromUrl("https://example.com")
        advanceUntilIdle()
        assertEquals(AnchorHookState.Celebrate(clotheWithId.id!!, clotheWithId.imageUrl), vm.anchorHookState.value)
    }

    @Test fun `onGenerateDrafts transitions to Hidden and emits navigation event on Success`() = runTest(dispatcher) {
        lookRepo.result = GenerateDraftsResult.Success(listOf(DraftLookPreview(7, "url")))
        val vm = viewModel()
        advanceUntilIdle()
        vm.anchorHookState.value = AnchorHookState.Celebrate(clotheWithId.id!!, clotheWithId.imageUrl)
        vm.onGenerateDrafts(clotheWithId.id!!)
        assertEquals(AnchorHookState.Generating(clotheWithId.id!!), vm.anchorHookState.value)
        advanceUntilIdle()
        assertEquals(AnchorHookState.Hidden, vm.anchorHookState.value)
        // Channel buffers the event; collect it after advanceUntilIdle
        val lookId = vm.navigateToLookEvent.first()
        assertEquals(7, lookId)
    }

    @Test fun `onGenerateDrafts NeedMore shows NeedMore state`() = runTest(dispatcher) {
        lookRepo.result = GenerateDraftsResult.NeedMore(listOf(MissingCategory.Bottom, MissingCategory.Shoes))
        val vm = viewModel()
        advanceUntilIdle()
        vm.anchorHookState.value = AnchorHookState.Celebrate(clotheWithId.id!!, clotheWithId.imageUrl)
        vm.onGenerateDrafts(clotheWithId.id!!)
        advanceUntilIdle()
        val hookState = vm.anchorHookState.value
        assertTrue(hookState is AnchorHookState.NeedMore)
        assertEquals(listOf(MissingCategory.Bottom, MissingCategory.Shoes), hookState.missing)
    }

    @Test fun `onGenerateDrafts Unsupported dismisses sheet`() = runTest(dispatcher) {
        lookRepo.result = GenerateDraftsResult.Unsupported
        val vm = viewModel()
        advanceUntilIdle()
        vm.anchorHookState.value = AnchorHookState.Celebrate(clotheWithId.id!!, clotheWithId.imageUrl)
        vm.onGenerateDrafts(clotheWithId.id!!)
        advanceUntilIdle()
        assertEquals(AnchorHookState.Hidden, vm.anchorHookState.value)
    }

    @Test fun `onGenerateDrafts AllSaved shows AllSaved state`() = runTest(dispatcher) {
        lookRepo.result = GenerateDraftsResult.AllSaved
        val vm = viewModel()
        advanceUntilIdle()
        vm.anchorHookState.value = AnchorHookState.Celebrate(clotheWithId.id!!, clotheWithId.imageUrl)
        vm.onGenerateDrafts(clotheWithId.id!!)
        advanceUntilIdle()
        assertEquals(AnchorHookState.AllSaved, vm.anchorHookState.value)
    }

    @Test fun `onGenerateDrafts Error sets Error state`() = runTest(dispatcher) {
        lookRepo.result = GenerateDraftsResult.Error
        val vm = viewModel()
        advanceUntilIdle()
        vm.anchorHookState.value = AnchorHookState.Celebrate(clotheWithId.id!!, clotheWithId.imageUrl)
        vm.onGenerateDrafts(clotheWithId.id!!)
        advanceUntilIdle()
        assertEquals(AnchorHookState.Error, vm.anchorHookState.value)
    }

    @Test fun `double tap sends single request`() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()
        vm.anchorHookState.value = AnchorHookState.Celebrate(clotheWithId.id!!, clotheWithId.imageUrl)
        vm.onGenerateDrafts(clotheWithId.id!!)
        vm.onGenerateDrafts(clotheWithId.id!!)
        advanceUntilIdle()
        assertEquals(1, lookRepo.requests.size)
    }

    @Test fun `dismissHook resets state to Hidden`() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()
        vm.anchorHookState.value = AnchorHookState.Celebrate(clotheWithId.id!!, clotheWithId.imageUrl)
        vm.dismissHook()
        assertEquals(AnchorHookState.Hidden, vm.anchorHookState.value)
    }
}

// Creates a minimal ImageBitmap stub without touching android.graphics.Bitmap,
// safe to use in Robolectric unit tests where Bitmap.createBitmap may return null.
private fun stubImageBitmap(): ImageBitmap = object : ImageBitmap {
    override val colorSpace: ColorSpace = ColorSpaces.Srgb
    override val config: ImageBitmapConfig = ImageBitmapConfig.Argb8888
    override val hasAlpha: Boolean = false
    override val height: Int = 1
    override val width: Int = 1
    override fun prepareToDraw() {}
    override fun readPixels(
        buffer: IntArray,
        startX: Int,
        startY: Int,
        width: Int,
        height: Int,
        bufferOffset: Int,
        stride: Int,
    ) {}
}

private class AnchorHookWardrobeRepository(private val clothe: Clothe) : WardrobeRepository {
    var throwOnLoad: Exception? = null

    override suspend fun getClothes(occasion: String?) = listOf(clothe)

    override suspend fun loadClothe(bitmap: ImageBitmap, occasion: String?): Clothe {
        throwOnLoad?.let { throw it }
        return clothe
    }

    override suspend fun uploadFromUrl(pageUrl: String) = clothe
    override suspend fun deleteClothe(clotheId: Int) = Unit
    override suspend fun updateClothe(
        clotheId: Int, name: String?, storeUrl: String?, season: String?, fit: String?,
        material: String?, brand: String?, occasion: String?, styleTags: String?,
    ): Clothe = clothe
    override suspend fun saveToWardrobe(clotheId: Int) = clothe
}

private class AnchorHookLookRepository : StubLookRepository() {
    var result: GenerateDraftsResult = GenerateDraftsResult.AllSaved
    val requests = mutableListOf<Int>()

    override suspend fun generateDrafts(clotheId: Int): GenerateDraftsResult {
        requests += clotheId
        return result
    }
}
