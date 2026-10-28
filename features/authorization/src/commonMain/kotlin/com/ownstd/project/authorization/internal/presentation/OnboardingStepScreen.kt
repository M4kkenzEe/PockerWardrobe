package com.ownstd.project.authorization.internal.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ownstd.project.designsystem.components.ClothisGhostButton
import com.ownstd.project.designsystem.components.ClothisOtpInputField
import com.ownstd.project.designsystem.components.ClothisPrimaryButton
import com.ownstd.project.designsystem.theme.ClothisTheme

private fun stepIndex(state: ViewState): Int = when (state) {
    ViewState.ONBOARDING_NAME -> 0
    ViewState.ONBOARDING_EMAIL -> 1
    ViewState.ONBOARDING_OTP -> 2
    ViewState.ONBOARDING_PASSWORD -> 3
    else -> -1
}

@Composable
internal fun OnboardingFlow(
    viewState: ViewState,
    errorMessage: String?,
    otpError: String?,
    initialName: String,
    initialEmail: String,
    registrationResendCountdown: Int,
    isLoading: Boolean,
    isEmailVerified: Boolean,
    onNameChange: (String) -> Unit,
    onNameNext: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onEmailNext: (String) -> Unit,
    onPasswordSubmit: (String) -> Unit,
    onOtpComplete: (String) -> Unit,
    onOtpChange: () -> Unit,
    onResendOtp: () -> Unit,
    onContinueVerified: () -> Unit,
    onBack: () -> Unit,
) {
    AnimatedContent(
        targetState = viewState,
        transitionSpec = {
            val targetIndex = stepIndex(targetState)
            val initialIndex = stepIndex(initialState)
            val direction = if (targetIndex >= initialIndex) 1 else -1
            slideInHorizontally { fullWidth -> fullWidth * direction } togetherWith
                slideOutHorizontally { fullWidth -> -fullWidth * direction }
        },
        label = "OnboardingSlide",
    ) { state ->
        OnboardingStepLayout(
            currentStep = stepIndex(state) + 1,
            totalSteps = 4,
            onBack = onBack,
        ) {
            when (state) {
                ViewState.ONBOARDING_NAME -> NameStep(
                    initialName = initialName,
                    onNameChange = onNameChange,
                    errorMessage = errorMessage,
                    onNext = onNameNext,
                )
                ViewState.ONBOARDING_EMAIL -> EmailStep(
                    initialEmail = initialEmail,
                    onEmailChange = onEmailChange,
                    errorMessage = errorMessage,
                    isLoading = isLoading,
                    onNext = onEmailNext,
                )
                ViewState.ONBOARDING_PASSWORD -> PasswordStep(
                    errorMessage = errorMessage,
                    isLoading = isLoading,
                    onSubmit = onPasswordSubmit,
                )
                ViewState.ONBOARDING_OTP -> OtpStep(
                    errorMessage = errorMessage,
                    otpError = otpError,
                    resendCountdown = registrationResendCountdown,
                    isLoading = isLoading,
                    isEmailVerified = isEmailVerified,
                    onOtpComplete = onOtpComplete,
                    onOtpChange = onOtpChange,
                    onResend = onResendOtp,
                    onContinue = onContinueVerified,
                )
                else -> Unit
            }
        }
    }
}

@Composable
private fun OnboardingStepLayout(
    currentStep: Int,
    totalSteps: Int,
    onBack: () -> Unit,
    content: @Composable () -> Unit,
) {
    val colors = ClothisTheme.colors
    val dimens = ClothisTheme.dimens

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.spaceLg, vertical = dimens.spaceMd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(dimens.touchTargetMin)) {
                Text(
                    text = "←",
                    modifier = Modifier.semantics { contentDescription = "Назад" },
                    color = colors.onCanvas,
                    fontSize = 22.sp,
                )
            }
            Spacer(modifier = Modifier.width(dimens.spaceSm))
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(dimens.spaceXs),
            ) {
                repeat(totalSteps) { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .background(
                                color = if (index < currentStep) colors.clay else colors.surfaceElevated,
                                shape = RoundedCornerShape(dimens.radiusFull),
                            ),
                    )
                }
            }
            Spacer(modifier = Modifier.width(dimens.touchTargetMin))
        }

        content()
    }
}

