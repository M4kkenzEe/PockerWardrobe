package com.ownstd.project.pincard.internal.presentation.viewmodel

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ownstd.project.pincard.internal.data.model.Clothe
import com.ownstd.project.pincard.internal.domain.FreemiumLimitException
import com.ownstd.project.pincard.internal.domain.WardrobeRefreshSignal
import com.ownstd.project.pincard.internal.domain.usecase.WardrobeUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class WardrobeViewModel(
    private val useCase: WardrobeUseCase,
    private val wardrobeRefreshSignal: WardrobeRefreshSignal
) : ViewModel() {
    val clothes = MutableStateFlow<List<Clothe>>(emptyList())
    val selectedOccasionFilter = MutableStateFlow<String?>(null)
    val isLoading = MutableStateFlow(true)
    val isUploading = MutableStateFlow(false)
    val uploadError = MutableStateFlow(false)
    val showPaywall = MutableStateFlow(false)

    private var firstLoadComplete = false

    init {
        getClothes()
        viewModelScope.launch {
            wardrobeRefreshSignal.flow.collect { getClothes() }
        }
    }

    fun clearUploadError() {
        uploadError.value = false
    }

    fun dismissPaywall() {
        showPaywall.value = false
    }

    fun getClothes(occasion: String? = selectedOccasionFilter.value) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                useCase.getClothes(occasion)
            }.onSuccess {
                clothes.value = it
                println("🎯 [CLOTHES_VM] Loaded ${it.size} clothes occasion=$occasion")
            }.onFailure { exception ->
                println("❌ [CLOTHES_VM_ERROR] ${exception::class.simpleName}: ${exception.message}")
                exception.printStackTrace()
            }
            if (!firstLoadComplete) {
                firstLoadComplete = true
                isLoading.value = false
            }
        }
    }

    fun setOccasionFilter(occasion: String?) {
        selectedOccasionFilter.value = occasion
        getClothes(occasion)
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
                isUploading.value = false
                if (exception is FreemiumLimitException) {
                    showPaywall.value = true
                } else {
                    exception.printStackTrace()
                    uploadError.value = true
                }
            }
        }
    }

    fun uploadFromUrl(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                useCase.uploadFromUrl(url)
            }.onSuccess { clothe ->
                clothes.update { currentList -> currentList + clothe }
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
