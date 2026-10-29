package com.ownstd.project.pincard

import com.ownstd.project.pincard.internal.data.model.DraftLook
import com.ownstd.project.pincard.internal.data.model.GenerateLooksResult
import com.ownstd.project.pincard.internal.data.model.Look
import com.ownstd.project.pincard.internal.data.model.LookRepositoryResult
import com.ownstd.project.pincard.internal.data.model.ShareResponse
import com.ownstd.project.pincard.internal.domain.repository.LookRepository
import com.ownstd.project.pincard.internal.domain.usecase.LookUseCase
import com.ownstd.project.pincard.internal.presentation.viewmodel.GenerateLooksError
import com.ownstd.project.pincard.internal.presentation.viewmodel.LooksViewModel
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
import kotlin.test.assertFalse
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class LooksViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val repo = GenerateRepository()
    private val viewModel get() = LooksViewModel(LookUseCase(repo), dispatcher)

    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun `empty look ids report no new looks and keep the list untouched`() = runTest(dispatcher) {
        repo.generateResult = GenerateLooksResult.Success(emptyList())
        val vm = viewModel
        vm.generateLooks()
        advanceUntilIdle()
        assertEquals(GenerateLooksError.NoNewLooks, vm.generateError.value)
        assertEquals(0, repo.getLooksCalls)
        assertFalse(vm.isGenerating.value)
    }

    @Test fun `non empty look ids reload looks without error`() = runTest(dispatcher) {
        repo.generateResult = GenerateLooksResult.Success(listOf(7))
        repo.looks = listOf(Look(id = 7, name = "Образ", url = ""))
        val vm = viewModel
        vm.generateLooks()
        advanceUntilIdle()
        assertNull(vm.generateError.value)
        assertEquals(1, repo.getLooksCalls)
        assertEquals(repo.looks, vm.looks.value)
    }

    @Test fun `not enough clothes keeps its own error`() = runTest(dispatcher) {
        repo.generateResult = GenerateLooksResult.NotEnoughClothes
        val vm = viewModel
        vm.generateLooks()
        advanceUntilIdle()
        assertEquals(GenerateLooksError.NotEnoughClothes, vm.generateError.value)
        assertEquals(0, repo.getLooksCalls)
    }

    @Test fun `network failure keeps its own error`() = runTest(dispatcher) {
        repo.generateResult = GenerateLooksResult.NetworkError(IllegalStateException("HTTP 500"))
        val vm = viewModel
        vm.generateLooks()
        advanceUntilIdle()
        assertEquals(GenerateLooksError.NetworkError, vm.generateError.value)
        assertEquals(0, repo.getLooksCalls)
    }
}

private class GenerateRepository : LookRepository {
    var generateResult: GenerateLooksResult = GenerateLooksResult.Success(emptyList())
    var looks: List<Look> = emptyList()
    var getLooksCalls = 0

    override suspend fun getLooks(): List<Look> {
        getLooksCalls++
        return looks
    }

    override suspend fun generateLooks(): GenerateLooksResult = generateResult

    override suspend fun addLook(look: DraftLook, image: ByteArray): LookRepositoryResult<Unit> = error("unused")
    override suspend fun getLookById(lookId: Int): Look? = error("unused")
    override suspend fun getLookByShareToken(token: String): Look? = error("unused")
    override suspend fun addLookByShareToken(sharedToken: String) = error("unused")
    override suspend fun addClotheByShareToken(shareToken: String, clotheId: Int) = error("unused")
    override suspend fun deleteLook(lookId: Int) = error("unused")
    override suspend fun shareLook(lookId: Int): ShareResponse? = error("unused")
    override suspend fun getAffiliateLink(storeUrl: String): String? = error("unused")
}
