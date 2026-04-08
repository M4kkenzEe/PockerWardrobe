package com.ownstd.project.outfit.internal.presentation.list

import com.ownstd.project.core.compose.foundation.Outcome
import com.ownstd.project.outfit.internal.domain.usecase.DeleteLookUseCase
import com.ownstd.project.outfit.internal.domain.usecase.GetLooksUseCase
import com.ownstd.project.outfit.internal.domain.usecase.ShareLookUseCase
import com.ownstd.project.outfit.internal.presentation.list.interactionModel.OutfitIntent
import com.ownstd.project.outfit.internal.presentation.list.interactionModel.OutfitSideEffect
import com.ownstd.project.outfit.internal.presentation.list.interactionModel.OutfitState
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.reduce

fun outfitStore(
    getLooksUseCase: GetLooksUseCase,
    deleteLookUseCase: DeleteLookUseCase,
    shareLookUseCase: ShareLookUseCase,
    onSideEffect: (OutfitSideEffect) -> Unit,
): Store<OutfitState, OutfitIntent, Nothing> = store(
    initial = OutfitState(isLoading = true)
) {
    init {
        getLooksUseCase().collect { result ->
            when (result) {
                is Outcome.Success -> updateState {
                    copy(looks = result.data, isLoading = false, isEmpty = false)
                }
                is Outcome.Empty -> updateState {
                    copy(isLoading = false, isEmpty = true)
                }
                is Outcome.Error -> {
                    updateState { copy(isLoading = false) }
                    onSideEffect(OutfitSideEffect.ShowError(result.message))
                }
            }
        }
    }

    reduce { intent ->
        when (intent) {
            is OutfitIntent.LoadLooks -> {
                updateState { copy(isLoading = true) }
                getLooksUseCase().collect { result ->
                    when (result) {
                        is Outcome.Success -> updateState {
                            val filtered = if (activeStyle != null) {
                                result.data.filter { it.style == activeStyle }
                            } else {
                                result.data
                            }
                            copy(
                                looks = result.data,
                                isLoading = false,
                                isEmpty = false,
                            ).let { s -> s.copy(looks = filtered) }
                        }
                        is Outcome.Empty -> updateState {
                            copy(isLoading = false, isEmpty = true)
                        }
                        is Outcome.Error -> {
                            updateState { copy(isLoading = false) }
                            onSideEffect(OutfitSideEffect.ShowError(result.message))
                        }
                    }
                }
            }

            is OutfitIntent.SelectStyle -> updateState {
                val filtered = if (intent.style != null) {
                    looks.filter { it.style == intent.style }
                } else {
                    looks
                }
                copy(activeStyle = intent.style, looks = filtered)
            }

            is OutfitIntent.LookClicked -> {
                val id = intent.lookId
                onSideEffect(OutfitSideEffect.NavigateToDetail(id))
            }

            is OutfitIntent.DeleteLook -> {
                val result = deleteLookUseCase(intent.id)
                if (result.isSuccess) {
                    intent(OutfitIntent.LoadLooks)
                } else {
                    onSideEffect(
                        OutfitSideEffect.ShowError(
                            result.exceptionOrNull()?.message ?: "Ошибка удаления"
                        )
                    )
                }
            }

            is OutfitIntent.ShareLook -> {
                val result = shareLookUseCase(intent.id)
                if (result.isFailure) {
                    onSideEffect(
                        OutfitSideEffect.ShowError(
                            result.exceptionOrNull()?.message ?: "Ошибка при получении ссылки"
                        )
                    )
                }
            }

            is OutfitIntent.FavoriteLook -> {
                // TODO: реализуется позже
            }

            is OutfitIntent.FabClicked ->
                onSideEffect(OutfitSideEffect.NavigateToConstructor)
        }
    }
}
