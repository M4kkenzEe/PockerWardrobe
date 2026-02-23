package com.ownstd.project.pincard.internal.presentation.viewmodel

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ownstd.project.pincard.internal.data.model.Clothe
import com.ownstd.project.pincard.internal.domain.usecase.WardrobeUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class WardrobeViewModel(private val useCase: WardrobeUseCase) : ViewModel() {
    init {
        getClothes()
    }

    val clothes = MutableStateFlow<List<Clothe>>(emptyList())
    fun getClothes() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                useCase.getClothes()
            }.onSuccess {
                clothes.value = it
                println("🎯 [CLOTHES_VM] Loaded ${it.size} clothes")
            }.onFailure { exception ->
                println("❌ [CLOTHES_VM_ERROR] ${exception::class.simpleName}: ${exception.message}")
                exception.printStackTrace()
            }
        }
    }

    fun loadClothe(bitmap: ImageBitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            println("🖼️ [VM_UPLOAD] Starting loadClothe")
            runCatching {
                useCase.loadClothe(bitmap)
            }.onSuccess {
                println("✅ [VM_UPLOAD] loadClothe completed, refreshing list")
                getClothes()
            }.onFailure { exception ->
                println("❌ [VM_UPLOAD_ERROR] ${exception::class.simpleName}: ${exception.message}")
                exception.printStackTrace()
            }
        }
    }

    fun uploadFromUrl(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                useCase.uploadFromUrl(url)
            }.onSuccess { clothe ->
                clothes.update { currentList ->
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