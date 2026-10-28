package com.ownstd.project.authorization.ui

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.ownstd.project.authorization.internal.domain.AuthorizationRepository
import com.ownstd.project.authorization.internal.presentation.AuthTestTags
import com.ownstd.project.authorization.internal.presentation.AuthorizationScreen
import com.ownstd.project.authorization.internal.presentation.AuthorizationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.koin.compose.KoinIsolatedContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.koinApplication
import org.koin.dsl.module

/**
 * Hosts the real [AuthorizationScreen]; an isolated Koin context hands it a view model backed by [repository]:
 * usually a controllable fake, or the real repository over a mock HTTP engine for end-to-end checks.
 * The global Koin context is not used: Compose caches its root scope statically,
 * so a context restarted for the next test is seen as closed.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class AuthScreenRule<R : AuthorizationRepository>(val repository: R) : TestRule {
    val compose = createComposeRule()

    // Repository calls finish before the next assertion,
    // while delay() (resend timers, slow login) waits for virtual time.
    private val dispatcher = UnconfinedTestDispatcher()

    private val koin = koinApplication {
        modules(module { viewModel { AuthorizationViewModel(repository, dispatcher) } })
    }

    var isSessionOpened = false
        private set

    override fun apply(base: Statement, description: Description): Statement =
        compose.apply(withMainDispatcher(base), description)

    fun showAuthorizationScreen() {
        compose.setContent {
            KoinIsolatedContext(koin) { AuthorizationScreen(openSession = { isSessionOpened = true }) }
        }
    }

    /** Lets every pending timer run out, e.g. the resend cooldown. */
    fun runPendingTimers() {
        dispatcher.scheduler.advanceUntilIdle()
    }

    private fun withMainDispatcher(base: Statement) = object : Statement() {
        override fun evaluate() {
            Dispatchers.setMain(dispatcher)
            try {
                base.evaluate()
            } finally {
                koin.close()
                Dispatchers.resetMain()
            }
        }
    }
}

/** Field tags sit on the wrapper around the editable node, so the tagged node or its descendant is matched. */
internal fun SemanticsNodeInteractionsProvider.textField(tag: String): SemanticsNodeInteraction =
    onNode(hasSetTextAction() and (hasTestTag(tag) or hasAnyAncestor(hasTestTag(tag))))

internal fun SemanticsNodeInteractionsProvider.typeInto(tag: String, text: String) {
    textField(tag).performTextInput(text)
}

/** A message rendered inside the tagged container, e.g. a field's validation error. */
internal fun SemanticsNodeInteractionsProvider.textInside(tag: String, text: String): SemanticsNodeInteraction =
    onNode(hasText(text) and hasAnyAncestor(hasTestTag(tag)))

internal fun ComposeContentTestRule.logIn(email: String, password: String) {
    typeInto(AuthTestTags.LOGIN_EMAIL_FIELD, email)
    typeInto(AuthTestTags.LOGIN_PASSWORD_FIELD, password)
    onNodeWithTag(AuthTestTags.LOGIN_SUBMIT_BUTTON).performClick()
    waitForIdle()
}
