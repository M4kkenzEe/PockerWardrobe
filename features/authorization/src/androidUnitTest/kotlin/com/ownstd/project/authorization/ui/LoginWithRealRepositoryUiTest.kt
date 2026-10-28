package com.ownstd.project.authorization.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import com.ownstd.project.authorization.InMemoryTokenStorage
import com.ownstd.project.authorization.internal.data.repository.AuthServiceImpl
import com.ownstd.project.authorization.internal.data.repository.AuthorizationRepositoryImpl
import com.ownstd.project.authorization.internal.data.repository.INVALID_CREDENTIALS_TEXT
import com.ownstd.project.authorization.internal.presentation.AuthTestTags
import com.ownstd.project.authorization.networkRepositoryOver
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertFalse

/** Screen → view model → real repository and service → mock HTTP: checks the text the user actually reads. */
@RunWith(RobolectricTestRunner::class)
internal class LoginWithRealRepositoryUiTest {

    private val network = networkRepositoryOver(
        MockEngine { respond(content = "", status = HttpStatusCode.Unauthorized) }
    )

    @get:Rule
    val rule = AuthScreenRule(AuthorizationRepositoryImpl(AuthServiceImpl(network), InMemoryTokenStorage(), network))

    private val compose get() = rule.compose

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `login answered with 401 shows the invalid credentials message`() {
        rule.showAuthorizationScreen()

        compose.logIn("anna@example.com", "WrongPass1")

        // MockEngine answers on Ktor's own threads, so the Compose clock alone does not wait for the response.
        compose.waitUntilExactlyOneExists(hasTestTag(AuthTestTags.LOGIN_ERROR), timeoutMillis = 5_000)
        compose.onNodeWithTag(AuthTestTags.LOGIN_ERROR).assertTextEquals(INVALID_CREDENTIALS_TEXT)
        assertFalse(rule.isSessionOpened)
    }
}
