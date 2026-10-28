package com.ownstd.project.authorization.internal.presentation

/**
 * Checks that the string contains '@' with at least one character before and after it.
 * Mirrors the backend email validation contract.
 */
internal fun isValidEmail(email: String): Boolean {
    val atIndex = email.indexOf('@')
    return atIndex > 0 && atIndex < email.length - 1
}

/**
 * Password must be at least 8 characters, contain at least one digit and one uppercase letter.
 * Mirrors the backend password validation contract.
 */
internal fun isValidPassword(password: String): Boolean =
    password.length >= 8 &&
    password.any { it.isDigit() } &&
    password.any { it.isUpperCase() }

internal const val PASSWORD_RULE_ERROR = "Мин. 8 символов, 1 цифра и 1 заглавная буква"

internal fun emailErrorMessage(email: String, touched: Boolean): String? =
    if (touched && email.isNotEmpty() && !isValidEmail(email)) "Введите корректный email" else null

internal fun passwordErrorMessage(password: String, touched: Boolean): String? =
    if (touched && password.isNotEmpty() && !isValidPassword(password)) PASSWORD_RULE_ERROR else null

internal fun confirmPasswordErrorMessage(password: String, confirm: String, touched: Boolean): String? =
    if (touched && confirm.isNotEmpty() && password != confirm) "Пароли не совпадают" else null

internal fun canSubmitPassword(password: String, confirmation: String, isLoading: Boolean): Boolean =
    isValidPassword(password) && password == confirmation && !isLoading

internal fun isResendEnabled(countdown: Int, isLoading: Boolean): Boolean =
    countdown == 0 && !isLoading

internal fun formatResendText(countdown: Int): String =
    if (countdown > 0) "Повторно через ${countdown}с" else "Отправить повторно"
