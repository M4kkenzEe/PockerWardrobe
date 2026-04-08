package com.ownstd.project.profile.internal.presentation.detail.editProfile

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ownstd.project.core.compose.components.AppTopBar
import com.ownstd.project.core.compose.foundation.handle
import com.ownstd.project.core.compose.theme.Theme
import com.ownstd.project.profile.internal.presentation.detail.editProfile.interactionModel.EditProfileIntent
import com.ownstd.project.profile.internal.presentation.detail.editProfile.interactionModel.EditProfileSideEffect
import com.ownstd.project.profile.internal.presentation.detail.editProfile.interactionModel.EditProfileState
import org.koin.compose.viewmodel.koinViewModel
import pro.respawn.flowmvi.compose.dsl.subscribe
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun EditProfileScreen(onBack: () -> Unit) {
    val viewModel = koinViewModel<EditProfileViewModel>()

    viewModel.sideEffect.handle { effect ->
        when (effect) {
            is EditProfileSideEffect.NavigateBack -> onBack()
            is EditProfileSideEffect.ShowError -> Unit
        }
    }

    val state by viewModel.store.subscribe()

    EditProfileContent(
        state = state,
        onBack = onBack,
        onIntent = { viewModel.store.intent(it) },
    )
}

@Composable
private fun EditProfileContent(
    state: EditProfileState,
    onBack: () -> Unit,
    onIntent: (EditProfileIntent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Theme.colors.background.primary),
    ) {
        AppTopBar(
            title = "Редактирование",
            onBack = onBack,
            trailingContent = {
                if (!state.isSaving) {
                    androidx.compose.material.TextButton(
                        onClick = { onIntent(EditProfileIntent.SaveClicked) },
                    ) {
                        Text(
                            text = "Сохранить",
                            style = Theme.typography.body,
                            color = Theme.colors.accent.primary,
                        )
                    }
                }
            },
        )

        if (state.isLoading || state.isSaving) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Theme.colors.label.primary)
            }
            return
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Avatar placeholder
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(Theme.colors.background.surface),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = state.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    style = Theme.typography.h2,
                    color = Theme.colors.label.primary,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = state.name,
                onValueChange = { onIntent(EditProfileIntent.NameChanged(it)) },
                label = { Text("Имя") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Theme.colors.label.primary,
                    focusedBorderColor = Theme.colors.accent.primary,
                    unfocusedBorderColor = Theme.colors.stroke.primary,
                    focusedLabelColor = Theme.colors.accent.primary,
                    unfocusedLabelColor = Theme.colors.label.secondary,
                ),
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = state.username,
                onValueChange = { onIntent(EditProfileIntent.UsernameChanged(it)) },
                label = { Text("@ Ник") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Theme.colors.label.primary,
                    focusedBorderColor = Theme.colors.accent.primary,
                    unfocusedBorderColor = Theme.colors.stroke.primary,
                    focusedLabelColor = Theme.colors.accent.primary,
                    unfocusedLabelColor = Theme.colors.label.secondary,
                ),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Пол",
                style = Theme.typography.caption,
                color = Theme.colors.label.secondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf("FEMALE" to "Женский", "MALE" to "Мужской", "OTHER" to "Другой")
                    .forEach { (value, label) ->
                        val isSelected = state.gender == value
                        Button(
                            onClick = { onIntent(EditProfileIntent.GenderChanged(value)) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = if (isSelected) Theme.colors.label.primary
                                    else Theme.colors.background.surface,
                                contentColor = if (isSelected) Theme.colors.background.primary
                                    else Theme.colors.label.primary,
                            ),
                            elevation = ButtonDefaults.elevation(0.dp),
                        ) {
                            Text(text = label, style = Theme.typography.caption)
                        }
                    }
            }
        }
    }
}

@Preview
@Composable
private fun EditProfileScreenPreview() {
    EditProfileContent(
        state = EditProfileState(
            isLoading = false,
            name = "Алиса",
            username = "alice",
            gender = "FEMALE",
        ),
        onBack = {},
        onIntent = {},
    )
}
