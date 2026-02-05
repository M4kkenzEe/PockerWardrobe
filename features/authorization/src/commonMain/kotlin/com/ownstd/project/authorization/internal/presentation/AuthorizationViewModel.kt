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
    val errorState = MutableStateFlow<String?>(null)
    val isSessionOpen = MutableStateFlow(false)
    val prefillEmail = MutableStateFlow("")
    val prefillPassword = MutableStateFlow("")

    fun loginUser(username: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = authorizationRepository.loginUser(username, password)
            if (result != null) {
                errorState.value = result
            } else {
                isSessionOpen.value = true
            }
        }
    }

    fun registerUser(username: String, email: String, password: String, gender: Gender) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = authorizationRepository.registerUser(username, email, password, gender)
            if (result != null) {
                errorState.value = result
            } else {
                prefillEmail.value = email
                prefillPassword.value = password
                errorState.value = null
                viewState.value = ViewState.LOGIN
            }
        }
    }

}