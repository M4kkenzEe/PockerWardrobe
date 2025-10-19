package com.ownstd.project.pincard.internal.presentation.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.ownstd.project.storage.ClipboardManager

@Composable
actual fun rememberClipboardManager(): ClipboardManager {
    val context = LocalContext.current
    return remember { ClipboardManager(context) }
}
