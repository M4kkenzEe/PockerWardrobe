package com.ownstd.project.wardrobe.internal.presentation.list

import com.ownstd.project.core.compose.foundation.Outcome
import com.ownstd.project.wardrobe.internal.domain.model.Clothe
import com.ownstd.project.wardrobe.internal.domain.model.FilterOptions
import com.ownstd.project.wardrobe.internal.domain.model.SortOrder
import com.ownstd.project.wardrobe.internal.domain.usecase.DeleteClotheUseCase
import com.ownstd.project.wardrobe.internal.domain.usecase.GetClothesUseCase
import com.ownstd.project.wardrobe.internal.presentation.list.interactionModel.WardrobeIntent
import com.ownstd.project.wardrobe.internal.presentation.list.interactionModel.WardrobeSideEffect
import com.ownstd.project.wardrobe.internal.presentation.list.interactionModel.WardrobeState
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.reduce

fun wardrobeStore(
    getClothesUseCase: GetClothesUseCase,
    deleteClotheUseCase: DeleteClotheUseCase,
    onSideEffect: (WardrobeSideEffect) -> Unit,
): Store<WardrobeState, WardrobeIntent, Nothing> = store(
    initial = WardrobeState(isLoading = true)
) {
    init {
        getClothesUseCase().collect { result ->
            when (result) {
                is Outcome.Success -> updateState {
                    copy(
                        clothes = result.data,
                        filteredClothes = result.data.applyFilter(activeCategory, filterOptions),
                        isLoading = false,
                        isEmpty = false,
                    )
                }
                is Outcome.Empty -> updateState { copy(isLoading = false, isEmpty = true) }
                is Outcome.Error -> {
                    updateState { copy(isLoading = false) }
                    onSideEffect(WardrobeSideEffect.ShowError(result.message))
                }
            }
        }
    }
    reduce { wardrobeIntent ->
        when (wardrobeIntent) {
            is WardrobeIntent.LoadClothes -> {
                updateState { copy(isLoading = true) }
                getClothesUseCase().collect { result ->
                    when (result) {
                        is Outcome.Success -> updateState {
                            copy(
                                clothes = result.data,
                                filteredClothes = result.data.applyFilter(activeCategory, filterOptions),
                                isLoading = false,
                                isEmpty = false,
                            )
                        }
                        is Outcome.Empty -> updateState { copy(isLoading = false, isEmpty = true) }
                        is Outcome.Error -> {
                            updateState { copy(isLoading = false) }
                            onSideEffect(WardrobeSideEffect.ShowError(result.message))
                        }
                    }
                }
            }

            is WardrobeIntent.SelectCategory -> updateState {
                val filtered = clothes.applyFilter(wardrobeIntent.category, filterOptions)
                copy(activeCategory = wardrobeIntent.category, filteredClothes = filtered)
            }

            is WardrobeIntent.ApplyFilter -> updateState {
                val filtered = clothes.applyFilter(activeCategory, wardrobeIntent.options)
                copy(
                    filterOptions = wardrobeIntent.options,
                    filteredClothes = filtered,
                    isFilterActive = wardrobeIntent.options.isActive,
                )
            }

            is WardrobeIntent.DeleteClothe -> {
                val result = deleteClotheUseCase(wardrobeIntent.id)
                if (result.isSuccess) {
                    intent(WardrobeIntent.LoadClothes)
                } else {
                    onSideEffect(
                        WardrobeSideEffect.ShowError(
                            result.exceptionOrNull()?.message ?: "Ошибка удаления"
                        )
                    )
                }
            }

            is WardrobeIntent.ItemClicked ->
                onSideEffect(WardrobeSideEffect.NavigateToDetail(wardrobeIntent.clotheId))

            is WardrobeIntent.ItemLongClicked ->
                onSideEffect(WardrobeSideEffect.ShowItemMenu(wardrobeIntent.clotheId))

            is WardrobeIntent.FabClicked ->
                onSideEffect(WardrobeSideEffect.ShowAddBottomSheet)

            is WardrobeIntent.FilterClicked ->
                onSideEffect(WardrobeSideEffect.ShowFilterBottomSheet)
        }
    }
}

private fun List<Clothe>.applyFilter(
    category: String?,
    options: FilterOptions,
): List<Clothe> {
    var result = this
    if (category != null) result = result.filter { it.category == category }
    if (options.seasons.isNotEmpty()) result = result.filter { c ->
        c.season?.any { it in options.seasons } == true
    }
    if (options.colors.isNotEmpty()) result = result.filter { c ->
        c.color != null && c.color in options.colors
    }
    result = when (options.sortOrder) {
        SortOrder.BY_NAME -> result.sortedBy { it.name }
        SortOrder.BY_CATEGORY -> result.sortedBy { it.category }
        SortOrder.BY_DATE -> result
    }
    return result
}
