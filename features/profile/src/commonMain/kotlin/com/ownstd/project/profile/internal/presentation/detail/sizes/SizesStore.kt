package com.ownstd.project.profile.internal.presentation.detail.sizes

import com.ownstd.project.core.compose.foundation.Outcome
import com.ownstd.project.profile.internal.domain.model.UserSizesModel
import com.ownstd.project.profile.internal.domain.usecase.GetUserSizesUseCase
import com.ownstd.project.profile.internal.domain.usecase.UpdateUserSizesUseCase
import com.ownstd.project.profile.internal.presentation.detail.sizes.interactionModel.SizeField
import com.ownstd.project.profile.internal.presentation.detail.sizes.interactionModel.SizesIntent
import com.ownstd.project.profile.internal.presentation.detail.sizes.interactionModel.SizesSideEffect
import com.ownstd.project.profile.internal.presentation.detail.sizes.interactionModel.SizesState
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.reduce

internal fun sizesStore(
    getUserSizesUseCase: GetUserSizesUseCase,
    updateUserSizesUseCase: UpdateUserSizesUseCase,
    onSideEffect: (SizesSideEffect) -> Unit,
): Store<SizesState, SizesIntent, Nothing> = store(
    initial = SizesState(isLoading = true)
) {
    init {
        intent(SizesIntent.Load)
    }

    reduce { sizesIntent ->
        when (sizesIntent) {
            is SizesIntent.Load -> {
                updateState { copy(isLoading = true) }
                getUserSizesUseCase().collect { result ->
                    when (result) {
                        is Outcome.Success -> updateState {
                            copy(sizes = result.data, isLoading = false)
                        }
                        is Outcome.Empty -> updateState { copy(isLoading = false) }
                        is Outcome.Error -> {
                            updateState { copy(isLoading = false) }
                            onSideEffect(SizesSideEffect.ShowError(result.message))
                        }
                    }
                }
            }

            is SizesIntent.SelectRegion ->
                updateState { copy(activeRegion = sizesIntent.region) }

            is SizesIntent.EditField ->
                updateState { copy(editingField = sizesIntent.field) }

            is SizesIntent.DismissEdit ->
                updateState { copy(editingField = null) }

            is SizesIntent.SaveField -> {
                var updated = UserSizesModel()
                updateState {
                    val current = sizes ?: UserSizesModel()
                    updated = when (sizesIntent.field) {
                        SizeField.HEIGHT -> current.copy(height = sizesIntent.value)
                        SizeField.WEIGHT -> current.copy(weight = sizesIntent.value)
                        SizeField.CHEST  -> current.copy(chest = sizesIntent.value)
                        SizeField.WAIST  -> current.copy(waist = sizesIntent.value)
                        SizeField.HIPS   -> current.copy(hips = sizesIntent.value)
                    }
                    copy(isSaving = true, editingField = null)
                }
                updateUserSizesUseCase(updated).fold(
                    onSuccess = { saved ->
                        updateState { copy(sizes = saved, isSaving = false) }
                    },
                    onFailure = { e ->
                        updateState { copy(isSaving = false) }
                        onSideEffect(SizesSideEffect.ShowError(e.message ?: "Ошибка сохранения"))
                    }
                )
            }
        }
    }
}
