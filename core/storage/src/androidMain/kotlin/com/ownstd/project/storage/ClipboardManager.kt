package com.ownstd.project.storage

import android.content.ClipData
import android.content.ClipboardManager as AndroidClipboardManager
import android.content.Context

actual class ClipboardManager(private val context: Context) {
    actual fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as AndroidClipboardManager
        val clip = ClipData.newPlainText("share_url", text)
        clipboard.setPrimaryClip(clip)
    }
}

actual fun getClipboardManager(): ClipboardManager {
    throw UnsupportedOperationException("Use getClipboardManager(context: Context) on Android")
}

fun getClipboardManager(context: Context): ClipboardManager {
    return ClipboardManager(context)
}
