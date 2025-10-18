package com.ownstd.project.pincard.internal.external

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Android implementation для расшаривания контента
 */
actual class ShareManager(private val context: Context) {

    actual fun shareText(text: String, title: String?) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, title)
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }
}

/**
 * Composable для создания ShareManager в Android
 */
@Composable
actual fun rememberShareManager(): ShareManager {
    val context = LocalContext.current
    return remember { ShareManager(context) }
}
