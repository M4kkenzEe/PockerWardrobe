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
    lookId: Int
) : ViewModel() {

    private val _look = MutableStateFlow<Look?>(null)
    val look: StateFlow<Look?> = _look

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
//        loadLookByUrl(lookUrl)
        loadLookById(lookId)
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
                println("Error loading look: $exception")
            }.also {
                _isLoading.value = false
            }
        }
    }

//    private fun loadLookByUrl(url: String) {
//        viewModelScope.launch(Dispatchers.IO) {
//            _isLoading.value = true
//            runCatching {
//                // Получаем все образы и находим нужный по URL
//                val looks = useCase.getLooks()
//                looks.find { it.url == url }
//            }.onSuccess { lookResponse ->
//                if (lookResponse != null) {
//                    _look.value = DraftLook(
//                        name = lookResponse.name,
//                        lookItems = lookResponse.lookItems,
////                        url = lookResponse.url
//                    )
//                }
//            }.onFailure { exception ->
//                println("Error loading look: $exception")
//            }.also {
//                _isLoading.value = false
//            }
//        }
//    }

    fun addToWardrobe() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentLook = _look.value ?: return@launch
            runCatching {
                // TODO: Реализовать добавление в гардероб
                // useCase.addLook(currentLook, byteArray)
                println("Adding look to wardrobe: ${currentLook.name}")
            }.onSuccess {
                println("Look successfully added to wardrobe")
            }.onFailure { exception ->
                println("Error adding look to wardrobe: $exception")
            }
        }
    }
}
