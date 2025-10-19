package com.ownstd.project.storage

import platform.UIKit.UIPasteboard

actual class ClipboardManager {
    actual fun copyToClipboard(text: String) {
        UIPasteboard.generalPasteboard.string = text
    }
}

actual fun getClipboardManager(): ClipboardManager {
    return ClipboardManager()
}
