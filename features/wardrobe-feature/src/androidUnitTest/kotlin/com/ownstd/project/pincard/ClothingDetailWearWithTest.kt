package com.ownstd.project.pincard

import com.ownstd.project.pincard.internal.data.model.Clothe
import com.ownstd.project.pincard.internal.data.model.DraftLookPreview
import com.ownstd.project.pincard.internal.data.model.GenerateDraftsResult
import com.ownstd.project.pincard.internal.data.model.MissingCategory
import com.ownstd.project.pincard.internal.data.model.parseMissingCategories
import com.ownstd.project.pincard.internal.domain.WardrobeRefreshSignal
import com.ownstd.project.pincard.internal.domain.repository.WardrobeRepository
import com.ownstd.project.pincard.internal.domain.usecase.LookUseCase
import com.ownstd.project.pincard.internal.domain.usecase.WardrobeUseCase
import com.ownstd.project.pincard.internal.presentation.viewmodel.ClothingDetailViewModel
import com.ownstd.project.pincard.internal.presentation.viewmodel.ClothingField
import com.ownstd.project.pincard.internal.presentation.viewmodel.WearWithState
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ClothingDetailWearWithTest {
    private val dispatcher = StandardTestDispatcher()
    private val item = Clothe(1, "Рубашка", imageUrl = "")
    private val wardrobeRepo = OwnedWardrobeRepository(item)
    private val lookRepo = DraftsLookRepository()

    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    private fun viewModel() = ClothingDetailViewModel(
        WardrobeUseCase(wardrobeRepo), LookUseCase(lookRepo), WardrobeRefreshSignal(), item.id!!, null, dispatcher,
    )

    private suspend fun kotlinx.coroutines.test.TestScope.loadedViewModel(): ClothingDetailViewModel {
        val vm = viewModel()
        advanceUntilIdle()
        return vm
    }

    @Test fun `idle until the user asks`() = runTest(dispatcher) {
        val vm = loadedViewModel()
        assertEquals(WearWithState.Idle, vm.wearWith.value)
        assertEquals(0, lookRepo.requests.size)
    }

    @Test fun `tap shows loading, then looks`() = runTest(dispatcher) {
        val vm = loadedViewModel()
        val looks = listOf(DraftLookPreview(1, "a"), DraftLookPreview(2, "b"))
        lookRepo.result = GenerateDraftsResult.Success(looks)
        vm.onWearWithClick()
        assertEquals(WearWithState.Loading, vm.wearWith.value)
        advanceUntilIdle()
        assertEquals(WearWithState.Success(looks), vm.wearWith.value)
        assertEquals(listOf(item.id!!), lookRepo.requests)
    }

    @Test fun `every backend outcome has its own state`() = runTest(dispatcher) {
        val expected = mapOf(
            GenerateDraftsResult.AllSaved to WearWithState.AllSaved,
            GenerateDraftsResult.NeedMore(listOf(MissingCategory.Bottom, MissingCategory.Shoes)) to
                WearWithState.NeedMore(listOf(MissingCategory.Bottom, MissingCategory.Shoes)),
            GenerateDraftsResult.Unsupported to WearWithState.Unsupported,
            GenerateDraftsResult.Error to WearWithState.Error,
        )
        expected.forEach { (result, state) ->
            val vm = loadedViewModel()
            lookRepo.result = result
            vm.onWearWithClick()
            advanceUntilIdle()
            assertEquals(state, vm.wearWith.value)
        }
    }

    @Test fun `thrown failure ends in error instead of hanging in loading`() = runTest(dispatcher) {
        val vm = loadedViewModel()
        lookRepo.failure = IllegalStateException("boom")
        vm.onWearWithClick()
        advanceUntilIdle()
        assertEquals(WearWithState.Error, vm.wearWith.value)
    }

    @Test fun `double tap sends a single request`() = runTest(dispatcher) {
        val vm = loadedViewModel()
        vm.onWearWithClick()
        vm.onWearWithClick()
        advanceUntilIdle()
        vm.onWearWithClick()
        advanceUntilIdle()
        assertEquals(1, lookRepo.requests.size)
    }

    @Test fun `retry after error goes through loading and can succeed`() = runTest(dispatcher) {
        val vm = loadedViewModel()
        lookRepo.result = GenerateDraftsResult.Error
        vm.onWearWithClick()
        advanceUntilIdle()
        assertEquals(WearWithState.Error, vm.wearWith.value)
        lookRepo.result = GenerateDraftsResult.AllSaved
        vm.onWearWithClick()
        assertEquals(WearWithState.Loading, vm.wearWith.value)
        advanceUntilIdle()
        assertEquals(WearWithState.AllSaved, vm.wearWith.value)
        assertEquals(2, lookRepo.requests.size)
    }

    @Test fun `catalog item cannot generate outfits`() = runTest(dispatcher) {
        wardrobeRepo.owned = false
        val vm = ClothingDetailViewModel(
            WardrobeUseCase(wardrobeRepo), LookUseCase(lookRepo), WardrobeRefreshSignal(), item.id!!, item, dispatcher,
        )
        advanceUntilIdle()
        vm.onWearWithClick()
        advanceUntilIdle()
        assertEquals(WearWithState.Idle, vm.wearWith.value)
        assertTrue(lookRepo.requests.isEmpty())
    }

    @Test fun `edit mode blocks generation`() = runTest(dispatcher) {
        val vm = loadedViewModel()
        vm.onEditToggle()
        vm.onWearWithClick()
        advanceUntilIdle()
        assertEquals(WearWithState.Idle, vm.wearWith.value)
        assertTrue(lookRepo.requests.isEmpty())
    }

    @Test fun `saving edits resets the section to idle`() = runTest(dispatcher) {
        val vm = loadedViewModel()
        lookRepo.result = GenerateDraftsResult.AllSaved
        vm.onWearWithClick()
        advanceUntilIdle()
        vm.onEditToggle()
        vm.onFieldChange(ClothingField.Name, "Другая рубашка")
        vm.onSave()
        advanceUntilIdle()
        assertEquals(WearWithState.Idle, vm.wearWith.value)
    }

    @Test fun `saving edits discards an answer that arrives later`() = runTest(dispatcher) {
        val vm = loadedViewModel()
        val gate = CompletableDeferred<GenerateDraftsResult>()
        lookRepo.gate = gate
        vm.onWearWithClick()
        advanceUntilIdle()
        vm.onEditToggle()
        vm.onFieldChange(ClothingField.Name, "Другая рубашка")
        vm.onSave()
        advanceUntilIdle()
        gate.complete(GenerateDraftsResult.AllSaved)
        advanceUntilIdle()
        assertEquals(WearWithState.Idle, vm.wearWith.value)
    }

    @Test fun `retry of the clothe load keeps the generated section`() = runTest(dispatcher) {
        val vm = loadedViewModel()
        lookRepo.result = GenerateDraftsResult.AllSaved
        vm.onWearWithClick()
        advanceUntilIdle()
        vm.retry()
        advanceUntilIdle()
        assertEquals(WearWithState.AllSaved, vm.wearWith.value)
        assertEquals(1, lookRepo.requests.size)
    }

    @Test fun `missing categories are parsed and unknown ones dropped`() {
        assertEquals(
            listOf(MissingCategory.Top, MissingCategory.Bottom, MissingCategory.Shoes),
            parseMissingCategories(listOf("top", "bottom", "shoes", "hat")),
        )
    }
}

