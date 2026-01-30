package com.ownstd.project.pincard.internal.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ownstd.project.pincard.internal.data.model.Look
import com.ownstd.project.pincard.internal.domain.usecase.LookUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class LooksViewModel(private val useCase: LookUseCase) : ViewModel() {
    private val _looks = MutableStateFlow<List<Look>>(emptyList())
    val looks: StateFlow<List<Look>> = _looks.asStateFlow()

    fun getLooks() {
        println("GGG : getlooks")
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                useCase.getLooks()
            }.onSuccess {
                println("🎯 [LOOKS_VM] UseCase returned ${it.size} looks")
                _looks.value = it
                println("🎯 [LOOKS_VM] StateFlow updated with ${_looks.value.size} looks")
            }.onFailure { exception ->
                println("🎯 [LOOKS_VM_ERROR] ${exception::class.simpleName}: ${exception.message}")
                println(exception)
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
}