package com.ownstd.project.authorization.internal.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ownstd.project.authorization.internal.presentation.design_system.BG_GREY_COLOR
import com.ownstd.project.authorization.internal.presentation.design_system.BLUE_COLOR
import com.ownstd.project.authorization.internal.presentation.design_system.GREY_COLOR
import org.koin.compose.viewmodel.koinViewModel

private val TELEGRAM_BLUE = Color(0xFF229ED9)

@Composable
fun AuthorizationScreen(
    openSession: () -> Unit
) {
    val viewModel: AuthorizationViewModel = koinViewModel()
    val viewState by viewModel.viewState.collectAsState()
    val isError by viewModel.errorState.collectAsState()
    val isSessionOpen by viewModel.isSessionOpen.collectAsState()
    val prefillEmail by viewModel.prefillEmail.collectAsState()
    val prefillPassword by viewModel.prefillPassword.collectAsState()
    val isTelegramLoading by viewModel.isTelegramLoading.collectAsState()

    val uriHandler = LocalUriHandler.current
    LaunchedEffect(Unit) {
        viewModel.openUrlEvent.collect { url ->
            uriHandler.openUri(url)
        }
    }

    if (isSessionOpen) {
        openSession()
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(BG_GREY_COLOR)
            .statusBarsPadding()
    ) {
        when (viewState) {
            ViewState.LOGIN -> LoginScreen(
                errorMessage = isError,
                initialEmail = prefillEmail,
                initialPassword = prefillPassword,
                isTelegramLoading = isTelegramLoading,
                onLogin = viewModel::loginUser,
                onTelegramAuth = viewModel::startTelegramAuth,
                onSwitchToRegister = {
                    viewModel.viewState.value = ViewState.REGISTRATION
                }
            )

            ViewState.REGISTRATION -> RegistrationScreen(
                errorMessage = isError,
                isTelegramLoading = isTelegramLoading,
                onRegister = viewModel::registerUser,
                onTelegramAuth = viewModel::startTelegramAuth,
                onSwitchToLogin = {
                    viewModel.viewState.value = ViewState.LOGIN
                }
            )
        }
    }
}

@Composable
fun LoginScreen(
    errorMessage: String?,
    initialEmail: String = "",
    initialPassword: String = "",
    isTelegramLoading: Boolean = false,
    onLogin: (String, String) -> Unit,
    onTelegramAuth: () -> Unit,
    onSwitchToRegister: () -> Unit
) {
    var email by remember(initialEmail) { mutableStateOf(initialEmail) }
    var password by remember(initialPassword) { mutableStateOf(initialPassword) }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(bottom = 44.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Добро пожаловать",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Вход в аккаунт",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = BLUE_COLOR,
                    unfocusedBorderColor = GREY_COLOR,
                    cursorColor = BLUE_COLOR
                ),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Пароль") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = BLUE_COLOR,
                    unfocusedBorderColor = GREY_COLOR,
                    cursorColor = BLUE_COLOR
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { passwordVisible = !passwordVisible }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = passwordVisible,
                    onCheckedChange = { passwordVisible = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = BLUE_COLOR,
                        uncheckedColor = GREY_COLOR
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Показать пароль",
                    fontSize = 14.sp,
                    color = GREY_COLOR
                )
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { onLogin(email, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = BLUE_COLOR,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Войти",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            OrDivider()

            TelegramButton(
                isLoading = isTelegramLoading,
                onClick = onTelegramAuth
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Нет аккаунта? Зарегистрироваться",
                fontSize = 14.sp,
                color = GREY_COLOR,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSwitchToRegister() }
                    .padding(vertical = 8.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun RegistrationScreen(
    errorMessage: String?,
    isTelegramLoading: Boolean = false,
    onRegister: (String, String, String, Gender) -> Unit,
    onTelegramAuth: () -> Unit,
    onSwitchToLogin: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var gender by remember { mutableStateOf(Gender.OTHER) }
    var dropDownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(bottom = 44.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Создать аккаунт",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Регистрация",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Имя пользователя") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = BLUE_COLOR,
                    unfocusedBorderColor = GREY_COLOR,
                    cursorColor = BLUE_COLOR
                ),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = BLUE_COLOR,
                    unfocusedBorderColor = GREY_COLOR,
                    cursorColor = BLUE_COLOR
                ),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Пароль") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = BLUE_COLOR,
                    unfocusedBorderColor = GREY_COLOR,
                    cursorColor = BLUE_COLOR
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { passwordVisible = !passwordVisible }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = passwordVisible,
                    onCheckedChange = { passwordVisible = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = BLUE_COLOR,
                        uncheckedColor = GREY_COLOR
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Показать пароль",
                    fontSize = 14.sp,
                    color = GREY_COLOR
                )
            }

            Box {
                OutlinedTextField(
                    value = when (gender) {
                        Gender.MALE -> "Мужской"
                        Gender.FEMALE -> "Женский"
                        Gender.OTHER -> "Другое"
                    },
                    onValueChange = {},
                    label = { Text("Пол") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { dropDownExpanded = true },
                    enabled = false,
                    readOnly = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        disabledBorderColor = GREY_COLOR,
                        disabledTextColor = Color.Black,
                        disabledLabelColor = GREY_COLOR
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                DropdownMenu(
                    expanded = dropDownExpanded,
                    onDismissRequest = { dropDownExpanded = false }
                ) {
                    DropdownMenuItem(onClick = {
                        gender = Gender.MALE
                        dropDownExpanded = false
                    }) {
                        Text(text = "Мужской")
                    }
                    DropdownMenuItem(onClick = {
                        gender = Gender.FEMALE
                        dropDownExpanded = false
                    }) {
                        Text(text = "Женский")
                    }
                    DropdownMenuItem(onClick = {
                        gender = Gender.OTHER
                        dropDownExpanded = false
                    }) {
                        Text(text = "Другое")
                    }
                }
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { onRegister(username, email, password, gender) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = BLUE_COLOR,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Зарегистрироваться",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            OrDivider()

            TelegramButton(
                isLoading = isTelegramLoading,
                onClick = onTelegramAuth
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Уже есть аккаунт? Войти",
                fontSize = 14.sp,
                color = GREY_COLOR,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSwitchToLogin() }
                    .padding(vertical = 8.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun OrDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Divider(modifier = Modifier.weight(1f), color = GREY_COLOR.copy(alpha = 0.4f))
        Text(
            text = "или",
            fontSize = 13.sp,
            color = GREY_COLOR,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        Divider(modifier = Modifier.weight(1f), color = GREY_COLOR.copy(alpha = 0.4f))
    }
}

@Composable
private fun TelegramButton(
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = { if (!isLoading) onClick() },
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = TELEGRAM_BLUE,
            contentColor = Color.White,
            disabledBackgroundColor = TELEGRAM_BLUE.copy(alpha = 0.6f),
            disabledContentColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = "✈ Войти через Telegram",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
