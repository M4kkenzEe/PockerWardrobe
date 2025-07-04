package com.ownstd.project.pincard.internal.presentation.viewmodel

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ownstd.project.pincard.internal.data.model.Clothe
import com.ownstd.project.pincard.internal.domain.WardrobeUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

internal class WardrobeViewModel(private val useCase: WardrobeUseCase) : ViewModel() {
    init {
        getClothes()
    }

    val clothes = MutableStateFlow<List<Clothe>>(emptyList())
    fun getClothes() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                useCase.getClothes()
            }.onSuccess {
                clothes.value = it
            }.onFailure { exception ->
                println(exception)
            }
        }
    }

    fun loadClothe(bitmap: ImageBitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            useCase.loadClothe(bitmap)
            getClothes()
        }
    }
}