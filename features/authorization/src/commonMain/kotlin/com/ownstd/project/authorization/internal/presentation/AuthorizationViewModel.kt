package com.ownstd.project.authorization.internal.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ownstd.project.authorization.internal.domain.AuthorizationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class AuthorizationViewModel(
    private val authorizationRepository: AuthorizationRepository
) : ViewModel() {
    private val _viewState = MutableStateFlow(ViewState.LOGIN)
    val viewState: StateFlow<ViewState> = _viewState.asStateFlow()

    private val _errorState = MutableStateFlow(false)
    val errorState: StateFlow<Boolean> = _errorState.asStateFlow()

    private val _isSessionOpen = MutableStateFlow(false)
    val isSessionOpen: StateFlow<Boolean> = _isSessionOpen.asStateFlow()

    fun loginUser(username: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = authorizationRepository.loginUser(username, password)
            if (result) {
                _isSessionOpen.value = true
            } else {
                _errorState.value = true
            }
        }
    }

    fun registerUser(username: String, email: String, password: String, gender: Gender) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = authorizationRepository.registerUser(username, email, password, gender)
            if (!result) {
                _errorState.value = true
            }
        }
    }

    fun switchToRegistration() {
        _viewState.value = ViewState.REGISTRATION
    }

    fun switchToLogin() {
        _viewState.value = ViewState.LOGIN
    }

}