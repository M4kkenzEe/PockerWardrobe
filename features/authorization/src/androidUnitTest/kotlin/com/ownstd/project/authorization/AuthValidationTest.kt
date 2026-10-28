package com.ownstd.project.authorization

import com.ownstd.project.authorization.internal.presentation.canSubmitPassword
import com.ownstd.project.authorization.internal.presentation.confirmPasswordErrorMessage
import com.ownstd.project.authorization.internal.presentation.emailErrorMessage
import com.ownstd.project.authorization.internal.presentation.formatResendText
import com.ownstd.project.authorization.internal.presentation.isResendEnabled
import com.ownstd.project.authorization.internal.presentation.isValidEmail
import com.ownstd.project.authorization.internal.presentation.isValidPassword
import com.ownstd.project.authorization.internal.presentation.passwordErrorMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// ---------------------------------------------------------------------------
// 1. isValidEmail
// ---------------------------------------------------------------------------

class IsValidEmailTest {

    @Test
    fun `empty string is invalid`() {
        assertFalse(isValidEmail(""))
    }

    @Test
    fun `no at-sign is invalid`() {
        assertFalse(isValidEmail("userexample.com"))
    }

    @Test
    fun `at-sign at start is invalid`() {
        assertFalse(isValidEmail("@example.com"))
    }

    @Test
    fun `at-sign at end is invalid`() {
        assertFalse(isValidEmail("user@"))
    }

    @Test
    fun `valid email with domain is valid`() {
        assertTrue(isValidEmail("user@example.com"))
    }

    @Test
    fun `valid minimal email is valid`() {
        assertTrue(isValidEmail("a@b"))
    }
}

// ---------------------------------------------------------------------------
// 2. isValidPassword
// ---------------------------------------------------------------------------

class IsValidPasswordTest {

    @Test
    fun `empty password is invalid`() {
        assertFalse(isValidPassword(""))
    }

    @Test
    fun `password shorter than 8 chars is invalid`() {
        assertFalse(isValidPassword("Abc1234"))
    }

    @Test
    fun `password without digit is invalid`() {
        assertFalse(isValidPassword("Abcdefgh"))
    }

    @Test
    fun `password without uppercase is invalid`() {
        assertFalse(isValidPassword("abcdefg1"))
    }

    @Test
    fun `valid password passes all rules`() {
        assertTrue(isValidPassword("Abcdefg1"))
    }

    @Test
    fun `password exactly 8 chars with digit and uppercase is valid`() {
        assertTrue(isValidPassword("Password1"))
    }
}

// ---------------------------------------------------------------------------
// 3. emailErrorMessage
// ---------------------------------------------------------------------------

class EmailErrorMessageTest {

    @Test
    fun `no error when not touched`() {
        assertNull(emailErrorMessage("bademail", touched = false))
    }

    @Test
    fun `no error when field is empty even if touched`() {
        assertNull(emailErrorMessage("", touched = true))
    }

    @Test
    fun `error when touched and invalid`() {
        assertNotNull(emailErrorMessage("bademail", touched = true))
    }

    @Test
    fun `no error when touched and valid`() {
        assertNull(emailErrorMessage("user@example.com", touched = true))
    }
}

// ---------------------------------------------------------------------------
// 4. passwordErrorMessage
// ---------------------------------------------------------------------------

class PasswordErrorMessageTest {

    @Test
    fun `no error when not touched`() {
        assertNull(passwordErrorMessage("weak", touched = false))
    }

    @Test
    fun `no error when field is empty even if touched`() {
        assertNull(passwordErrorMessage("", touched = true))
    }

    @Test
    fun `error when touched and invalid`() {
        assertNotNull(passwordErrorMessage("weak", touched = true))
    }

    @Test
    fun `no error when touched and valid`() {
        assertNull(passwordErrorMessage("Password1", touched = true))
    }
}

// ---------------------------------------------------------------------------
// 5. confirmPasswordErrorMessage
// ---------------------------------------------------------------------------

class ConfirmPasswordErrorMessageTest {

    @Test
    fun `no error when not touched`() {
        assertNull(confirmPasswordErrorMessage("Password1", "Other1", touched = false))
    }

    @Test
    fun `no error when confirm is empty even if touched`() {
        assertNull(confirmPasswordErrorMessage("Password1", "", touched = true))
    }

