package com.ownstd.project.card.internal.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FirstViewModel : ViewModel() {
    private var _state = MutableStateFlow(0)
    val state: StateFlow<Int> = _state.asStateFlow()

    private fun hello() = (0..100).random()

    fun showNumber() {
        viewModelScope.launch {
            delay(5000)
            _state.value = hello()
        }
    }

    init {
        showNumber()
    }
}