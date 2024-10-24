package com.ownstd.project.internal

import androidx.lifecycle.ViewModel
import kotlinprojecttesting.features.pin_card_feature.generated.resources.Res
import kotlinprojecttesting.features.pin_card_feature.generated.resources.ggg
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.DrawableResource

class ConstructorViewModel : ViewModel() {
    private val _clotheList = MutableStateFlow(listOf(Res.drawable.ggg))
    val clotheList: StateFlow<List<DrawableResource>> = _clotheList

    fun addClothe() {
        _clotheList.value += Res.drawable.ggg
    }
}