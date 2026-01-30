package com.ownstd.project.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ownstd.project.authorization.internal.domain.LogoutUseCase
import com.ownstd.project.profile.domain.ProfileUseCase
import com.ownstd.project.profile.domain.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class ProfileViewModel(
    private val profileUseCase: ProfileUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    init {
        getProfile()
    }

    private val _profileState = MutableStateFlow<User?>(null)
    val profileState: StateFlow<User?> = _profileState.asStateFlow()

    private fun getProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            val user = profileUseCase.getProfile()
            _profileState.value = user
        }
    }

    fun logout() {
        logoutUseCase.execute()
    }
}