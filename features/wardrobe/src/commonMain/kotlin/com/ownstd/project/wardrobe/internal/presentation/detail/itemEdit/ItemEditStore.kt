package com.ownstd.project.wardrobe.internal.presentation.detail.itemEdit

import com.ownstd.project.core.compose.foundation.Outcome
import com.ownstd.project.wardrobe.internal.domain.usecase.GetClotheByIdUseCase
import com.ownstd.project.wardrobe.internal.domain.usecase.UpdateClotheUseCase
import com.ownstd.project.wardrobe.internal.presentation.detail.itemEdit.interactionModel.ItemEditIntent
import com.ownstd.project.wardrobe.internal.presentation.detail.itemEdit.interactionModel.ItemEditSideEffect
import com.ownstd.project.wardrobe.internal.presentation.detail.itemEdit.interactionModel.ItemEditState
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.reduce

fun itemEditStore(
    getClotheByIdUseCase: GetClotheByIdUseCase,
    updateClotheUseCase: UpdateClotheUseCase,
    clotheId: Int,
    onSideEffect: (ItemEditSideEffect) -> Unit,
): Store<ItemEditState, ItemEditIntent, Nothing> = store(
    initial = ItemEditState(isLoading = true)
) {
    init {
        updateState { copy(isLoading = true) }
        getClotheByIdUseCase(clotheId).collect { result ->
            when (result) {
                is Outcome.Success -> updateState { copy(clothe = result.data, isLoading = false) }
                is Outcome.Empty -> {
                    updateState { copy(isLoading = false) }
                    onSideEffect(ItemEditSideEffect.NavigateBack)
                }
                is Outcome.Error -> {
                    updateState { copy(isLoading = false) }
                    onSideEffect(ItemEditSideEffect.ShowError(result.message))
                }
            }
        }
    }

    reduce { intent ->
        when (intent) {
            is ItemEditIntent.Load -> {
                updateState { copy(isLoading = true) }
                getClotheByIdUseCase(intent.clotheId).collect { result ->
                    when (result) {
                        is Outcome.Success -> updateState { copy(clothe = result.data, isLoading = false) }
                        is Outcome.Empty -> {
                            updateState { copy(isLoading = false) }
                            onSideEffect(ItemEditSideEffect.NavigateBack)
                        }
                        is Outcome.Error -> {
                            updateState { copy(isLoading = false) }
                            onSideEffect(ItemEditSideEffect.ShowError(result.message))
                        }
                    }
                }
            }

            is ItemEditIntent.NameChanged ->
                updateState { copy(clothe = clothe?.copy(name = intent.value)) }

            is ItemEditIntent.CategoryChanged ->
                updateState { copy(clothe = clothe?.copy(category = intent.value.ifBlank { null })) }

            is ItemEditIntent.SizeChanged ->
                updateState { copy(clothe = clothe?.copy(size = intent.value.ifBlank { null })) }

            is ItemEditIntent.MaterialChanged ->
                updateState { copy(clothe = clothe?.copy(material = intent.value.ifBlank { null })) }

            is ItemEditIntent.FitChanged ->
                updateState { copy(clothe = clothe?.copy(fit = intent.value.ifBlank { null })) }

            is ItemEditIntent.StylesChanged ->
                updateState {
                    val styles = intent.value.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    copy(clothe = clothe?.copy(styles = styles.ifEmpty { null }))
                }

            is ItemEditIntent.SeasonChanged ->
                updateState {
                    val season = intent.value.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    copy(clothe = clothe?.copy(season = season.ifEmpty { null }))
                }

            is ItemEditIntent.BrandChanged ->
                updateState { copy(clothe = clothe?.copy(brand = intent.value.ifBlank { null })) }

            is ItemEditIntent.MarketplaceLinkChanged -> updateState {
                val links = clothe?.marketplaceLinks?.toMutableList() ?: return@updateState this
                if (intent.index < links.size) links[intent.index] = intent.url
                copy(clothe = clothe.copy(marketplaceLinks = links))
            }

            is ItemEditIntent.MarketplaceLinkRemoved -> updateState {
                val links = clothe?.marketplaceLinks?.toMutableList() ?: return@updateState this
                if (intent.index < links.size) links.removeAt(intent.index)
                copy(clothe = clothe.copy(marketplaceLinks = links))
            }

            is ItemEditIntent.AddMarketplaceLink -> updateState {
                val links = (clothe?.marketplaceLinks ?: emptyList()) + ""
                copy(clothe = clothe?.copy(marketplaceLinks = links))
            }

            is ItemEditIntent.CancelClicked ->
                onSideEffect(ItemEditSideEffect.NavigateBack)

            is ItemEditIntent.SaveClicked -> {
                var capturedClothe: com.ownstd.project.wardrobe.internal.domain.model.ClotheDetail? = null
                updateState { capturedClothe = clothe; this }
                val clotheToSave = capturedClothe ?: return@reduce
                val clotheIdToSave = clotheToSave.id ?: return@reduce
                updateState { copy(isSaving = true) }
                val result = updateClotheUseCase(clotheIdToSave, clotheToSave)
                updateState { copy(isSaving = false) }
                if (result.isSuccess) {
                    onSideEffect(ItemEditSideEffect.NavigateBack)
                } else {
                    onSideEffect(
                        ItemEditSideEffect.ShowError(
                            result.exceptionOrNull()?.message ?: "Ошибка сохранения"
                        )
                    )
                }
            }
        }
    }
}
