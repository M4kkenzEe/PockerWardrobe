package com.ownstd.project.card.internal.presentation.screens

import androidx.compose.runtime.Composable
import com.ownstd.project.pincard.external.ImagePickerApp
import com.ownstd.project.pincard.internal.presentation.compose.ConstructorScreen
import com.ownstd.project.pincard.internal.presentation.compose.WardrobeScreen

@Composable
internal fun SettingsScreen(navigateTo: () -> Unit = {}) {
//    ConstructorScreen()
    ImagePickerApp()
}