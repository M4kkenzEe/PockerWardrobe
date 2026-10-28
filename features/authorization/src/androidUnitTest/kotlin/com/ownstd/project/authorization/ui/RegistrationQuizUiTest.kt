package com.ownstd.project.authorization.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import com.ownstd.project.authorization.ControllableAuthorizationRepository
import com.ownstd.project.authorization.internal.domain.EMAIL_ALREADY_REGISTERED_MESSAGE
import com.ownstd.project.authorization.internal.domain.INVALID_OR_EXPIRED_CODE_MESSAGE
import com.ownstd.project.authorization.internal.presentation.AuthTestTags
import com.ownstd.project.authorization.internal.presentation.NAME_REQUIRED_ERROR
import com.ownstd.project.authorization.internal.presentation.PASSWORD_RULE_ERROR
import com.ownstd.project.authorization.internal.presentation.confirmPasswordErrorMessage
import com.ownstd.project.authorization.internal.presentation.emailErrorMessage
import com.ownstd.project.authorization.internal.presentation.formatResendText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
internal class RegistrationQuizUiTest {

    @get:Rule
    val rule = AuthScreenRule(ControllableAuthorizationRepository())

    private val compose get() = rule.compose

    @Test
    fun `completing all quiz steps creates the account and opens the session`() {
        openQuiz()

        passNameStep()
        passEmailStep()
        enterOtp(CODE)
        passPasswordStep(PASSWORD, PASSWORD)

        assertTrue(rule.isSessionOpened)
        assertEquals(EMAIL to PASSWORD, rule.repository.lastRegistration)
    }

    @Test
    fun `blank name shows the hint and keeps next disabled`() {
        openQuiz()

        compose.typeInto(AuthTestTags.NAME_FIELD, NAME)
        compose.textField(AuthTestTags.NAME_FIELD).performTextClearance()

        compose.textInside(AuthTestTags.NAME_FIELD, NAME_REQUIRED_ERROR).assertIsDisplayed()
        compose.onNodeWithTag(AuthTestTags.NAME_NEXT_BUTTON).assertIsNotEnabled()
    }

    @Test
    fun `invalid email shows the format error and keeps next disabled`() {
        openQuiz()
        passNameStep()

        compose.typeInto(AuthTestTags.EMAIL_FIELD, INVALID_EMAIL)

        val formatError = emailErrorMessage(INVALID_EMAIL, touched = true)!!
        compose.textInside(AuthTestTags.EMAIL_FIELD, formatError).assertIsDisplayed()
        compose.onNodeWithTag(AuthTestTags.EMAIL_NEXT_BUTTON).assertIsNotEnabled()
    }

    @Test
    fun `already registered email keeps the user on the email step with the error`() {
        rule.repository.registrationCodeError = EMAIL_ALREADY_REGISTERED_MESSAGE
        openQuiz()
        passNameStep()

        passEmailStep()

        compose.onNodeWithTag(AuthTestTags.ONBOARDING_ERROR).assertTextEquals(EMAIL_ALREADY_REGISTERED_MESSAGE)
        compose.onNodeWithTag(AuthTestTags.EMAIL_STEP).assertIsDisplayed()
    }

    @Test
    fun `wrong code shows the inline error and keeps the user on the code step`() {
        rule.repository.verifyCodeError = INVALID_OR_EXPIRED_CODE_MESSAGE
        openQuizAtCodeStep()

        enterOtp(CODE)

        compose.textInside(AuthTestTags.OTP_INPUT, INVALID_OR_EXPIRED_CODE_MESSAGE).assertIsDisplayed()
        compose.onNodeWithTag(AuthTestTags.OTP_STEP).assertIsDisplayed()
    }

    @Test
    fun `resend code is blocked until the cooldown ends`() {
        openQuizAtCodeStep()
        compose.onNodeWithTag(AuthTestTags.OTP_RESEND_BUTTON).assertIsNotEnabled()

        rule.runPendingTimers()

        compose.onNodeWithTag(AuthTestTags.OTP_RESEND_BUTTON)
            .assertIsEnabled()
            .assertTextEquals(formatResendText(0))
    }

