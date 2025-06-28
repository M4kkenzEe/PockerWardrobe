package com.ownstd.project.wardrobe.api

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ownstd.project.network.AuthService
import androidx.compose.foundation.layout.Box
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import kotlinx.coroutines.launch
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import org.koin.compose.koinInject

@Composable
fun TestApiScreen() {
    val authService: AuthService = koinInject()
    val scope = rememberCoroutineScope()

    // State для полей ввода
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var loginUsername by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }

    // State для результатов
    var resultMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Заголовок
            Text(
                text = "Test API Screen",
            )

            // Секция регистрации
            Text("Register")

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            OutlinedTextField(
                value = gender,
                onValueChange = { gender = it },
                label = { Text("Gender") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    isLoading = true
                    scope.launch {
                        authService.register(username, email, password, gender)
                            .onSuccess { user ->
                                resultMessage = "Registration successful: $user"
                            }
                            .onFailure { exception ->
                                resultMessage = "Registration failed: ${exception.message}"
                            }
                        isLoading = false
                    }
                },
                enabled = !isLoading && username.isNotBlank() && email.isNotBlank() &&
                        password.isNotBlank() && gender.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Register")
            }

            // Секция логина
            Text("Login", style = MaterialTheme.typography.subtitle1)

            OutlinedTextField(
                value = loginUsername,
                onValueChange = { loginUsername = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = loginPassword,
                onValueChange = { loginPassword = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Button(
                onClick = {
                    isLoading = true
                    scope.launch {
                        authService.login(loginUsername, loginPassword)
                            .onSuccess { token ->
                                resultMessage = "Login successful, token: $token"
                            }
                            .onFailure { exception ->
                                resultMessage = "Login failed: ${exception.message}"
                            }
                        isLoading = false
                    }
                },
                enabled = !isLoading && loginUsername.isNotBlank() && loginPassword.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login")
            }

            // Индикатор загрузки и результат
            if (isLoading) {
                CircularProgressIndicator()
            }

            Text(
                text = resultMessage,
                style = MaterialTheme.typography.body2,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}