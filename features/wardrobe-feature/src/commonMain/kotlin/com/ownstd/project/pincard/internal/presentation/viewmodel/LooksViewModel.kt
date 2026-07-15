package com.ownstd.project.pincard.internal.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ownstd.project.pincard.internal.data.model.GenerateLooksResult
import com.ownstd.project.pincard.internal.data.model.Look
import com.ownstd.project.pincard.internal.domain.usecase.LookUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

internal sealed class GenerateLooksError {
    data object NotEnoughClothes : GenerateLooksError()
    data object NetworkError : GenerateLooksError()
}

internal class LooksViewModel(private val useCase: LookUseCase) : ViewModel() {
    val looks = MutableStateFlow<List<Look>>(emptyList())
    val isLoading = MutableStateFlow(true)
    val isGenerating = MutableStateFlow(false)
    val generateError = MutableStateFlow<GenerateLooksError?>(null)

    private var firstLoadComplete = false

    fun getLooks() {
        println("GGG : getlooks")
        viewModelScope.launch(Dispatchers.IO) {
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
        viewModelScope.launch(Dispatchers.IO) {
            useCase.deleteLook(lookId)
            getLooks()
        }
    }

    fun shareLook(lookId: Int, onSuccess: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
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
        viewModelScope.launch(Dispatchers.IO) {
            isGenerating.value = true
            when (val result = useCase.generateLooks()) {
                is GenerateLooksResult.Success -> getLooks()
                is GenerateLooksResult.NotEnoughClothes -> generateError.value = GenerateLooksError.NotEnoughClothes
                is GenerateLooksResult.NetworkError -> generateError.value = GenerateLooksError.NetworkError
            }
            isGenerating.value = false
        }
    }

    fun clearGenerateError() {
        generateError.value = null
    }
}