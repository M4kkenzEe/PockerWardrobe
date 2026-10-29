package com.ownstd.project.pincard

import com.ownstd.project.pincard.internal.data.model.DraftLook
import com.ownstd.project.pincard.internal.data.model.GenerateDraftsResult
import com.ownstd.project.pincard.internal.data.model.GenerateLooksResult
import com.ownstd.project.pincard.internal.data.model.Look
import com.ownstd.project.pincard.internal.data.model.LookRepositoryResult
import com.ownstd.project.pincard.internal.data.model.ShareResponse
import com.ownstd.project.pincard.internal.domain.WardrobeRefreshSignal
import com.ownstd.project.pincard.internal.domain.repository.LookRepository
import com.ownstd.project.pincard.internal.domain.usecase.LookUseCase
import com.ownstd.project.pincard.internal.presentation.viewmodel.AddLookMode
import com.ownstd.project.pincard.internal.presentation.viewmodel.LookDetailsError
import com.ownstd.project.pincard.internal.presentation.viewmodel.LookDetailsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LookDetailsViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val repo = DetailsLookRepository()
    private val refreshSignal = WardrobeRefreshSignal()

    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    private fun draftViewModel() = LookDetailsViewModel(
        LookUseCase(repo), refreshSignal, lookId = DRAFT_ID, ioDispatcher = dispatcher,
    )

    private fun TestScope.countRefreshes(): () -> Int {
        var refreshes = 0
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { refreshSignal.flow.collect { refreshes++ } }
        return { refreshes }
    }

    @Test fun `draft look gets the draft add mode`() = runTest(dispatcher) {
        val vm = draftViewModel()
        advanceUntilIdle()
        assertEquals(AddLookMode.Draft, vm.addLookMode.value)
    }

    @Test fun `regular look has no add button`() = runTest(dispatcher) {
        repo.look = repo.look.copy(isDraft = false)
        val vm = draftViewModel()
        advanceUntilIdle()
        assertNull(vm.addLookMode.value)
    }

    @Test fun `saving draft marks it added and refreshes the looks list`() = runTest(dispatcher) {
        val vm = draftViewModel()
        advanceUntilIdle()
        val refreshes = countRefreshes()
        advanceUntilIdle()
        vm.onAddLookClick()
        assertTrue(vm.isAddingLook.value)
        advanceUntilIdle()
        assertEquals(listOf(DRAFT_ID), repo.savedDraftIds)
        assertTrue(vm.lookAdded.value)
        assertFalse(vm.isAddingLook.value)
        assertEquals(1, refreshes())
    }

    @Test fun `double tap sends a single request`() = runTest(dispatcher) {
        val vm = draftViewModel()
        advanceUntilIdle()
        vm.onAddLookClick()
        vm.onAddLookClick()
        advanceUntilIdle()
        vm.onAddLookClick()
        advanceUntilIdle()
        assertEquals(listOf(DRAFT_ID), repo.savedDraftIds)
    }

    @Test fun `failed save reports the error and unlocks the button for retry`() = runTest(dispatcher) {
        val vm = draftViewModel()
        advanceUntilIdle()
        val errors = mutableListOf<LookDetailsError>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.errorEvent.collect { errors += it } }
        val refreshes = countRefreshes()
        advanceUntilIdle()
        repo.failSave = true
        vm.onAddLookClick()
        advanceUntilIdle()
        assertEquals(listOf(LookDetailsError.AddLook), errors)
        assertFalse(vm.lookAdded.value)
        assertFalse(vm.isAddingLook.value)
        assertEquals(0, refreshes())

        repo.failSave = false
        vm.onAddLookClick()
        advanceUntilIdle()
        assertTrue(vm.lookAdded.value)
        assertEquals(1, refreshes())
    }

    @Test fun `share mode still imports by token and never saves a draft`() = runTest(dispatcher) {
        val vm = LookDetailsViewModel(
            LookUseCase(repo), refreshSignal, shareToken = "token", ioDispatcher = dispatcher,
        )
        advanceUntilIdle()
        assertEquals(AddLookMode.Share, vm.addLookMode.value)
        vm.onAddLookClick()
        advanceUntilIdle()
        assertEquals(listOf("token"), repo.importedTokens)
        assertTrue(repo.savedDraftIds.isEmpty())
        assertTrue(vm.lookAdded.value)
    }

    private companion object {
        const val DRAFT_ID = 10
    }
}

private class DetailsLookRepository : LookRepository {
    var look = Look(id = 10, name = "Образ", url = "", isDraft = true)
    var failSave = false
    val savedDraftIds = mutableListOf<Int>()
    val importedTokens = mutableListOf<String>()

    override suspend fun getLookById(lookId: Int): Look? = look
    override suspend fun getLookByShareToken(token: String): Look? = look.copy(isDraft = false)
    override suspend fun addLookByShareToken(sharedToken: String) { importedTokens += sharedToken }

    override suspend fun generateDrafts(clotheId: Int): GenerateDraftsResult = error("unused")

    override suspend fun saveDraft(lookId: Int) {
        if (failSave) error("HTTP 500")
        savedDraftIds += lookId
    }

    override suspend fun getLooks(): List<Look> = error("unused")
    override suspend fun addLook(look: DraftLook, image: ByteArray): LookRepositoryResult<Unit> = error("unused")
    override suspend fun addClotheByShareToken(shareToken: String, clotheId: Int) = error("unused")
    override suspend fun deleteLook(lookId: Int) = error("unused")
    override suspend fun shareLook(lookId: Int): ShareResponse? = error("unused")
    override suspend fun generateLooks(): GenerateLooksResult = error("unused")
    override suspend fun getAffiliateLink(storeUrl: String): String? = error("unused")
}
