package com.ownstd.project.core.deeplink

import android.content.Intent

/**
 * Вызывается из MainActivity.onCreate / onNewIntent для передачи входящего deeplink.
 *
 * ```kotlin
 * // MainActivity.kt
 * override fun onCreate(savedInstanceState: Bundle?) {
 *     super.onCreate(savedInstanceState)
 *     intent?.data?.toString()?.let { DeepLinkReceiver.handle(it) }
 * }
 * override fun onNewIntent(intent: Intent) {
 *     super.onNewIntent(intent)
 *     intent.data?.toString()?.let { DeepLinkReceiver.handle(it) }
 * }
 * ```
 */
object DeepLinkReceiver {
    fun handle(intent: Intent) {
        intent.data?.toString()?.let { uri ->
            if (uri.startsWith("clothis://")) {
                DeepLinkManager.emit(uri)
            }
        }
    }

    fun handle(uri: String) {
        if (uri.startsWith("clothis://")) {
            DeepLinkManager.emit(uri)
        }
    }
}