@Composable
private fun NameStep(
    initialName: String,
    onNameChange: (String) -> Unit,
    errorMessage: String?,
    onNext: (String) -> Unit,
) {
    val colors = ClothisTheme.colors
    val dimens = ClothisTheme.dimens
    var name by remember { mutableStateOf(initialName) }
    var touched by remember { mutableStateOf(initialName.isNotEmpty()) }
    val isValid = name.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = dimens.screenHorizontalPadding)
            .padding(top = dimens.space3xl),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(text = "Как тебя зовут?", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = colors.onCanvas)
        Spacer(modifier = Modifier.height(dimens.spaceXs))
        Text(text = "Это имя будет отображаться в профиле", fontSize = 14.sp, color = colors.onCanvasMuted)
        Spacer(modifier = Modifier.height(dimens.space3xl))

        OnboardingTextField(
            value = name,
            onValueChange = { name = it; touched = true; onNameChange(it) },
            placeholder = "Имя",
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next, keyboardType = KeyboardType.Text),
            keyboardActions = KeyboardActions(onNext = { if (isValid) onNext(name.trim()) }),
            errorMessage = if (touched && name.isBlank()) "Введи своё имя" else null,
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(dimens.spaceMd))
            Text(text = errorMessage, color = colors.error, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.weight(1f))
        ClothisPrimaryButton(
            text = "Далее",
            onClick = { onNext(name.trim()) },
            enabled = isValid,
            modifier = Modifier.fillMaxWidth().padding(bottom = dimens.space3xl),
        )
    }
}

@Composable
private fun EmailStep(
    initialEmail: String,
    onEmailChange: (String) -> Unit,
    errorMessage: String?,
    isLoading: Boolean,
    onNext: (String) -> Unit,
) {
    val colors = ClothisTheme.colors
    val dimens = ClothisTheme.dimens
    var email by remember { mutableStateOf(initialEmail) }
    var touched by remember { mutableStateOf(initialEmail.isNotEmpty()) }
    val emailErr = emailErrorMessage(email, touched)
    val isValid = isValidEmail(email)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = dimens.screenHorizontalPadding)
            .padding(top = dimens.space3xl),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(text = "Твой email", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = colors.onCanvas)
        Spacer(modifier = Modifier.height(dimens.spaceXs))
        Text(text = "Мы отправим код подтверждения на эту почту", fontSize = 14.sp, color = colors.onCanvasMuted)
        Spacer(modifier = Modifier.height(dimens.space3xl))

        OnboardingTextField(
            value = email,
            onValueChange = { email = it; touched = true; onEmailChange(it) },
            placeholder = "example@mail.com",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { if (isValid) onNext(email.trim()) }),
            errorMessage = emailErr,
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(dimens.spaceMd))
            Text(text = errorMessage, color = colors.error, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.weight(1f))
        ClothisPrimaryButton(
            text = "Далее",
            onClick = { onNext(email.trim()) },
            enabled = isValid && !isLoading,
            isLoading = isLoading,
            modifier = Modifier.fillMaxWidth().padding(bottom = dimens.space3xl),
        )
    }
}

