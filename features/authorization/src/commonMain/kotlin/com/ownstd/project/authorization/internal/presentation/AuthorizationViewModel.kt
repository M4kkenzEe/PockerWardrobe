package com.ownstd.project.authorization.internal.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ownstd.project.authorization.internal.domain.AuthorizationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

internal class AuthorizationViewModel(
    private val authorizationRepository: AuthorizationRepository
) : ViewModel() {
    val viewState = MutableStateFlow(ViewState.LOGIN)
    val errorState = MutableStateFlow(false)
    val isSessionOpen = MutableStateFlow(false)

    fun loginUser(username: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = authorizationRepository.loginUser(username, password)
            if (result) {
                isSessionOpen.value = true
            } else {
                errorState.value = true
            }
        }
    }

    fun registerUser(username: String, email: String, password: String, gender: Gender) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = authorizationRepository.registerUser(username, email, password, gender)
            if (!result) {
                errorState.value = true
            }
        }
    }

}