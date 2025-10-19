package com.ownstd.project.pincard.internal.presentation.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.ownstd.project.storage.ClipboardManager

@Composable
actual fun rememberClipboardManager(): ClipboardManager {
    return remember { ClipboardManager() }
}
