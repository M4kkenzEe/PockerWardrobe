package com.ownstd.project.card.internal.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.ownstd.project.card.internal.presentation.viewmodel.SecondViewModel
import com.ownstd.project.tiktok_feed.presentation.ui.TikTokFeedScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ProfileScreen(navigateTo: () -> Unit = {}) {
    val viewModel = koinViewModel<SecondViewModel>()
    val st by viewModel.state.collectAsState()
    TikTokFeedScreen()
}