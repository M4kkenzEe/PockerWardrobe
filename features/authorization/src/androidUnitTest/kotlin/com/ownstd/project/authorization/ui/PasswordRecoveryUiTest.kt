package com.ownstd.project.authorization.ui

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.ownstd.project.authorization.ControllableAuthorizationRepository
import com.ownstd.project.authorization.internal.domain.RESET_INVALID_CODE_MESSAGE
import com.ownstd.project.authorization.internal.presentation.AuthTestTags
import com.ownstd.project.authorization.internal.presentation.PASSWORD_CHANGED_NOTICE
import com.ownstd.project.authorization.internal.presentation.formatResendText
import com.ownstd.project.authorization.internal.presentation.resetCodeSentText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
internal class PasswordRecoveryUiTest {

    @get:Rule
    val rule = AuthScreenRule(ControllableAuthorizationRepository())

    private val compose get() = rule.compose

    @Test
    fun `requesting a code opens the code screen for that email`() {
        openForgotPassword()

        requestCode()

        compose.onNodeWithTag(AuthTestTags.RESET_PASSWORD_SCREEN).assertIsDisplayed()
        compose.onNodeWithTag(AuthTestTags.RESET_CODE_SENT_TEXT)
            .assertTextEquals(resetCodeSentText(EMAIL, isCodeResent = false))
        assertEquals(listOf(EMAIL), rule.repository.passwordResetRequestEmails)
    }

    @Test
    fun `resend is blocked until the cooldown ends and then sends a new code`() {
        openForgotPassword()
        requestCode()
        compose.onNodeWithTag(AuthTestTags.RESET_RESEND_LINK).assertHasNoClickAction()

        rule.runPendingTimers()
        compose.onNodeWithTag(AuthTestTags.RESET_RESEND_LINK)
            .assertTextEquals(formatResendText(0))
            .assertHasClickAction()
            .performClick()

        compose.onNodeWithTag(AuthTestTags.RESET_CODE_SENT_TEXT)
            .assertTextEquals(resetCodeSentText(EMAIL, isCodeResent = true))
        assertEquals(listOf(EMAIL, EMAIL), rule.repository.passwordResetRequestEmails)
    }

    @Test
    fun `code and new password lead to the login screen with a notice and no session`() {
        openForgotPassword()
        requestCode()

        submitNewPassword()

        compose.onNodeWithTag(AuthTestTags.LOGIN_SCREEN).assertIsDisplayed()
        compose.onNodeWithTag(AuthTestTags.LOGIN_NOTICE).assertTextEquals(PASSWORD_CHANGED_NOTICE)
        compose.onNodeWithTag(AuthTestTags.RESET_PASSWORD_SCREEN).assertDoesNotExist()
        compose.onNodeWithTag(AuthTestTags.FORGOT_PASSWORD_SCREEN).assertDoesNotExist()
        assertFalse(rule.isSessionOpened)
        assertEquals(Triple(EMAIL, CODE, NEW_PASSWORD), rule.repository.lastReset)
    }

    @Test
    fun `login form is prefilled with the email of the reset`() {
        openForgotPassword()
        requestCode()

        submitNewPassword()

        compose.textField(AuthTestTags.LOGIN_EMAIL_FIELD).assertTextContains(EMAIL)
    }

    @Test
    fun `signing in with the new password opens the session and drops the notice`() {
        openForgotPassword()
        requestCode()
        submitNewPassword()

        compose.typeInto(AuthTestTags.LOGIN_PASSWORD_FIELD, NEW_PASSWORD)
        compose.onNodeWithTag(AuthTestTags.LOGIN_SUBMIT_BUTTON).performClick()
        compose.waitForIdle()

        assertEquals(EMAIL to NEW_PASSWORD, rule.repository.lastLogin)
        assertTrue(rule.isSessionOpened)
        compose.onNodeWithTag(AuthTestTags.LOGIN_NOTICE).assertDoesNotExist()
    }

    @Test
    fun `opening forgot password again hides the notice`() {
        openForgotPassword()
        requestCode()
        submitNewPassword()

        compose.onNodeWithTag(AuthTestTags.LOGIN_FORGOT_PASSWORD_LINK).performClick()
        compose.onNodeWithTag(AuthTestTags.FORGOT_PASSWORD_BACK_LINK).performClick()

        compose.onNodeWithTag(AuthTestTags.LOGIN_NOTICE).assertDoesNotExist()
    }

    @Test
    fun `wrong code shows the error and keeps the user on the code screen`() {
        rule.repository.resetPasswordError = RESET_INVALID_CODE_MESSAGE
        openForgotPassword()
        requestCode()

        submitNewPassword()

        compose.onNodeWithTag(AuthTestTags.RESET_ERROR).assertTextEquals(RESET_INVALID_CODE_MESSAGE)
        compose.onNodeWithTag(AuthTestTags.RESET_PASSWORD_SCREEN).assertIsDisplayed()
        compose.onNodeWithTag(AuthTestTags.LOGIN_NOTICE).assertDoesNotExist()
        assertFalse(rule.isSessionOpened)
    }

    @Test
    fun `back from forgot password returns to login`() {
        openForgotPassword()

        compose.onNodeWithTag(AuthTestTags.FORGOT_PASSWORD_BACK_LINK).performClick()

        compose.onNodeWithTag(AuthTestTags.LOGIN_SCREEN).assertIsDisplayed()
    }

    @Test
    fun `back from the code screen returns to login`() {
        openForgotPassword()
        requestCode()

        compose.onNodeWithTag(AuthTestTags.RESET_BACK_LINK).performClick()

        compose.onNodeWithTag(AuthTestTags.LOGIN_SCREEN).assertIsDisplayed()
    }

    private fun openForgotPassword() {
        rule.showAuthorizationScreen()
        compose.onNodeWithTag(AuthTestTags.LOGIN_FORGOT_PASSWORD_LINK).performClick()
    }

    private fun requestCode() {
        compose.typeInto(AuthTestTags.FORGOT_PASSWORD_EMAIL_FIELD, EMAIL)
        compose.onNodeWithTag(AuthTestTags.FORGOT_PASSWORD_SUBMIT_BUTTON).performClick()
    }

    private fun submitNewPassword() {
        compose.typeInto(AuthTestTags.RESET_CODE_FIELD, CODE)
        compose.typeInto(AuthTestTags.RESET_NEW_PASSWORD_FIELD, NEW_PASSWORD)
        compose.typeInto(AuthTestTags.RESET_CONFIRM_PASSWORD_FIELD, NEW_PASSWORD)
        compose.onNodeWithTag(AuthTestTags.RESET_SUBMIT_BUTTON).performClick()
        compose.waitForIdle()
    }

    private companion object {
        const val EMAIL = "anna@example.com"
        const val CODE = "934754"
        const val NEW_PASSWORD = "NewSecure123"
    }
}
