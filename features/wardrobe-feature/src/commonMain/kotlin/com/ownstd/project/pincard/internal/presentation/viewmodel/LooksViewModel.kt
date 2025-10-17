package com.ownstd.project.pincard.internal.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ownstd.project.pincard.internal.data.model.Look
import com.ownstd.project.pincard.internal.domain.usecase.LookUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

internal class LooksViewModel(private val useCase: LookUseCase) : ViewModel() {
    val looks = MutableStateFlow<List<Look>>(emptyList())
    fun getLooks() {
        println("GGG : getlooks")
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

    fun deleteLook(lookId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            useCase.deleteLook(lookId)
            getLooks()
        }
    }
}