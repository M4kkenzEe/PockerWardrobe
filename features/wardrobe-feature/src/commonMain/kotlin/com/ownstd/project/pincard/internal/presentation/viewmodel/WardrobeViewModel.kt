package com.ownstd.project.pincard.internal.presentation.viewmodel

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ownstd.project.pincard.internal.data.model.Clothe
import com.ownstd.project.pincard.internal.domain.usecase.WardrobeUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class WardrobeViewModel(private val useCase: WardrobeUseCase) : ViewModel() {
    init {
        getClothes()
    }

    private val _clothes = MutableStateFlow<List<Clothe>>(emptyList())
    val clothes: StateFlow<List<Clothe>> = _clothes.asStateFlow()
    fun getClothes() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                useCase.getClothes()
            }.onSuccess {
                println("🎯 [CLOTHES_VM] UseCase returned ${it.size} clothes")
                _clothes.value = it
                println("🎯 [CLOTHES_VM] StateFlow updated with ${_clothes.value.size} clothes")
            }.onFailure { exception ->
                println("🎯 [CLOTHES_VM_ERROR] ${exception::class.simpleName}: ${exception.message}")
                println(exception)
            }
        }
    }

    fun loadClothe(bitmap: ImageBitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            useCase.loadClothe(bitmap)
            getClothes()
        }
    }

    fun uploadFromUrl(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                useCase.uploadFromUrl(url)
            }.onSuccess { clothe ->
                _clothes.update { currentList ->
                    currentList + clothe
                }
            }.onFailure { exception ->
                println(exception)
            }
        }
    }

    fun deleteClothe(clotheId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            useCase.delete(clotheId)
            getClothes()
        }
    }
}