package com.ownstd.project.authorization.internal.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AuthorizationScreen(
    openSession: () -> Unit
) {
    val viewModel: AuthorizationViewModel = koinViewModel()
    val viewState by viewModel.viewState.collectAsState()
    val isError by viewModel.errorState.collectAsState()
    val isSessionOpen by viewModel.isSessionOpen.collectAsState()

    if (isSessionOpen) {
        openSession()
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize().padding(top = 60.dp)
    ) {
        when (viewState) {
            ViewState.LOGIN -> LoginScreen(viewModel::loginUser) {
                viewModel.viewState.value = ViewState.REGISTRATION
            }

            ViewState.REGISTRATION -> RegistrationScreen(viewModel::registerUser) {
                viewModel.viewState.value = ViewState.LOGIN
            }
        }
        if (isError) {
            Text(text = "Error", color = Color.Red, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun LoginScreen(
    login: (String, String) -> Unit,
    register: () -> Unit
) {
    var tfLoginState by remember { mutableStateOf("") }
    var tfPasswordState by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Authorization Screen")
            TextField(
                value = tfLoginState, onValueChange = { tfLoginState = it },
                label = { Text("Username") })
            TextField(
                value = tfPasswordState, onValueChange = { tfPasswordState = it },
                label = { Text("password") })
            Button(onClick = {
                login(tfLoginState, tfPasswordState)
            }) {
                Text("Войти")
            }
            Button(onClick = {
                register()
            }) {
                Text("Регистрация")
            }
        }
    }
}

@Composable
fun RegistrationScreen(
    register: (String, String, String, Gender) -> Unit,
    login: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf(Gender.OTHER) }
    var dropDownExpanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Registration Screen")
            TextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") }
            )
            TextField(
                value = password, onValueChange = { password = it },
                label = { Text("password") }
            )
            TextField(
                value = email, onValueChange = { email = it },
                label = { Text("email") })

            DropdownMenu(
                expanded = dropDownExpanded,
                onDismissRequest = {
                    dropDownExpanded = false
                },
                modifier = Modifier
            ) {
                DropdownMenuItem(onClick = { gender = Gender.MALE }) {
                    Text(text = "Male")
                }
                DropdownMenuItem(onClick = { gender = Gender.FEMALE }) {
                    Text(text = "Female")
                }
                DropdownMenuItem(onClick = { gender = Gender.OTHER }) {
                    Text(text = "Other")
                }
            }
            Button(onClick = {
                register(username, email, password, gender)
            }) {
                Text("Зарегистрироваться")
            }
            Button(onClick = {
                login()
            }) {
                Text("Войти")
            }
        }
    }
}

