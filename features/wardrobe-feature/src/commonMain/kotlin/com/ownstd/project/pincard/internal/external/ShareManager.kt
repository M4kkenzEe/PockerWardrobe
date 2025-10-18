package com.ownstd.project.pincard.internal.external

import androidx.compose.runtime.Composable

/**
 * Platform-specific manager для расшаривания контента
 */
expect class ShareManager {
    /**
     * Открывает системный диалог для расшаривания текста/ссылки
     * @param text Текст для расшаривания
     * @param title Заголовок диалога (опционально)
     */
    fun shareText(text: String, title: String? = null)
}

/**
 * Platform-specific Composable для создания ShareManager
 */
@Composable
expect fun rememberShareManager(): ShareManager