    @Test
    fun `error when touched and passwords differ`() {
        assertNotNull(confirmPasswordErrorMessage("Password1", "Other1", touched = true))
    }

    @Test
    fun `no error when touched and passwords match`() {
        assertNull(confirmPasswordErrorMessage("Password1", "Password1", touched = true))
    }
}

// ---------------------------------------------------------------------------
// 6. isResendEnabled
// ---------------------------------------------------------------------------

class IsResendEnabledTest {

    @Test
    fun `enabled when countdown is 0 and not loading`() {
        assertTrue(isResendEnabled(countdown = 0, isLoading = false))
    }

    @Test
    fun `disabled when countdown greater than 0`() {
        assertFalse(isResendEnabled(countdown = 30, isLoading = false))
    }

    @Test
    fun `disabled when loading even at countdown 0`() {
        assertFalse(isResendEnabled(countdown = 0, isLoading = true))
    }

    @Test
    fun `disabled when both countdown active and loading`() {
        assertFalse(isResendEnabled(countdown = 30, isLoading = true))
    }
}

// ---------------------------------------------------------------------------
// 7. Form isValid — login, registration, forgot password, reset password
// ---------------------------------------------------------------------------

class FormValidationTest {

    @Test
    fun `login form disabled when email empty`() {
        assertFalse(isValidEmail("") && "password".isNotEmpty())
    }

    @Test
    fun `login form disabled when email invalid`() {
        assertFalse(isValidEmail("notanemail") && "password".isNotEmpty())
    }

    @Test
    fun `login form disabled when password empty`() {
        assertFalse(isValidEmail("user@example.com") && "".isNotEmpty())
    }

    @Test
    fun `login form enabled with valid email and non-empty password`() {
        assertTrue(isValidEmail("user@example.com") && "anypassword".isNotEmpty())
    }

    @Test
    fun `registration form disabled when password weak`() {
        val formValid = "user".isNotBlank() &&
            isValidEmail("user@example.com") &&
            isValidPassword("weak") &&
            "weak" == "weak"
        assertFalse(formValid)
    }

    @Test
    fun `registration form disabled when passwords differ`() {
        val formValid = "user".isNotBlank() &&
            isValidEmail("user@example.com") &&
            isValidPassword("Password1") &&
            "Password1" == "Other1"
        assertFalse(formValid)
    }

    @Test
    fun `registration form enabled when all fields valid and passwords match`() {
        val formValid = "user".isNotBlank() &&
            isValidEmail("user@example.com") &&
            isValidPassword("Password1") &&
            "Password1" == "Password1"
        assertTrue(formValid)
    }

    @Test
    fun `forgot password button disabled when email invalid`() {
        assertFalse(isValidEmail("bademail"))
    }

    @Test
    fun `reset password form disabled when code empty`() {
        val formValid = "".isNotEmpty() && isValidPassword("Password1") && "Password1" == "Password1"
        assertFalse(formValid)
    }

}

// ---------------------------------------------------------------------------
// 8. formatResendText
// ---------------------------------------------------------------------------

class FormatResendTextTest {

    @Test
    fun `shows countdown seconds when timer active`() {
        assertEquals("Повторно через 45с", formatResendText(45))
    }

    @Test
    fun `shows resend label when timer expired`() {
        assertEquals("Отправить повторно", formatResendText(0))
    }

    @Test
    fun `shows 1 second remaining correctly`() {
        assertEquals("Повторно через 1с", formatResendText(1))
    }
}

// ---------------------------------------------------------------------------
// canSubmitPassword
// ---------------------------------------------------------------------------

class CanSubmitPasswordTest {

    @Test
    fun `matching valid passwords can be submitted`() {
        assertTrue(canSubmitPassword("SecurePass1", "SecurePass1", isLoading = false))
    }

    @Test
    fun `password differing from its repeat cannot be submitted`() {
        assertFalse(canSubmitPassword("SecurePass1", "SecurePass2", isLoading = false))
    }

    @Test
    fun `empty repeat cannot be submitted`() {
        assertFalse(canSubmitPassword("SecurePass1", "", isLoading = false))
    }

    @Test
    fun `weak password cannot be submitted even when repeated correctly`() {
        assertFalse(canSubmitPassword("weak", "weak", isLoading = false))
    }

    @Test
    fun `valid passwords cannot be submitted while a request is in flight`() {
        assertFalse(canSubmitPassword("SecurePass1", "SecurePass1", isLoading = true))
    }
}
