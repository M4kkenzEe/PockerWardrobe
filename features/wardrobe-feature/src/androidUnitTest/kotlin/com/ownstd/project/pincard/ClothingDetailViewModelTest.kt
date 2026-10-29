package com.ownstd.project.pincard

import androidx.compose.ui.graphics.ImageBitmap
import com.ownstd.project.pincard.internal.data.model.Clothe
import com.ownstd.project.pincard.internal.domain.WardrobeRefreshSignal
import com.ownstd.project.pincard.internal.domain.repository.WardrobeRepository
import com.ownstd.project.pincard.internal.domain.usecase.LookUseCase
import com.ownstd.project.pincard.internal.domain.usecase.WardrobeUseCase
import com.ownstd.project.pincard.internal.presentation.viewmodel.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class ClothingDetailViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val item = Clothe(1, "Рубашка", imageUrl = "", material = "Хлопок", storeUrl = "https://example.com/item")
    private val repo = DetailRepository(item)

    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }
    private fun viewModel(preloaded: Clothe? = null) = ClothingDetailViewModel(
        WardrobeUseCase(repo), LookUseCase(StubLookRepository()), WardrobeRefreshSignal(), 1, preloaded, dispatcher,
    )

    @Test fun `saving keeps ownership and only sends changed fields`() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()
        vm.onEditToggle()
        vm.onFieldChange(ClothingField.Name, "Льняная рубашка")
        vm.onSave()
        advanceUntilIdle()
        assertEquals(mapOf("name" to "Льняная рубашка"), repo.patch)
        assertEquals(true, vm.state.value.isOwned)
        assertFalse(vm.state.value.isEditMode)
        assertTrue(vm.state.value.didSave)
        vm.onEditToggle()
        assertTrue(vm.state.value.isEditMode)
    }

    @Test fun `failed ownership stays unknown and retry restores owned actions`() = runTest(dispatcher) {
        repo.failLoad = true
        val vm = viewModel(item)
        advanceUntilIdle()
        assertNotNull(vm.state.value.details)
        assertNull(vm.state.value.isOwned)
        assertEquals(ClothingDetailError.Ownership, vm.state.value.error)
        vm.onEditToggle()
        vm.saveToWardrobe()
        assertFalse(vm.state.value.isEditMode)
        assertEquals(0, repo.addCount)
        repo.failLoad = false
        vm.retry()
        advanceUntilIdle()
        assertEquals(true, vm.state.value.isOwned)
    }

    @Test fun `save failure retains draft for retry`() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()
        vm.onEditToggle()
        vm.onFieldChange(ClothingField.Material, "Лён")
        repo.failSave = true
        vm.onSave()
        advanceUntilIdle()
        assertEquals(ClothingDetailError.Save, vm.state.value.error)
        assertEquals("Лён", vm.state.value.draft[ClothingField.Material])
        assertTrue(vm.state.value.isEditMode)
        repo.failSave = false
        vm.onSave()
        advanceUntilIdle()
        assertEquals("Лён", vm.state.value.clothe?.material)
    }

    @Test fun `clearing optional field sends empty string instead of omitted null`() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()
        vm.onEditToggle()
        vm.onFieldChange(ClothingField.StoreUrl, "")
        vm.onSave()
        advanceUntilIdle()
        assertEquals(mapOf("storeUrl" to ""), repo.patch)
        assertNull(vm.state.value.details?.storeUrl)
    }

    @Test fun `invalid URL and empty name cannot be saved`() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()
        vm.onEditToggle()
        vm.onFieldChange(ClothingField.StoreUrl, "javascript:alert(1)")
        vm.onSave()
        assertEquals(ClothingDetailError.InvalidUrl, vm.state.value.error)
        vm.onFieldChange(ClothingField.Name, "   ")
        vm.onSave()
        assertEquals(ClothingDetailError.NameRequired, vm.state.value.error)
        assertEquals(0, repo.patchCount)
    }

    @Test fun `saving blocks duplicate requests and changes to submitted draft`() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()
        vm.onEditToggle()
        vm.onFieldChange(ClothingField.Name, "Новое название")
        vm.onSave()
        vm.onSave()
        vm.onFieldChange(ClothingField.Name, "Другой текст")
        vm.onEditToggle()
        assertTrue(vm.state.value.isEditMode)
        advanceUntilIdle()
        assertEquals(1, repo.patchCount)
        assertEquals("Новое название", vm.state.value.clothe?.name)
    }

    @Test fun `cancel discards draft without writing and not found is recoverable`() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()
        vm.onEditToggle()
        vm.onFieldChange(ClothingField.Name, "Не сохранять")
        vm.onEditToggle()
        vm.onEditToggle()
        assertEquals(item.name, vm.state.value.draft[ClothingField.Name])
        assertEquals(0, repo.patchCount)
        repo.owned = false
        val missing = viewModel()
        advanceUntilIdle()
        assertEquals(ClothingDetailError.NotFound, missing.state.value.error)
        assertFalse(missing.state.value.isLoading)
    }

    @Test fun `catalog item cannot be edited and duplicate add is blocked`() = runTest(dispatcher) {
        repo.owned = false
        val vm = viewModel(item)
        advanceUntilIdle()
        vm.onEditToggle()
        assertFalse(vm.state.value.isEditMode)
        vm.saveToWardrobe()
        vm.saveToWardrobe()
        advanceUntilIdle()
        assertEquals(1, repo.addCount)
        assertEquals(true, vm.state.value.isOwned)
    }
}

private class DetailRepository(var item: Clothe) : WardrobeRepository {
    var failLoad = false
    var failSave = false
    var owned = true
    var patch = emptyMap<String, String>()
    var patchCount = 0
    var addCount = 0
    override suspend fun getClothes(occasion: String?): List<Clothe> {
        if (failLoad) error("offline")
        return if (owned) listOf(item) else emptyList()
    }
    override suspend fun loadClothe(bitmap: ImageBitmap, occasion: String?): Clothe = Clothe.empty()
    override suspend fun uploadFromUrl(pageUrl: String): Clothe = error("unused")
    override suspend fun deleteClothe(clotheId: Int) = Unit
    override suspend fun saveToWardrobe(clotheId: Int): Clothe { addCount++; return item }
    override suspend fun updateClothe(
        clotheId: Int, name: String?, storeUrl: String?, season: String?, fit: String?,
        material: String?, brand: String?, occasion: String?, styleTags: String?,
    ): Clothe {
        patchCount++
        if (failSave) error("offline")
        patch = listOf("name" to name, "storeUrl" to storeUrl, "season" to season, "fit" to fit,
            "material" to material, "brand" to brand, "occasion" to occasion, "styleTags" to styleTags)
            .mapNotNull { (key, value) -> value?.let { key to it } }.toMap()
        item = item.copy(name = name ?: item.name, storeUrl = storeUrl ?: item.storeUrl, material = material ?: item.material)
        return item
    }
}
