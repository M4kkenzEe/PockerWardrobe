package com.ownstd.project.outfitconstructor.internal.presentation.root

import com.ownstd.project.core.compose.foundation.Outcome
import com.ownstd.project.outfitconstructor.internal.domain.model.DraftLook
import com.ownstd.project.outfitconstructor.internal.domain.usecase.AddLookUseCase
import com.ownstd.project.outfitconstructor.internal.domain.usecase.GetClothesUseCase
import com.ownstd.project.outfitconstructor.internal.presentation.root.interactionModel.CanvasItem
import com.ownstd.project.outfitconstructor.internal.presentation.root.interactionModel.OutfitConstructorIntent
import com.ownstd.project.outfitconstructor.internal.presentation.root.interactionModel.OutfitConstructorSideEffect
import com.ownstd.project.outfitconstructor.internal.presentation.root.interactionModel.OutfitConstructorState
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.reduce

fun outfitConstructorStore(
    getClothesUseCase: GetClothesUseCase,
    addLookUseCase: AddLookUseCase,
    onSideEffect: (OutfitConstructorSideEffect) -> Unit,
): Store<OutfitConstructorState, OutfitConstructorIntent, Nothing> = store(
    initial = OutfitConstructorState()
) {
    init {
        updateState { copy(isLoadingClothes = true) }
        getClothesUseCase().collect { result ->
            when (result) {
                is Outcome.Success -> updateState {
                    copy(availableClothes = result.data, isLoadingClothes = false)
                }
                is Outcome.Empty -> updateState {
                    copy(availableClothes = emptyList(), isLoadingClothes = false)
                }
                is Outcome.Error -> {
                    updateState { copy(isLoadingClothes = false) }
                    onSideEffect(OutfitConstructorSideEffect.ShowError(result.message))
                }
            }
        }
    }

    reduce { intent ->
        when (intent) {
            is OutfitConstructorIntent.LoadClothes -> {
                updateState { copy(isLoadingClothes = true) }
                getClothesUseCase().collect { result ->
                    when (result) {
                        is Outcome.Success -> updateState {
                            copy(availableClothes = result.data, isLoadingClothes = false)
                        }
                        is Outcome.Empty -> updateState {
                            copy(availableClothes = emptyList(), isLoadingClothes = false)
                        }
                        is Outcome.Error -> {
                            updateState { copy(isLoadingClothes = false) }
                            onSideEffect(OutfitConstructorSideEffect.ShowError(result.message))
                        }
                    }
                }
            }

            is OutfitConstructorIntent.AddItem -> {
                val clothe = intent.clothe
                updateState {
                    if (canvasItems.any { it.clothe.id == clothe.id }) return@updateState this
                    copy(
                        canvasItems = canvasItems + CanvasItem(
                            clothe = clothe,
                            x = 40f + canvasItems.size * 20f,
                            y = 40f + canvasItems.size * 20f,
                        ),
                        isPickerVisible = false,
                    )
                }
            }

            is OutfitConstructorIntent.SelectItem -> updateState {
                copy(
                    canvasItems = canvasItems.map { item ->
                        item.copy(isSelected = item.clothe.id == intent.clotheId)
                    }
                )
            }

            is OutfitConstructorIntent.MoveItem -> updateState {
                copy(
                    canvasItems = canvasItems.map { item ->
                        if (item.clothe.id == intent.clotheId) item.copy(x = intent.x, y = intent.y)
                        else item
                    }
                )
            }

            is OutfitConstructorIntent.ScaleItem -> updateState {
                copy(
                    canvasItems = canvasItems.map { item ->
                        if (item.clothe.id == intent.clotheId) item.copy(scale = intent.scale.coerceIn(0.3f, 3f))
                        else item
                    }
                )
            }

            is OutfitConstructorIntent.RemoveItem -> updateState {
                copy(canvasItems = canvasItems.filter { it.clothe.id != intent.clotheId })
            }

            is OutfitConstructorIntent.ResetCanvas -> updateState {
                copy(canvasItems = emptyList())
            }

            is OutfitConstructorIntent.ShowPicker -> updateState {
                copy(isPickerVisible = true)
            }

            is OutfitConstructorIntent.HidePicker -> updateState {
                copy(isPickerVisible = false)
            }

            is OutfitConstructorIntent.FilterCategory -> updateState {
                copy(activeCategory = intent.category)
            }

            is OutfitConstructorIntent.SaveClicked -> {
                var capturedItems: List<CanvasItem> = emptyList()
                updateState {
                    capturedItems = canvasItems
                    copy(isSaving = true)
                }
                if (capturedItems.isNotEmpty()) {
                    val imageBytes = intent.captureCanvas()
                    val draft = DraftLook(
                        name = "Образ",
                        clotheIds = capturedItems.mapNotNull { it.clothe.id },
                    )
                    val result = addLookUseCase(draft, imageBytes)
                    updateState { copy(isSaving = false) }
                    if (result.isSuccess) {
                        onSideEffect(OutfitConstructorSideEffect.NavigateBack)
                    } else {
                        onSideEffect(
                            OutfitConstructorSideEffect.ShowError(
                                result.exceptionOrNull()?.message ?: "Ошибка сохранения"
                            )
                        )
                    }
                } else {
                    updateState { copy(isSaving = false) }
                }
            }

            is OutfitConstructorIntent.BackClicked ->
                onSideEffect(OutfitConstructorSideEffect.NavigateBack)
        }
    }
}