@Composable
private fun PasswordStep(
    errorMessage: String?,
    isLoading: Boolean,
    onSubmit: (String) -> Unit,
) {
    val colors = ClothisTheme.colors
    val dimens = ClothisTheme.dimens
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var passwordTouched by remember { mutableStateOf(false) }
    var confirmTouched by remember { mutableStateOf(false) }

    val passwordErr = passwordErrorMessage(password, passwordTouched)
    val confirmErr = confirmPasswordErrorMessage(password, confirm, confirmTouched)
    val canSubmit = canSubmitPassword(password, confirm, isLoading)
    val visualTransformation = PasswordVisualTransformation()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = dimens.screenHorizontalPadding)
            .padding(top = dimens.space3xl),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(text = "Придумай пароль", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = colors.onCanvas)
        Spacer(modifier = Modifier.height(dimens.spaceXs))
        Text(text = "Мин. 8 символов, 1 цифра и 1 заглавная буква", fontSize = 14.sp, color = colors.onCanvasMuted)
        Spacer(modifier = Modifier.height(dimens.space3xl))

        OnboardingTextField(
            value = password,
            onValueChange = { password = it; passwordTouched = true },
            placeholder = "Пароль",
            visualTransformation = visualTransformation,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
            errorMessage = passwordErr,
        )
        Spacer(modifier = Modifier.height(dimens.spaceLg))

        OnboardingTextField(
            value = confirm,
            onValueChange = { confirm = it; confirmTouched = true },
            placeholder = "Повтори пароль",
            visualTransformation = visualTransformation,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { if (canSubmit) onSubmit(password) }),
            errorMessage = confirmErr,
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(dimens.spaceMd))
            Text(text = errorMessage, color = colors.error, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.weight(1f))
        ClothisPrimaryButton(
            text = "Создать аккаунт",
            onClick = { onSubmit(password) },
            enabled = canSubmit,
            isLoading = isLoading,
            modifier = Modifier.fillMaxWidth().padding(bottom = dimens.space3xl),
        )
    }
}

@Composable
private fun OtpStep(
    errorMessage: String?,
    otpError: String?,
    resendCountdown: Int,
    isLoading: Boolean,
    isEmailVerified: Boolean,
    onOtpComplete: (String) -> Unit,
    onOtpChange: () -> Unit,
    onResend: () -> Unit,
    onContinue: () -> Unit,
) {
    val colors = ClothisTheme.colors
    val dimens = ClothisTheme.dimens
    var code by remember { mutableStateOf("") }
    val resendActive = isResendEnabled(resendCountdown, isLoading)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = dimens.screenHorizontalPadding)
            .padding(top = dimens.space3xl),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(text = "Код из письма", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = colors.onCanvas)
        Spacer(modifier = Modifier.height(dimens.spaceXs))
        Text(
            text = if (isEmailVerified) "Email уже подтверждён, можно продолжить" else "Введи 6-значный код из письма",
            fontSize = 14.sp,
            color = colors.onCanvasMuted,
        )
        Spacer(modifier = Modifier.height(dimens.space3xl))

        ClothisOtpInputField(
            code = code,
            onCodeChange = { newCode ->
                code = newCode
                onOtpChange()
                if (newCode.length == 6) onOtpComplete(newCode)
            },
            error = otpError,
        )

        Spacer(modifier = Modifier.height(dimens.spaceLg))

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(dimens.spaceMd))
            Text(text = errorMessage, color = colors.error, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.weight(1f))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(bottom = dimens.space3xl),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp), color = colors.clay, strokeWidth = 2.dp)
            }
        } else {
            if (isEmailVerified) {
                ClothisPrimaryButton(
                    text = "Продолжить",
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth().padding(bottom = dimens.spaceMd),
                )
            }
            if (code.length == 6) {
                ClothisPrimaryButton(
                    text = "Подтвердить",
                    onClick = { onOtpComplete(code) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = dimens.spaceMd),
                )
            }
            ClothisGhostButton(
                text = formatResendText(resendCountdown),
                onClick = onResend,
                enabled = resendActive,
                modifier = Modifier.fillMaxWidth().padding(bottom = dimens.space3xl),
            )
        }
    }
}


@Composable
private fun OnboardingTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val colors = ClothisTheme.colors
    val dimens = ClothisTheme.dimens

    Column(modifier = modifier) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surfaceElevated, RoundedCornerShape(dimens.radiusButton))
                .border(
                    width = dimens.borderWidth,
                    color = if (errorMessage != null) colors.error else colors.divider,
                    shape = RoundedCornerShape(dimens.radiusButton),
                )
                .padding(horizontal = dimens.spaceLg, vertical = dimens.spaceXl),
            singleLine = true,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            cursorBrush = SolidColor(colors.clay),
            textStyle = TextStyle(color = colors.onCanvas, fontSize = 16.sp),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(text = placeholder, color = colors.onCanvasMuted, fontSize = 16.sp)
                }
                innerTextField()
            },
        )
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(dimens.spaceXs))
            Text(
                text = errorMessage,
                color = colors.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = dimens.spaceXs),
            )
        }
    }
}
