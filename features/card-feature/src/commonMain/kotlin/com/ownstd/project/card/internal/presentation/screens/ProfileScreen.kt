package com.ownstd.project.card.internal.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.ownstd.project.profile.presentation.ProfileScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ProfileScreen(onLogout: () -> Unit = {}, onOpenDebug: () -> Unit = {}) {
    ProfileScreen(onLogout = onLogout, onOpenDebug = onOpenDebug)
}