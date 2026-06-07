package com.ownstd.project.pincard.internal.presentation.viewmodel

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ownstd.project.pincard.internal.data.model.Clothe
import com.ownstd.project.pincard.internal.domain.usecase.WardrobeUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class WardrobeViewModel(private val useCase: WardrobeUseCase) : ViewModel() {
    init {
        getClothes()
    }

    private val allClothes = MutableStateFlow<List<Clothe>>(emptyList())
    val selectedOccasionFilter = MutableStateFlow<String?>(null)

    val clothes = combine(allClothes, selectedOccasionFilter) { list, filter ->
        if (filter == null) list
        else list.filter { it.occasion == filter }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val isUploading = MutableStateFlow(false)
    val uploadError = MutableStateFlow(false)

    fun setOccasionFilter(occasion: String?) {
        selectedOccasionFilter.value = occasion
    }

    fun clearUploadError() {
        uploadError.value = false
    }

    fun getClothes() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                useCase.getClothes()
            }.onSuccess {
                allClothes.value = it
                println("🎯 [CLOTHES_VM] Loaded ${it.size} clothes")
            }.onFailure { exception ->
                println("❌ [CLOTHES_VM_ERROR] ${exception::class.simpleName}: ${exception.message}")
                exception.printStackTrace()
            }
        }
    }

    fun loadClothe(bitmap: ImageBitmap, occasion: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            println("🖼️ [VM_UPLOAD] Starting loadClothe occasion=$occasion")
            isUploading.value = true
            runCatching {
                useCase.loadClothe(bitmap, occasion)
            }.onSuccess {
                println("✅ [VM_UPLOAD] loadClothe completed, refreshing list")
                isUploading.value = false
                getClothes()
            }.onFailure { exception ->
                println("❌ [VM_UPLOAD_ERROR] ${exception::class.simpleName}: ${exception.message}")
                exception.printStackTrace()
                isUploading.value = false
                uploadError.value = true
            }
        }
    }

    fun uploadFromUrl(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                useCase.uploadFromUrl(url)
            }.onSuccess { clothe ->
                allClothes.update { currentList ->
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