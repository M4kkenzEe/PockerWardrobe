package com.ownstd.project.pincard.internal.presentation.viewmodel

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ownstd.project.pincard.internal.data.model.Clothe
import com.ownstd.project.pincard.internal.data.model.GenerateDraftsResult
import com.ownstd.project.pincard.internal.domain.FreemiumLimitException
import com.ownstd.project.pincard.internal.domain.WardrobeRefreshSignal
import com.ownstd.project.pincard.internal.domain.usecase.LookUseCase
import com.ownstd.project.pincard.internal.domain.usecase.WardrobeUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class WardrobeViewModel(
    private val useCase: WardrobeUseCase,
    private val lookUseCase: LookUseCase,
    private val wardrobeRefreshSignal: WardrobeRefreshSignal,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    val clothes = MutableStateFlow<List<Clothe>>(emptyList())
    val selectedOccasionFilter = MutableStateFlow<String?>(null)
    val isLoading = MutableStateFlow(true)
    val isUploading = MutableStateFlow(false)
    val uploadError = MutableStateFlow(false)
    val showPaywall = MutableStateFlow(false)
    val anchorHookState = MutableStateFlow<AnchorHookState>(AnchorHookState.Hidden)

    private val _navigateToLookEvent = Channel<Int>(Channel.BUFFERED)
    val navigateToLookEvent: Flow<Int> = _navigateToLookEvent.receiveAsFlow()

    private var firstLoadComplete = false
    private var anchorJob: Job? = null

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

    fun dismissHook() {
        anchorJob?.cancel()
        anchorHookState.value = AnchorHookState.Hidden
    }

    fun getClothes(occasion: String? = selectedOccasionFilter.value) {
        viewModelScope.launch(ioDispatcher) {
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
        viewModelScope.launch(ioDispatcher) {
            println("🖼️ [VM_UPLOAD] Starting loadClothe occasion=$occasion")
            isUploading.value = true
            runCatching {
                useCase.loadClothe(bitmap, occasion)
            }.onSuccess { clothe ->
                println("✅ [VM_UPLOAD] loadClothe completed, refreshing list")
                isUploading.value = false
                getClothes()
                clothe.id?.let {
                    anchorJob?.cancel()
                    anchorHookState.value = AnchorHookState.Celebrate(it, clothe.imageUrl)
                }
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
        viewModelScope.launch(ioDispatcher) {
            runCatching {
                useCase.uploadFromUrl(url)
            }.onSuccess { clothe ->
                clothes.update { currentList -> currentList + clothe }
                clothe.id?.let {
                    anchorJob?.cancel()
                    anchorHookState.value = AnchorHookState.Celebrate(it, clothe.imageUrl)
                }
            }.onFailure { exception ->
                println(exception)
            }
        }
    }

    fun onGenerateDrafts(clotheId: Int) {
        if (anchorJob?.isActive == true) return
        anchorHookState.value = AnchorHookState.Generating(clotheId)
        anchorJob = viewModelScope.launch(ioDispatcher) {
            val result = try {
                lookUseCase.generateDrafts(clotheId)
            } catch (ex: Exception) {
                if (ex is CancellationException) throw ex
                GenerateDraftsResult.Error
            }
            ensureActive()
            when (result) {
                is GenerateDraftsResult.Success -> {
                    anchorHookState.value = AnchorHookState.Hidden
                    result.looks.firstOrNull()?.let { _navigateToLookEvent.trySend(it.lookId) }
                }
                GenerateDraftsResult.AllSaved -> anchorHookState.value = AnchorHookState.AllSaved
                is GenerateDraftsResult.NeedMore -> anchorHookState.value = AnchorHookState.NeedMore(result.missing)
                GenerateDraftsResult.Unsupported -> anchorHookState.value = AnchorHookState.Hidden
                GenerateDraftsResult.Error -> anchorHookState.value = AnchorHookState.Error
            }
        }
    }

    fun deleteClothe(clotheId: Int) {
        viewModelScope.launch(ioDispatcher) {
            useCase.delete(clotheId)
            getClothes()
        }
    }
}
