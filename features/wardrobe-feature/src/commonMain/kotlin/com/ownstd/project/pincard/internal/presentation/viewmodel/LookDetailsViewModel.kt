package com.ownstd.project.pincard.internal.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ownstd.project.pincard.internal.data.model.Look
import com.ownstd.project.pincard.internal.domain.WardrobeRefreshSignal
import com.ownstd.project.pincard.internal.domain.usecase.LookUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal enum class AddLookMode { Share, Draft }

internal enum class LookDetailsError { AddLook, AddClothe }

internal class LookDetailsViewModel(
    private val useCase: LookUseCase,
    private val wardrobeRefreshSignal: WardrobeRefreshSignal,
    lookId: Int? = null,
    private val shareToken: String? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    private val _look = MutableStateFlow<Look?>(null)
    val look: StateFlow<Look?> = _look

    // Черновик определяется сервером; в share-режиме вещи чужие, поэтому share приоритетнее.
    val addLookMode: StateFlow<AddLookMode?> = _look
        .map { look ->
            when {
                shareToken != null -> AddLookMode.Share
                look?.isDraft == true -> AddLookMode.Draft
                else -> null
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

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

    private val _errorEvent = MutableSharedFlow<LookDetailsError>(extraBufferCapacity = 1)
    val errorEvent: SharedFlow<LookDetailsError> = _errorEvent.asSharedFlow()

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
        viewModelScope.launch(ioDispatcher) {
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
        viewModelScope.launch(ioDispatcher) {
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
        viewModelScope.launch(ioDispatcher) {
            _affiliateLinkLoadingIds.update { it + clotheId }
            val url = runCatching { useCase.getAffiliateLink(storeUrl) }.getOrNull() ?: storeUrl
            _affiliateLinkLoadingIds.update { it - clotheId }
            _pendingAffiliateUrl.value = url
        }
    }

    fun consumeAffiliateUrl() {
        _pendingAffiliateUrl.value = null
    }

    fun onAddLookClick() {
        when (addLookMode.value) {
            AddLookMode.Share -> shareToken?.let(::addToWardrobe)
            AddLookMode.Draft -> saveDraft()
            null -> Unit
        }
    }

    private fun saveDraft() {
        val draftId = _look.value?.takeIf { it.isDraft }?.id ?: return
        if (_lookAdded.value || !_isAddingLook.compareAndSet(expect = false, update = true)) return
        viewModelScope.launch(ioDispatcher) {
            runCatching {
                useCase.saveDraft(draftId)
            }.onSuccess {
                _lookAdded.value = true
                wardrobeRefreshSignal.emit()
            }.onFailure { exception ->
                _errorEvent.tryEmit(LookDetailsError.AddLook)
                println("Error saving draft look $draftId: $exception")
            }.also {
                _isAddingLook.value = false
            }
        }
    }

    private fun addToWardrobe(token: String) {
        viewModelScope.launch(ioDispatcher) {
            _isAddingLook.value = true
            runCatching {
                useCase.addLookByShareToken(token)
            }.onSuccess {
                _lookAdded.value = true
                wardrobeRefreshSignal.emit()
            }.onFailure { exception ->
                _errorEvent.tryEmit(LookDetailsError.AddLook)
                println("Error adding look to wardrobe: $exception")
            }.also {
                _isAddingLook.value = false
            }
        }
    }

    fun addClotheToWardrobe(shareToken: String, clotheId: Int) {
        viewModelScope.launch(ioDispatcher) {
            _addingClotheIds.update { it + clotheId }
            runCatching {
                useCase.addClotheByShareToken(shareToken, clotheId)
            }.onSuccess {
                _addedClotheIds.update { it + clotheId }
                wardrobeRefreshSignal.emit()
            }.onFailure { exception ->
                _errorEvent.tryEmit(LookDetailsError.AddClothe)
                println("Error adding clothe $clotheId: ${exception.message}")
            }.also {
                _addingClotheIds.update { it - clotheId }
            }
        }
    }
}
