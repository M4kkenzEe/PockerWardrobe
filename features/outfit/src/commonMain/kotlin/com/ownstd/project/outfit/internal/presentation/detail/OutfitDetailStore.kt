package com.ownstd.project.outfit.internal.presentation.detail

import com.ownstd.project.core.compose.foundation.Outcome
import com.ownstd.project.outfit.internal.domain.usecase.DeleteLookUseCase
import com.ownstd.project.outfit.internal.domain.usecase.GetLookByIdUseCase
import com.ownstd.project.outfit.internal.domain.usecase.ShareLookUseCase
import com.ownstd.project.outfit.internal.presentation.detail.interactionModel.OutfitDetailIntent
import com.ownstd.project.outfit.internal.presentation.detail.interactionModel.OutfitDetailSideEffect
import com.ownstd.project.outfit.internal.presentation.detail.interactionModel.OutfitDetailState
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.reduce

fun outfitDetailStore(
    getLookByIdUseCase: GetLookByIdUseCase,
    deleteLookUseCase: DeleteLookUseCase,
    shareLookUseCase: ShareLookUseCase,
    lookId: Int,
    onSideEffect: (OutfitDetailSideEffect) -> Unit,
): Store<OutfitDetailState, OutfitDetailIntent, Nothing> = store(
    initial = OutfitDetailState(isLoading = true)
) {
    init {
        updateState { copy(isLoading = true) }
        getLookByIdUseCase(lookId).collect { result ->
            when (result) {
                is Outcome.Success -> updateState { copy(look = result.data, isLoading = false) }
                is Outcome.Empty -> {
                    updateState { copy(isLoading = false) }
                    onSideEffect(OutfitDetailSideEffect.NavigateBack)
                }
                is Outcome.Error -> {
                    updateState { copy(isLoading = false) }
                    onSideEffect(OutfitDetailSideEffect.ShowError(result.message))
                }
            }
        }
    }

    reduce { intent ->
        when (intent) {
            is OutfitDetailIntent.Load -> {
                updateState { copy(isLoading = true) }
                getLookByIdUseCase(intent.lookId).collect { result ->
                    when (result) {
                        is Outcome.Success -> updateState { copy(look = result.data, isLoading = false) }
                        is Outcome.Empty -> {
                            updateState { copy(isLoading = false) }
                            onSideEffect(OutfitDetailSideEffect.NavigateBack)
                        }
                        is Outcome.Error -> {
                            updateState { copy(isLoading = false) }
                            onSideEffect(OutfitDetailSideEffect.ShowError(result.message))
                        }
                    }
                }
            }

            is OutfitDetailIntent.MenuClicked -> Unit

            is OutfitDetailIntent.EditClicked -> {
                var capturedId: Int? = null
                updateState { capturedId = look?.id; this }
                val id = capturedId ?: return@reduce
                onSideEffect(OutfitDetailSideEffect.NavigateToConstructor(id))
            }

            is OutfitDetailIntent.DeleteClicked ->
                updateState { copy(showDeleteConfirm = true) }

            is OutfitDetailIntent.DeleteCancelled ->
                updateState { copy(showDeleteConfirm = false) }

            is OutfitDetailIntent.DeleteConfirmed -> {
                var capturedId: Int? = null
                updateState { capturedId = look?.id; this }
                val id = capturedId ?: return@reduce
                updateState { copy(showDeleteConfirm = false) }
                val result = deleteLookUseCase(id)
                if (result.isSuccess) {
                    onSideEffect(OutfitDetailSideEffect.NavigateBack)
                } else {
                    onSideEffect(
                        OutfitDetailSideEffect.ShowError(
                            result.exceptionOrNull()?.message ?: "Ошибка удаления"
                        )
                    )
                }
            }

            is OutfitDetailIntent.ShareClicked -> {
                var capturedId: Int? = null
                updateState { capturedId = look?.id; this }
                val id = capturedId ?: return@reduce
                val result = shareLookUseCase(id)
                if (result.isFailure) {
                    onSideEffect(
                        OutfitDetailSideEffect.ShowError(
                            result.exceptionOrNull()?.message ?: "Ошибка при получении ссылки"
                        )
                    )
                }
            }

            is OutfitDetailIntent.FavoriteClicked -> Unit

            is OutfitDetailIntent.ItemClicked ->
                onSideEffect(OutfitDetailSideEffect.NavigateToItem(intent.clotheId))
        }
    }
}
