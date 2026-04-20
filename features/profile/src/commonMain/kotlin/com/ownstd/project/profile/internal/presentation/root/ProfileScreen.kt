package com.ownstd.project.profile.internal.presentation.root

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ownstd.project.core.compose.components.AppTopBar
import com.ownstd.project.core.compose.components.SettingRow
import com.ownstd.project.core.compose.components.StatsRow
import com.ownstd.project.core.compose.foundation.handle
import com.ownstd.project.core.compose.theme.Theme
import com.ownstd.project.core.resources.MR
import com.ownstd.project.profile.internal.domain.model.UserModel
import com.ownstd.project.profile.internal.presentation.root.interactionModel.ProfileIntent
import com.ownstd.project.profile.internal.presentation.root.interactionModel.ProfileSideEffect
import com.ownstd.project.profile.internal.presentation.root.interactionModel.ProfileState
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import pro.respawn.flowmvi.compose.dsl.subscribe
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onNavigateToEdit: () -> Unit,
    onNavigateToSizes: () -> Unit,
    onNavigateToAuth: () -> Unit,
) {
    val viewModel = koinViewModel<ProfileViewModel>()
    val scope = rememberCoroutineScope()

    viewModel.sideEffect.handle { effect ->
        when (effect) {
            is ProfileSideEffect.NavigateToEdit -> onNavigateToEdit()
            is ProfileSideEffect.NavigateToSizes -> onNavigateToSizes()
            is ProfileSideEffect.NavigateToAuth -> onNavigateToAuth()
            is ProfileSideEffect.ShowError ->
                scope.launch { /* snackbar если нужен */ }
        }
    }

    val state by viewModel.store.subscribe()

    ProfileContent(
        state = state,
        onBack = onBack,
        onIntent = { viewModel.store.intent(it) },
    )
}

@Composable
private fun ProfileContent(
    state: ProfileState,
    onBack: () -> Unit,
    onIntent: (ProfileIntent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Theme.colors.background.primary),
    ) {
        AppTopBar(title = "Профиль", onBack = onBack)

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Theme.colors.label.primary)
            }
            return
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Avatar
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(Theme.colors.background.surface),
                contentAlignment = Alignment.Center,
            ) {
                if (state.user?.avatarUrl != null) {
                    AsyncImage(
                        model = state.user.avatarUrl,
                        contentDescription = "Аватар",
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Text(
                        text = state.user?.name?.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        style = Theme.typography.h2,
                        color = Theme.colors.label.primary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = state.user?.name ?: "",
                style = Theme.typography.h3,
                color = Theme.colors.label.primary,
            )

            Text(
                text = "@${state.user?.username ?: ""}",
                style = Theme.typography.caption,
                color = Theme.colors.label.secondary,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { onIntent(ProfileIntent.EditProfileClicked) },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Theme.colors.background.surface,
                    contentColor = Theme.colors.label.primary,
                ),
                elevation = ButtonDefaults.elevation(0.dp),
            ) {
                Text(text = "Редактировать профиль", style = Theme.typography.caption)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Stats
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(Theme.colors.background.surface, RoundedCornerShape(12.dp)),
            ) {
                StatsRow(
                    outfitsCount = state.user?.outfitsCount ?: 0,
                    clothesCount = state.user?.clothesCount ?: 0,
                    sharedCount = state.user?.sharedCount ?: 0,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Settings section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(Theme.colors.background.surface, RoundedCornerShape(12.dp)),
            ) {
                SettingRow(
                    title = "Размеры",
                    subtitle = state.user?.let { "EU · S" },
                    icon = MR.images.chevron_right,
                    onClick = { onIntent(ProfileIntent.SizesClicked) },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Theme toggle
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(Theme.colors.background.surface, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 4.dp),
            ) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (state.isDarkTheme) "Тёмная тема" else "Светлая тема",
                        style = Theme.typography.body,
                        color = Theme.colors.label.primary,
                        modifier = Modifier.weight(1f),
                    )
                    Switch(
                        checked = state.isDarkTheme,
                        onCheckedChange = { onIntent(ProfileIntent.ToggleTheme) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Theme.colors.accent.primary,
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Logout
            Button(
                onClick = { onIntent(ProfileIntent.LogoutClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Theme.colors.label.error,
                    contentColor = Theme.colors.label.onError,
                ),
                elevation = ButtonDefaults.elevation(0.dp),
            ) {
                Text(text = "Выйти из аккаунта", style = Theme.typography.body)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (state.showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { onIntent(ProfileIntent.LogoutDismissed) },
            title = { Text("Выйти из аккаунта?", style = Theme.typography.h3) },
            text = { Text("Вы уверены, что хотите выйти?", style = Theme.typography.body) },
            confirmButton = {
                TextButton(onClick = { onIntent(ProfileIntent.LogoutConfirmed) }) {
                    Text("Выйти", color = Theme.colors.label.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { onIntent(ProfileIntent.LogoutDismissed) }) {
                    Text("Отмена", color = Theme.colors.label.secondary)
                }
            },
        )
    }
}

@Preview
@Composable
private fun ProfileScreenPreview() {
    ProfileContent(
        state = ProfileState(
            isLoading = false,
            user = UserModel(
                id = 1,
                name = "Алиса",
                email = "alice@example.com",
                username = "alice",
                outfitsCount = 42,
                clothesCount = 167,
                sharedCount = 28,
            ),
        ),
        onBack = {},
        onIntent = {},
    )
}

@Preview
@Composable
private fun ProfileScreenLoadingPreview() {
    ProfileContent(
        state = ProfileState(isLoading = true),
        onBack = {},
        onIntent = {},
    )
}
