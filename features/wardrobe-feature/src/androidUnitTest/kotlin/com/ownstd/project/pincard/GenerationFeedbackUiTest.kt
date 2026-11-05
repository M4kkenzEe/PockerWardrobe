package com.ownstd.project.pincard

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.ownstd.project.pincard.internal.data.model.MissingCategory
import com.ownstd.project.pincard.internal.presentation.compose.GenerateLooksFeedback
import com.ownstd.project.pincard.internal.presentation.viewmodel.GenerateLooksError
import org.jetbrains.compose.resources.PreviewContextConfigurationEffect
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.junit.Before
import org.robolectric.Robolectric
import android.content.pm.ProviderInfo
import android.content.ContentProvider
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertTrue

@OptIn(ExperimentalResourceApi::class)
@RunWith(RobolectricTestRunner::class)
class GenerationFeedbackUiTest {
    @Before fun initializeResources() {
        val provider = Class.forName("org.jetbrains.compose.resources.AndroidContextProvider").asSubclass(ContentProvider::class.java)
        Robolectric.buildContentProvider(provider).create(ProviderInfo().apply { authority = "test.compose.resources" })
    }

    @get:Rule val compose = createComposeRule()

    @Test fun missingCategoriesAndAddAction() {
        var added = false
        compose.setContent { PreviewContextConfigurationEffect();
            GenerateLooksFeedback(GenerateLooksError.NotEnoughClothes(listOf(MissingCategory.Bottom, MissingCategory.Shoes)), {}, { added = true })
        }
        compose.onNodeWithText("Чтобы составить образ, добавь низ и обувь").assertExists()
        compose.onNodeWithText("Добавить вещь").performClick()
        assertTrue(added)
    }
    @Test fun genericFeedbackCanBeDismissed() {
        var dismissed = false
        compose.setContent { PreviewContextConfigurationEffect(); GenerateLooksFeedback(GenerateLooksError.NotEnoughClothes(emptyList()), { dismissed = true }, {}) }
        compose.onNodeWithText("Добавь вещи в гардероб, чтобы составить образ").assertExists()
        compose.onNodeWithText("Закрыть").performClick()
        assertTrue(dismissed)
    }
    @Test fun noNewLooksOffersAddingInsteadOfMissingCategories() {
        compose.setContent { PreviewContextConfigurationEffect(); GenerateLooksFeedback(GenerateLooksError.NoNewLooks, {}, {}) }
        compose.onNodeWithText("Новых образов пока нет. Добавь ещё вещей — появятся новые сочетания").assertExists()
        compose.onNodeWithText("Добавить вещь").assertExists()
    }
    @Test fun networkErrorOffersRetry() {
        var retried = false
        compose.setContent { PreviewContextConfigurationEffect(); GenerateLooksFeedback(GenerateLooksError.NetworkError, {}, { retried = true }) }
        compose.onNodeWithText("Повторить").performClick()
        compose.onNodeWithText("Добавить вещь").assertDoesNotExist()
        assertTrue(retried)
    }
}
