package com.ownstd.project.storage

expect class ClipboardManager {
    fun copyToClipboard(text: String)
}

expect fun getClipboardManager(): ClipboardManager
