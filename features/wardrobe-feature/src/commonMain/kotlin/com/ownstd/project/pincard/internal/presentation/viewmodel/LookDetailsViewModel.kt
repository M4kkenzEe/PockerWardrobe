package com.ownstd.project.pincard.internal.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ownstd.project.pincard.internal.data.model.DraftLook
import com.ownstd.project.pincard.internal.data.model.Look
import com.ownstd.project.pincard.internal.domain.usecase.LookUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

internal class LookDetailsViewModel(
    private val useCase: LookUseCase,
    lookId: Int? = null,
    shareToken: String? = null
) : ViewModel() {

    private val _look = MutableStateFlow<Look?>(null)
    val look: StateFlow<Look?> = _look

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

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
                if (lookResponse != null) {
                    _look.value = lookResponse
                }
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
                if (lookResponse != null) {
                    _look.value = lookResponse
                }
            }.onFailure { exception ->
                println("Error loading look by share token: $exception")
            }.also {
                _isLoading.value = false
            }
        }
    }

    fun addToWardrobe(token: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentLook = _look.value ?: return@launch
            runCatching {
                useCase.addLookByShareToken(token)
                println("Adding look to wardrobe: ${currentLook.name}")
            }.onSuccess {
                println("Look successfully added to wardrobe")
            }.onFailure { exception ->
                println("Error adding look to wardrobe: $exception")
            }
        }
    }
}