private class DraftsLookRepository : StubLookRepository() {
    var result: GenerateDraftsResult = GenerateDraftsResult.AllSaved
    var failure: Exception? = null
    var gate: CompletableDeferred<GenerateDraftsResult>? = null
    val requests = mutableListOf<Int>()

    override suspend fun generateDrafts(clotheId: Int): GenerateDraftsResult {
        requests += clotheId
        failure?.let { throw it }
        return gate?.await() ?: result
    }
}

private class OwnedWardrobeRepository(private val item: Clothe) : WardrobeRepository {
    var owned = true
    override suspend fun getClothes(occasion: String?): List<Clothe> = if (owned) listOf(item) else emptyList()
    override suspend fun updateClothe(
        clotheId: Int, name: String?, storeUrl: String?, season: String?, fit: String?,
        material: String?, brand: String?, occasion: String?, styleTags: String?,
    ): Clothe = item.copy(name = name ?: item.name)
    override suspend fun loadClothe(bitmap: ImageBitmap, occasion: String?): Clothe = error("unused")
    override suspend fun uploadFromUrl(pageUrl: String): Clothe = error("unused")
    override suspend fun deleteClothe(clotheId: Int) = error("unused")
    override suspend fun saveToWardrobe(clotheId: Int): Clothe = error("unused")
}
