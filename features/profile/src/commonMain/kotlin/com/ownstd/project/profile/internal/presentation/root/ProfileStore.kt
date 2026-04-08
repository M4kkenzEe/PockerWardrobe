package com.ownstd.project.profile.internal.presentation.root

import com.ownstd.project.core.compose.foundation.Outcome
import com.ownstd.project.profile.internal.domain.usecase.GetProfileUseCase
import com.ownstd.project.profile.internal.domain.usecase.ProfileLogoutUseCase
import com.ownstd.project.profile.internal.presentation.root.interactionModel.ProfileIntent
import com.ownstd.project.profile.internal.presentation.root.interactionModel.ProfileSideEffect
import com.ownstd.project.profile.internal.presentation.root.interactionModel.ProfileState
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.reduce

internal fun profileStore(
    getProfileUseCase: GetProfileUseCase,
    logoutUseCase: ProfileLogoutUseCase,
    onSideEffect: (ProfileSideEffect) -> Unit,
): Store<ProfileState, ProfileIntent, Nothing> = store(
    initial = ProfileState(isLoading = true)
) {
    init {
        intent(ProfileIntent.Load)
    }

    reduce { profileIntent ->
        when (profileIntent) {
            is ProfileIntent.Load -> {
                updateState { copy(isLoading = true) }
                getProfileUseCase().collect { result ->
                    when (result) {
                        is Outcome.Success -> updateState {
                            copy(user = result.data, isLoading = false)
                        }
                        is Outcome.Empty -> updateState { copy(isLoading = false) }
                        is Outcome.Error -> {
                            updateState { copy(isLoading = false) }
                            onSideEffect(ProfileSideEffect.ShowError(result.message))
                        }
                    }
                }
            }

            is ProfileIntent.EditProfileClicked ->
                onSideEffect(ProfileSideEffect.NavigateToEdit)

            is ProfileIntent.SizesClicked ->
                onSideEffect(ProfileSideEffect.NavigateToSizes)

            is ProfileIntent.ToggleTheme ->
                updateState { copy(isDarkTheme = !isDarkTheme) }

            is ProfileIntent.LogoutClicked ->
                updateState { copy(showLogoutDialog = true) }

            is ProfileIntent.LogoutDismissed ->
                updateState { copy(showLogoutDialog = false) }

            is ProfileIntent.LogoutConfirmed -> {
                updateState { copy(showLogoutDialog = false) }
                logoutUseCase().fold(
                    onSuccess = { onSideEffect(ProfileSideEffect.NavigateToAuth) },
                    onFailure = { e ->
                        onSideEffect(ProfileSideEffect.ShowError(e.message ?: "Ошибка выхода"))
                    }
                )
            }
        }
    }
}
