package com.ownstd.project.pincard.internal.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ownstd.project.pincard.internal.data.model.Clothe
import com.ownstd.project.pincard.internal.domain.usecase.WardrobeUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

data class ClothingDetailState(
    val clothe: Clothe? = null,
    val isEditMode: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
)

internal class ClothingDetailViewModel(
    private val useCase: WardrobeUseCase,
    private val clotheId: Int,
) : ViewModel() {

    val state = MutableStateFlow(ClothingDetailState())

    val editName = MutableStateFlow("")
    val editStoreUrl = MutableStateFlow("")
    val editSeason = MutableStateFlow("")
    val editFit = MutableStateFlow("")
    val editMaterial = MutableStateFlow("")
    val editBrand = MutableStateFlow("")
    val editOccasion = MutableStateFlow("")
    val editStyleTags = MutableStateFlow("")

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { useCase.getClothes() }
                .onSuccess { clothes ->
                    val clothe = clothes.find { it.id == clotheId }
                    state.value = ClothingDetailState(clothe = clothe)
                    clothe?.let { initEditFields(it) }
                }
                .onFailure { ex ->
                    state.value = ClothingDetailState(error = ex.message ?: "Ошибка загрузки")
                }
        }
    }

    private fun initEditFields(clothe: Clothe) {
        editName.value = clothe.name
        editStoreUrl.value = clothe.storeUrl ?: ""
        editSeason.value = clothe.season ?: ""
        editFit.value = clothe.fit ?: ""
        editMaterial.value = clothe.material ?: ""
        editBrand.value = clothe.brand ?: ""
        editOccasion.value = clothe.occasion ?: ""
        editStyleTags.value = clothe.styleTags ?: ""
    }

    fun onEditToggle() {
        val current = state.value
        if (!current.isEditMode) {
            current.clothe?.let { initEditFields(it) }
        }
        state.value = current.copy(isEditMode = !current.isEditMode, error = null)
    }

    fun onSave() {
        val current = state.value
        val clothe = current.clothe ?: return
        state.value = current.copy(isSaving = true, error = null)
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                useCase.updateClothe(
                    clotheId = clothe.id!!,
                    name = editName.value.takeIf { it.isNotBlank() },
                    storeUrl = editStoreUrl.value.takeIf { it.isNotBlank() },
                    season = editSeason.value.takeIf { it.isNotBlank() },
                    fit = editFit.value.takeIf { it.isNotBlank() },
                    material = editMaterial.value.takeIf { it.isNotBlank() },
                    brand = editBrand.value.takeIf { it.isNotBlank() },
                    occasion = editOccasion.value.takeIf { it.isNotBlank() },
                    styleTags = editStyleTags.value.takeIf { it.isNotBlank() },
                )
            }.onSuccess { updated ->
                state.value = ClothingDetailState(clothe = updated)
                initEditFields(updated)
            }.onFailure { ex ->
                state.value = state.value.copy(isSaving = false, error = ex.message ?: "Ошибка сохранения")
            }
        }
    }
}
