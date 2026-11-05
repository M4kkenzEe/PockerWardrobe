package com.ownstd.project.pincard.internal.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ownstd.project.pincard.internal.data.model.GenerateLooksResult
import com.ownstd.project.pincard.internal.data.model.Look
import com.ownstd.project.pincard.internal.data.model.MissingCategory
import kotlinx.coroutines.CancellationException
import com.ownstd.project.pincard.internal.domain.usecase.LookUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

internal sealed class GenerateLooksError {
    data class NotEnoughClothes(val missing: List<MissingCategory>) : GenerateLooksError()
    data object NetworkError : GenerateLooksError()
    data object NoNewLooks : GenerateLooksError()
}

internal class LooksViewModel(
    private val useCase: LookUseCase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    val looks = MutableStateFlow<List<Look>>(emptyList())
    val isLoading = MutableStateFlow(true)
    val isGenerating = MutableStateFlow(false)
    val generateError = MutableStateFlow<GenerateLooksError?>(null)

    private var firstLoadComplete = false

    fun getLooks() {
        println("GGG : getlooks")
        viewModelScope.launch(ioDispatcher) {
            runCatching {
                useCase.getLooks()
            }.onSuccess {
                println("🎯 [LOOKS_VM] UseCase returned ${it.size} looks")
                looks.value = it
                println("🎯 [LOOKS_VM] StateFlow updated with ${looks.value.size} looks")
            }.onFailure { exception ->
                println("🎯 [LOOKS_VM_ERROR] ${exception::class.simpleName}: ${exception.message}")
                println(exception)
            }
            if (!firstLoadComplete) {
                firstLoadComplete = true
                isLoading.value = false
            }
        }
    }

    fun deleteLook(lookId: Int) {
        viewModelScope.launch(ioDispatcher) {
            useCase.deleteLook(lookId)
            getLooks()
        }
    }

    fun shareLook(lookId: Int, onSuccess: (String) -> Unit) {
        viewModelScope.launch(ioDispatcher) {
            runCatching {
                useCase.shareLook(lookId)
            }.onSuccess { response ->
                response?.let {
                    onSuccess(it.shareUrl)
                }
            }.onFailure { exception ->
                println("ERR shareLook ViewModel: ${exception.message}")
            }
        }
    }

    fun generateLooks() {
        if (!isGenerating.compareAndSet(expect = false, update = true)) return
        generateError.value = null
        viewModelScope.launch(ioDispatcher) {
            try {
                when (val result = useCase.generateLooks()) {
                    is GenerateLooksResult.Success ->
                        if (result.lookIds.isEmpty()) generateError.value = GenerateLooksError.NoNewLooks else getLooks()
                    is GenerateLooksResult.NotEnoughClothes ->
                        generateError.value = GenerateLooksError.NotEnoughClothes(result.missing)
                    is GenerateLooksResult.NetworkError -> generateError.value = GenerateLooksError.NetworkError
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                generateError.value = GenerateLooksError.NetworkError
            } finally {
                isGenerating.value = false
            }
        }
    }

    fun clearGenerateError() {
        generateError.value = null
    }
}