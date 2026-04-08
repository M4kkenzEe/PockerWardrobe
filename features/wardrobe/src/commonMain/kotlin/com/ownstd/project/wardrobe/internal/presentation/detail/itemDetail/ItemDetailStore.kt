package com.ownstd.project.wardrobe.internal.presentation.detail.itemDetail

import com.ownstd.project.core.compose.foundation.Outcome
import com.ownstd.project.wardrobe.internal.domain.usecase.DeleteClotheUseCase
import com.ownstd.project.wardrobe.internal.domain.usecase.GetClotheByIdUseCase
import com.ownstd.project.wardrobe.internal.domain.usecase.GetClotheOutfitsUseCase
import com.ownstd.project.wardrobe.internal.presentation.detail.itemDetail.interactionModel.ItemDetailIntent
import com.ownstd.project.wardrobe.internal.presentation.detail.itemDetail.interactionModel.ItemDetailSideEffect
import com.ownstd.project.wardrobe.internal.presentation.detail.itemDetail.interactionModel.ItemDetailState
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.reduce

fun itemDetailStore(
    getClotheByIdUseCase: GetClotheByIdUseCase,
    getClotheOutfitsUseCase: GetClotheOutfitsUseCase,
    deleteClotheUseCase: DeleteClotheUseCase,
    clotheId: Int,
    onSideEffect: (ItemDetailSideEffect) -> Unit,
): Store<ItemDetailState, ItemDetailIntent, Nothing> = store(
    initial = ItemDetailState(isLoading = true)
) {
    init {
        updateState { copy(isLoading = true) }
        getClotheByIdUseCase(clotheId).collect { result ->
            when (result) {
                is Outcome.Success -> updateState { copy(clothe = result.data, isLoading = false) }
                is Outcome.Empty -> {
                    updateState { copy(isLoading = false) }
                    onSideEffect(ItemDetailSideEffect.NavigateBack)
                }
                is Outcome.Error -> {
                    updateState { copy(isLoading = false) }
                    onSideEffect(ItemDetailSideEffect.ShowError(result.message))
                }
            }
        }
        getClotheOutfitsUseCase(clotheId).collect { result ->
            if (result is Outcome.Success) {
                updateState { copy(relatedLooks = result.data) }
            }
        }
    }

    reduce { intent ->
        when (intent) {
            is ItemDetailIntent.Load -> {
                updateState { copy(isLoading = true) }
                getClotheByIdUseCase(intent.clotheId).collect { result ->
                    when (result) {
                        is Outcome.Success -> updateState { copy(clothe = result.data, isLoading = false) }
                        is Outcome.Empty -> {
                            updateState { copy(isLoading = false) }
                            onSideEffect(ItemDetailSideEffect.NavigateBack)
                        }
                        is Outcome.Error -> {
                            updateState { copy(isLoading = false) }
                            onSideEffect(ItemDetailSideEffect.ShowError(result.message))
                        }
                    }
                }
                getClotheOutfitsUseCase(intent.clotheId).collect { result ->
                    if (result is Outcome.Success) {
                        updateState { copy(relatedLooks = result.data) }
                    }
                }
            }

            is ItemDetailIntent.EditClicked -> {
                var capturedId: Int? = null
                updateState { capturedId = clothe?.id; this }
                val id = capturedId ?: return@reduce
                onSideEffect(ItemDetailSideEffect.NavigateToEdit(id))
            }

            is ItemDetailIntent.DeleteClicked ->
                updateState { copy(showDeleteConfirm = true) }

            is ItemDetailIntent.DeleteCancelled ->
                updateState { copy(showDeleteConfirm = false) }

            is ItemDetailIntent.DeleteConfirmed -> {
                var capturedId: Int? = null
                updateState { capturedId = clothe?.id; this }
                val id = capturedId ?: return@reduce
                updateState { copy(showDeleteConfirm = false) }
                val result = deleteClotheUseCase(id)
                if (result.isSuccess) {
                    onSideEffect(ItemDetailSideEffect.NavigateBack)
                } else {
                    onSideEffect(
                        ItemDetailSideEffect.ShowError(
                            result.exceptionOrNull()?.message ?: "Ошибка удаления"
                        )
                    )
                }
            }

            is ItemDetailIntent.LookClicked ->
                onSideEffect(ItemDetailSideEffect.NavigateToLook(intent.lookId))

            is ItemDetailIntent.MarketplaceLinkClicked ->
                onSideEffect(ItemDetailSideEffect.OpenUrl(intent.url))
        }
    }
}
