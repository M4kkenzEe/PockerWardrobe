package com.ownstd.project.profile.internal.presentation.detail.editProfile

import com.ownstd.project.core.compose.foundation.Outcome
import com.ownstd.project.profile.internal.domain.usecase.GetProfileUseCase
import com.ownstd.project.profile.internal.domain.usecase.UpdateProfileUseCase
import com.ownstd.project.profile.internal.presentation.detail.editProfile.interactionModel.EditProfileIntent
import com.ownstd.project.profile.internal.presentation.detail.editProfile.interactionModel.EditProfileSideEffect
import com.ownstd.project.profile.internal.presentation.detail.editProfile.interactionModel.EditProfileState
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.reduce

internal fun editProfileStore(
    getProfileUseCase: GetProfileUseCase,
    updateProfileUseCase: UpdateProfileUseCase,
    onSideEffect: (EditProfileSideEffect) -> Unit,
): Store<EditProfileState, EditProfileIntent, Nothing> = store(
    initial = EditProfileState(isLoading = true)
) {
    init {
        intent(EditProfileIntent.Load)
    }

    reduce { editIntent ->
        when (editIntent) {
            is EditProfileIntent.Load -> {
                updateState { copy(isLoading = true) }
                getProfileUseCase().collect { result ->
                    when (result) {
                        is Outcome.Success -> updateState {
                            copy(
                                user = result.data,
                                name = result.data.name,
                                username = result.data.username,
                                gender = result.data.gender,
                                isLoading = false,
                            )
                        }
                        is Outcome.Empty -> updateState { copy(isLoading = false) }
                        is Outcome.Error -> {
                            updateState { copy(isLoading = false) }
                            onSideEffect(EditProfileSideEffect.ShowError(result.message))
                        }
                    }
                }
            }

            is EditProfileIntent.NameChanged ->
                updateState { copy(name = editIntent.value) }

            is EditProfileIntent.UsernameChanged ->
                updateState { copy(username = editIntent.value) }

            is EditProfileIntent.GenderChanged ->
                updateState { copy(gender = editIntent.value) }

            is EditProfileIntent.CancelClicked ->
                onSideEffect(EditProfileSideEffect.NavigateBack)

            is EditProfileIntent.SaveClicked -> {
                var savedName = ""
                var savedUsername = ""
                var savedGender: String? = null
                updateState {
                    savedName = name
                    savedUsername = username
                    savedGender = gender
                    copy(isSaving = true)
                }
                updateProfileUseCase(
                    name = savedName,
                    username = savedUsername,
                    gender = savedGender,
                ).fold(
                    onSuccess = {
                        updateState { copy(isSaving = false) }
                        onSideEffect(EditProfileSideEffect.NavigateBack)
                    },
                    onFailure = { e ->
                        updateState { copy(isSaving = false) }
                        onSideEffect(EditProfileSideEffect.ShowError(e.message ?: "Ошибка сохранения"))
                    }
                )
            }
        }
    }
}
