package com.ownstd.project.pincard.internal.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ownstd.project.pincard.internal.data.model.Look
import com.ownstd.project.pincard.internal.domain.WardrobeRefreshSignal
import com.ownstd.project.pincard.internal.domain.usecase.LookUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class LookDetailsViewModel(
    private val useCase: LookUseCase,
    private val wardrobeRefreshSignal: WardrobeRefreshSignal,
    lookId: Int? = null,
    shareToken: String? = null
) : ViewModel() {

    private val _look = MutableStateFlow<Look?>(null)
    val look: StateFlow<Look?> = _look

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _affiliateLinkLoadingIds = MutableStateFlow<Set<Int>>(emptySet())
    val affiliateLinkLoadingIds: StateFlow<Set<Int>> = _affiliateLinkLoadingIds

    private val _pendingAffiliateUrl = MutableStateFlow<String?>(null)
    val pendingAffiliateUrl: StateFlow<String?> = _pendingAffiliateUrl

    // Per-item add-to-wardrobe states
    private val _addingClotheIds = MutableStateFlow<Set<Int>>(emptySet())
    val addingClotheIds: StateFlow<Set<Int>> = _addingClotheIds

    private val _addedClotheIds = MutableStateFlow<Set<Int>>(emptySet())
    val addedClotheIds: StateFlow<Set<Int>> = _addedClotheIds

    private val _errorEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val errorEvent: SharedFlow<String> = _errorEvent.asSharedFlow()

    // Whole-look add state
    private val _isAddingLook = MutableStateFlow(false)
    val isAddingLook: StateFlow<Boolean> = _isAddingLook

    private val _lookAdded = MutableStateFlow(false)
    val lookAdded: StateFlow<Boolean> = _lookAdded

    init {
        when {
            lookId != null -> loadLookById(lookId)
            shareToken != null -> loadLookByShareToken(shareToken)
            else -> {
                println("LookDetailsViewModel: Error - either lookId or shareToken must be provided")
                _isLoading.value = false
            }
        }
    }

    private fun loadLookById(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            runCatching {
                useCase.getLookById(id)
            }.onSuccess { lookResponse ->
                if (lookResponse != null) _look.value = lookResponse
            }.onFailure { exception ->
                println("Error loading look by ID: $exception")
            }.also {
                _isLoading.value = false
            }
        }
    }

    private fun loadLookByShareToken(token: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            runCatching {
                useCase.getLookByToken(token)
            }.onSuccess { lookResponse ->
                if (lookResponse != null) _look.value = lookResponse
            }.onFailure { exception ->
                println("Error loading look by share token: $exception")
            }.also {
                _isLoading.value = false
            }
        }
    }

    fun requestAffiliateLink(clotheId: Int, storeUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _affiliateLinkLoadingIds.update { it + clotheId }
            val url = runCatching { useCase.getAffiliateLink(storeUrl) }.getOrNull() ?: storeUrl
            _affiliateLinkLoadingIds.update { it - clotheId }
            _pendingAffiliateUrl.value = url
        }
    }

    fun consumeAffiliateUrl() {
        _pendingAffiliateUrl.value = null
    }

    fun addToWardrobe(token: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isAddingLook.value = true
            runCatching {
                useCase.addLookByShareToken(token)
            }.onSuccess {
                _lookAdded.value = true
                wardrobeRefreshSignal.emit()
            }.onFailure { exception ->
                _errorEvent.tryEmit("Не удалось добавить образ")
                println("Error adding look to wardrobe: $exception")
            }.also {
                _isAddingLook.value = false
            }
        }
    }

    fun addClotheToWardrobe(shareToken: String, clotheId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _addingClotheIds.update { it + clotheId }
            runCatching {
                useCase.addClotheByShareToken(shareToken, clotheId)
            }.onSuccess {
                _addedClotheIds.update { it + clotheId }
                wardrobeRefreshSignal.emit()
            }.onFailure { exception ->
                _errorEvent.tryEmit("Не удалось добавить вещь")
                println("Error adding clothe $clotheId: ${exception.message}")
            }.also {
                _addingClotheIds.update { it - clotheId }
            }
        }
    }
}
