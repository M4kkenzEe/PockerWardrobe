package com.ownstd.project.pincard.internal.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ownstd.project.pincard.internal.data.model.LookResponse
import com.ownstd.project.pincard.internal.domain.usecase.LookUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

internal class LooksViewModel(private val useCase: LookUseCase) : ViewModel() {
    init {
        getLooks()
    }

    val looks = MutableStateFlow<List<LookResponse>>(emptyList())
    fun getLooks() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                useCase.getLooks()
            }.onSuccess {
                looks.value = it
            }.onFailure { exception ->
                println(exception)
            }
        }
    }
}