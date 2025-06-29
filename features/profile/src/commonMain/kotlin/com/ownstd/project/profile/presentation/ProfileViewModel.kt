package com.ownstd.project.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ownstd.project.profile.domain.ProfileUseCase
import com.ownstd.project.profile.domain.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

internal class ProfileViewModel(private val profileUseCase: ProfileUseCase) : ViewModel() {

    init {
        getProfile()
    }

    val profileState = MutableStateFlow<User?>(null)

    private fun getProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            val user = profileUseCase.getProfile()
            profileState.value = user
        }
    }
}