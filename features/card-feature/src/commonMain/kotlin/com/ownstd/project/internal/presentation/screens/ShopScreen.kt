package com.ownstd.project.internal.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ownstd.project.WardrobeScreen
import com.ownstd.project.internal.presentation.viewmodel.SecondViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ShopScreen(navigateTo: () -> Unit = {}) {
    WardrobeScreen()
}