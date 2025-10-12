package com.ownstd.project.pincard.internal.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ownstd.project.pincard.internal.data.model.Clothe
import com.ownstd.project.pincard.internal.data.model.Look
import com.ownstd.project.pincard.internal.data.model.LookResponse
import com.ownstd.project.pincard.internal.data.model.LookRepositoryResult
import com.ownstd.project.pincard.internal.domain.usecase.LookUseCase
import com.ownstd.project.pincard.internal.domain.usecase.WardrobeUseCase
import com.ownstd.project.pincard.internal.presentation.model.LookItemUiState
import com.ownstd.project.pincard.internal.presentation.model.mapper
import com.ownstd.project.pincard.internal.presentation.model.toData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ConstructorViewModel(
    private val wardrobeUseCase: WardrobeUseCase,
    private val lookUseCase: LookUseCase
) : ViewModel() {
    private val _pickedClotheList = MutableStateFlow<List<LookItemUiState>>(emptyList())
    val pickedClotheList = _pickedClotheList.asStateFlow()

    val clotheList = MutableStateFlow<List<Clothe>>(emptyList())

    private var maxZIndex = 0f
    private var lastInteractedPhotoId: Int? = null

    private fun updateWardrobe() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                wardrobeUseCase.getClothes()
            }.onSuccess {
                clotheList.value = it
            }.onFailure { exception ->
                println(exception)
            }
        }
    }

    fun pickClothe(clothe: Clothe) {
        _pickedClotheList.update { list ->
            list + mapper(clothe)
        }
        setLastInteracted(clothe.id!!)
    }

    fun update(id: Int, transform: (LookItemUiState) -> LookItemUiState) {
        _pickedClotheList.update { list ->
            list.map { if (it.id == id) transform(it) else it }
        }
    }

    fun bringToFront(photoId: Int) {
        maxZIndex += 1f
        _pickedClotheList.update { list ->
            list.map { photo ->
                if (photo.id == photoId) photo.copy(zIndex = maxZIndex)
                else photo
            }
        }
        setLastInteracted(photoId)
    }

    fun deleteLastInteracted() {
        lastInteractedPhotoId?.let { id ->
            _pickedClotheList.update { list ->
                list.filter { it.id != id }
            }
            lastInteractedPhotoId = null
        }
    }

    fun setLastInteracted(photoId: Int) {
        lastInteractedPhotoId = photoId
    }

    fun save(image: ByteArray) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = lookUseCase.addLook(
                look = Look(
                    name = "",
                    lookItems = _pickedClotheList.value
                        .sortedBy { it.zIndex }
                        .map { it.toData() },
                ),
                image = image
            )

            when (result) {
                is LookRepositoryResult.BadRequest -> {
                    // Показать ошибку пользователю result.message
                }

                is LookRepositoryResult.InternalServerError -> {
                    // Сообщить о проблемах сервера
                }

                is LookRepositoryResult.NetworkError -> {
                    // Показать ошибку сети
                }

                else -> println("ADDED")
            }
        }
    }


    init {
        updateWardrobe()
    }

}

