package com.ownstd.project.pincard.internal.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ownstd.project.pincard.internal.data.model.Look
import com.ownstd.project.pincard.internal.domain.usecase.LookUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TinderOutfitState(
    val current: Look? = null,
    val upcoming: List<Look> = emptyList(),
    val isLoading: Boolean = false,
    val isEmpty: Boolean = false
)

internal class TinderOutfitViewModel(private val useCase: LookUseCase) : ViewModel() {

    private val _state = MutableStateFlow(TinderOutfitState(isLoading = true))
    val state = _state.asStateFlow()

    private val fullQueue = mutableListOf<Look>()
    private val likedLookIds = mutableSetOf<Int>()
    private val skippedLookIds = mutableSetOf<Int>()

    init {
        loadBatch()
    }

    fun loadBatch() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = _state.value.copy(isLoading = true)
            runCatching { useCase.getLooks() }.onSuccess { looks ->
                val newLooks = looks.filter { it.id !in likedLookIds && it.id !in skippedLookIds }
                fullQueue.clear()
                fullQueue.addAll(newLooks)
                updateStateFromQueue()
            }.onFailure {
                _state.value = TinderOutfitState(isLoading = false, isEmpty = fullQueue.isEmpty())
            }
        }
    }

    fun onSkip(lookId: Int) {
        lookId.let { skippedLookIds.add(it) }
        fullQueue.removeAll { it.id == lookId }
        updateStateFromQueue()
        if (fullQueue.size < 2) loadBatch()
    }

    fun onLike(lookId: Int) {
        lookId.let { likedLookIds.add(it) }
        fullQueue.removeAll { it.id == lookId }
        updateStateFromQueue()
        if (fullQueue.size < 2) loadBatch()
    }

    private fun updateStateFromQueue() {
        _state.value = TinderOutfitState(
            current = fullQueue.firstOrNull(),
            upcoming = fullQueue.drop(1).take(2),
            isLoading = false,
            isEmpty = fullQueue.isEmpty()
        )
    }
}
