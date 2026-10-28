package com.ownstd.project.authorization.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import com.ownstd.project.authorization.ControllableAuthorizationRepository
import com.ownstd.project.authorization.internal.domain.INVALID_CREDENTIALS_MESSAGE
import com.ownstd.project.authorization.internal.presentation.AuthTestTags
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
internal class LoginScreenUiTest {

    @get:Rule
    val rule = AuthScreenRule(ControllableAuthorizationRepository())

    private val compose get() = rule.compose

    @Test
    fun `empty login form shows fields and keeps login button disabled`() {
        rule.showAuthorizationScreen()

        compose.textField(AuthTestTags.LOGIN_EMAIL_FIELD).assertIsDisplayed()
        compose.textField(AuthTestTags.LOGIN_PASSWORD_FIELD).assertIsDisplayed()
        compose.onNodeWithTag(AuthTestTags.LOGIN_SUBMIT_BUTTON).assertIsNotEnabled()
    }

    @Test
    fun `valid credentials open the session`() {
        rule.showAuthorizationScreen()

        compose.logIn(EMAIL, PASSWORD)

        assertTrue(rule.isSessionOpened)
        assertEquals(EMAIL to PASSWORD, rule.repository.lastLogin)
    }

    @Test
    fun `rejected credentials show the error and keep the user on login`() {
        rule.repository.loginError = INVALID_CREDENTIALS_MESSAGE
        rule.showAuthorizationScreen()

        compose.logIn(EMAIL, "WrongPass1")

        compose.onNodeWithTag(AuthTestTags.LOGIN_ERROR).assertTextEquals(INVALID_CREDENTIALS_MESSAGE)
        compose.onNodeWithTag(AuthTestTags.LOGIN_SCREEN).assertIsDisplayed()
        assertFalse(rule.isSessionOpened)
    }

    @Test
    fun `pending login shows a spinner and blocks the button`() {
        rule.repository.loginDelayMs = 5_000
        rule.showAuthorizationScreen()

        compose.logIn(EMAIL, PASSWORD)

        compose.onNodeWithTag(AuthTestTags.LOGIN_PROGRESS, useUnmergedTree = true).assertIsDisplayed()
        compose.onNodeWithTag(AuthTestTags.LOGIN_SUBMIT_BUTTON).assertIsNotEnabled()
        assertEquals(1, rule.repository.loginCalls)
        assertFalse(rule.isSessionOpened)
    }

    private companion object {
        const val EMAIL = "anna@example.com"
        const val PASSWORD = "Secure123"
    }
}
