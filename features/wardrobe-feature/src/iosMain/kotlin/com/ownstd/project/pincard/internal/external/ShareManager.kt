package com.ownstd.project.pincard.internal.external

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow

/**
 * iOS implementation для расшаривания контента
 */
actual class ShareManager {

    actual fun shareText(text: String, title: String?) {
        val activityItems = listOf(text)
        val activityController = UIActivityViewController(
            activityItems = activityItems,
            applicationActivities = null
        )

        val window = UIApplication.sharedApplication.windows.firstOrNull() as? UIWindow
        val rootViewController = window?.rootViewController

        rootViewController?.presentViewController(
            viewControllerToPresent = activityController,
            animated = true,
            completion = null
        )
    }
}

/**
 * Composable для создания ShareManager в iOS
 */
@Composable
actual fun rememberShareManager(): ShareManager {
    return remember { ShareManager() }
}
