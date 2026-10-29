package com.ownstd.project.pincard.internal.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ownstd.project.pincard.internal.data.model.Clothe
import com.ownstd.project.pincard.internal.data.model.DraftLookPreview
import com.ownstd.project.pincard.internal.data.model.GenerateDraftsResult
import com.ownstd.project.pincard.internal.data.model.MissingCategory
import com.ownstd.project.pincard.internal.domain.WardrobeRefreshSignal
import com.ownstd.project.pincard.internal.domain.usecase.LookUseCase
import com.ownstd.project.pincard.internal.domain.usecase.WardrobeUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch

internal data class ClothingDetailState(
    val clothe: Clothe? = null,
    val details: ClothingDetailUiModel? = null,
    val draft: Map<ClothingField, String> = emptyMap(),
    val isLoading: Boolean = true,
    val isEditMode: Boolean = false,
    val isSaving: Boolean = false,
    val error: ClothingDetailError? = null,
    val isOwned: Boolean? = null,
    val isSavingToWardrobe: Boolean = false,
    val didSave: Boolean = false,
)

internal sealed class WearWithState {
    data object Idle : WearWithState()
    data object Loading : WearWithState()
    data class Success(val looks: List<DraftLookPreview>) : WearWithState()
    data object AllSaved : WearWithState()
    data class NeedMore(val missing: List<MissingCategory>) : WearWithState()
    data object Unsupported : WearWithState()
    data object Error : WearWithState()
}

internal class ClothingDetailViewModel(
    private val useCase: WardrobeUseCase,
    private val lookUseCase: LookUseCase,
    private val wardrobeRefreshSignal: WardrobeRefreshSignal,
    private val clotheId: Int,
    private val preloadedClothe: Clothe? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    val state = MutableStateFlow(ClothingDetailState())
    // Отдельно от state: retry() и onSave() пересоздают state целиком, а результат подбора должен пережить retry.
    val wearWith = MutableStateFlow<WearWithState>(WearWithState.Idle)
    val savedToWardrobeEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private var loadJob: Job? = null
    private var wearWithJob: Job? = null

    init { retry() }

    fun retry() {
        if (loadJob?.isActive == true) return
        state.update {
            it.copy(
                clothe = it.clothe ?: preloadedClothe,
                details = it.details ?: preloadedClothe?.toDetailUiModel(),
                isLoading = true,
                error = null,
            )
        }
        loadJob = viewModelScope.launch(ioDispatcher) {
            try {
                val owned = useCase.getClothes().find { it.id == clotheId }
                val clothe = owned ?: preloadedClothe
                state.value = if (clothe == null) {
                    ClothingDetailState(isLoading = false, error = ClothingDetailError.NotFound)
                } else {
                    ClothingDetailState(
                        clothe = clothe, details = clothe.toDetailUiModel(),
                        isLoading = false, isOwned = owned != null,
                    )
                }
            } catch (ex: Exception) {
                if (ex is CancellationException) throw ex
                state.update {
                    it.copy(
                        isLoading = false,
                        error = if (it.clothe == null) ClothingDetailError.Load else ClothingDetailError.Ownership,
                    )
                }
            }
        }
    }

    fun onWearWithClick() {
        val current = state.value
        val id = current.clothe?.id ?: return
        if (current.isOwned != true || current.isEditMode) return
        if (wearWith.value != WearWithState.Idle && wearWith.value != WearWithState.Error) return
        wearWith.value = WearWithState.Loading
        wearWithJob = viewModelScope.launch(ioDispatcher) {
            val result = try {
                lookUseCase.generateDrafts(id)
            } catch (ex: Exception) {
                if (ex is CancellationException) throw ex
                GenerateDraftsResult.Error
            }
            ensureActive()
            wearWith.value = result.toWearWithState()
        }
    }

    private fun resetWearWith() {
        wearWithJob?.cancel()
        wearWith.value = WearWithState.Idle
    }

    fun onEditToggle() {
        val current = state.value
        if (current.isOwned != true || current.isSaving) return
        state.value = current.copy(
            isEditMode = !current.isEditMode,
            draft = current.clothe?.toEditFields().orEmpty(),
            error = null, didSave = false,
        )
    }

    fun onFieldChange(field: ClothingField, value: String) {
        state.update {
            if (!it.isEditMode || it.isSaving) it
            else it.copy(draft = it.draft + (field to value), error = null)
        }
    }

    fun onStoreOpenFailed() {
        state.update { it.copy(error = ClothingDetailError.OpenStore) }
    }

    fun saveToWardrobe() {
        val current = state.value
        if (current.isOwned != false || current.isSavingToWardrobe) return
        val id = current.clothe?.id ?: return
        state.value = current.copy(isSavingToWardrobe = true, error = null)
        viewModelScope.launch(ioDispatcher) {
            try {
                useCase.saveToWardrobe(id)
                wardrobeRefreshSignal.emit()
                state.update { it.copy(isOwned = true, isSavingToWardrobe = false) }
                savedToWardrobeEvent.emit(Unit)
            } catch (ex: Exception) {
                if (ex is CancellationException) throw ex
                state.update { it.copy(isSavingToWardrobe = false, error = ClothingDetailError.Add) }
            }
        }
    }

    fun onSave() {
        val current = state.value
        if (!current.isEditMode || current.isOwned != true || current.isSaving) return
        val clothe = current.clothe ?: return
        val id = clothe.id ?: return
        val draft = current.draft.mapValues { it.value.trim() }
        if (draft[ClothingField.Name].isNullOrBlank()) {
            state.update { it.copy(error = ClothingDetailError.NameRequired) }
            return
        }
        val storeUrl = draft[ClothingField.StoreUrl].orEmpty()
        if (storeUrl.isNotEmpty() && validatedStoreUrl(storeUrl) == null) {
            state.update { it.copy(error = ClothingDetailError.InvalidUrl) }
            return
        }
        val original = clothe.toEditFields()
        fun changed(field: ClothingField): String? = draft[field]?.takeIf { it != original[field] }
        if (draft.all { (field, value) -> value == original[field] }) {
            state.update { it.copy(isEditMode = false, error = null) }
            return
        }
        state.value = current.copy(isSaving = true, error = null)
        viewModelScope.launch(ioDispatcher) {
            try {
                val updated = useCase.updateClothe(
                    clotheId = id,
                    name = changed(ClothingField.Name),
                    storeUrl = changed(ClothingField.StoreUrl),
                    season = changed(ClothingField.Season),
                    fit = changed(ClothingField.Fit),
                    material = changed(ClothingField.Material),
                    brand = changed(ClothingField.Brand),
                    occasion = changed(ClothingField.Occasion),
                    styleTags = changed(ClothingField.Style),
                )
                state.value = ClothingDetailState(
                    clothe = updated, details = updated.toDetailUiModel(),
                    isLoading = false, isOwned = true, didSave = true,
                )
                resetWearWith()
                wardrobeRefreshSignal.emit()
            } catch (ex: Exception) {
                if (ex is CancellationException) throw ex
                state.update { it.copy(isSaving = false, error = ClothingDetailError.Save) }
            }
        }
    }
}

private fun GenerateDraftsResult.toWearWithState(): WearWithState = when (this) {
    is GenerateDraftsResult.Success -> WearWithState.Success(looks)
    GenerateDraftsResult.AllSaved -> WearWithState.AllSaved
    is GenerateDraftsResult.NeedMore -> WearWithState.NeedMore(missing)
    GenerateDraftsResult.Unsupported -> WearWithState.Unsupported
    GenerateDraftsResult.Error -> WearWithState.Error
}