    @Test
    fun `verified email continues to the password step without a new code`() {
        openQuizAtCodeStep()
        enterOtp(CODE)
        compose.onNodeWithTag(AuthTestTags.ONBOARDING_BACK_BUTTON).performClick()

        compose.onNodeWithTag(AuthTestTags.OTP_CONTINUE_BUTTON).performClick()

        compose.onNodeWithTag(AuthTestTags.PASSWORD_STEP).assertIsDisplayed()
        assertEquals(listOf(EMAIL), rule.repository.registrationCodeEmails)
    }

    @Test
    fun `weak password shows the password rule and keeps submit disabled`() {
        openQuizAtCodeStep()
        enterOtp(CODE)

        compose.typeInto(AuthTestTags.PASSWORD_FIELD, "password")

        compose.textInside(AuthTestTags.PASSWORD_FIELD, PASSWORD_RULE_ERROR).assertIsDisplayed()
        compose.onNodeWithTag(AuthTestTags.PASSWORD_SUBMIT_BUTTON).assertIsNotEnabled()
    }

    @Test
    fun `mismatched passwords show the error and keep submit disabled`() {
        openQuizAtCodeStep()
        enterOtp(CODE)

        compose.typeInto(AuthTestTags.PASSWORD_FIELD, PASSWORD)
        compose.typeInto(AuthTestTags.PASSWORD_CONFIRM_FIELD, OTHER_PASSWORD)

        val mismatchError = confirmPasswordErrorMessage(PASSWORD, OTHER_PASSWORD, touched = true)!!
        compose.textInside(AuthTestTags.PASSWORD_CONFIRM_FIELD, mismatchError).assertIsDisplayed()
        compose.onNodeWithTag(AuthTestTags.PASSWORD_SUBMIT_BUTTON).assertIsNotEnabled()
        assertFalse(rule.isSessionOpened)
    }

    @Test
    fun `going back through the quiz keeps name and email and ends on login`() {
        openQuizAtCodeStep()

        compose.onNodeWithTag(AuthTestTags.ONBOARDING_BACK_BUTTON).performClick()
        compose.textField(AuthTestTags.EMAIL_FIELD).assertTextContains(EMAIL)

        compose.onNodeWithTag(AuthTestTags.ONBOARDING_BACK_BUTTON).performClick()
        compose.textField(AuthTestTags.NAME_FIELD).assertTextContains(NAME)

        compose.onNodeWithTag(AuthTestTags.ONBOARDING_BACK_BUTTON).performClick()
        compose.onNodeWithTag(AuthTestTags.LOGIN_SCREEN).assertIsDisplayed()
    }

    private fun openQuiz() {
        rule.showAuthorizationScreen()
        compose.onNodeWithTag(AuthTestTags.LOGIN_REGISTER_LINK).performClick()
    }

    private fun openQuizAtCodeStep() {
        openQuiz()
        passNameStep()
        passEmailStep()
    }

    private fun passNameStep() {
        compose.typeInto(AuthTestTags.NAME_FIELD, NAME)
        compose.onNodeWithTag(AuthTestTags.NAME_NEXT_BUTTON).performClick()
    }

    private fun passEmailStep() {
        compose.typeInto(AuthTestTags.EMAIL_FIELD, EMAIL)
        compose.onNodeWithTag(AuthTestTags.EMAIL_NEXT_BUTTON).performClick()
    }

    private fun enterOtp(code: String) {
        compose.typeInto(AuthTestTags.OTP_INPUT, code)
        compose.waitForIdle()
    }

    private fun passPasswordStep(password: String, confirmation: String) {
        compose.typeInto(AuthTestTags.PASSWORD_FIELD, password)
        compose.typeInto(AuthTestTags.PASSWORD_CONFIRM_FIELD, confirmation)
        compose.onNodeWithTag(AuthTestTags.PASSWORD_SUBMIT_BUTTON).performClick()
        compose.waitForIdle()
    }

    private companion object {
        const val NAME = "Анна"
        const val EMAIL = "anna@example.com"
        const val INVALID_EMAIL = "anna.example.com"
        const val CODE = "123456"
        const val PASSWORD = "Secure123"
        const val OTHER_PASSWORD = "Secure124"
    }
}
