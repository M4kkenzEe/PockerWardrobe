package com.ownstd.project

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIRectEdgeAll
import platform.UIKit.UIViewController


fun MainViewController(): UIViewController {
    val viewController = ComposeUIViewController {
        App()
    }

    viewController.setNeedsStatusBarAppearanceUpdate()
    viewController.edgesForExtendedLayout = UIRectEdgeAll

    // Скрытие статус бара

    // Возвращаем наш настроенный UIViewController
    return viewController
}

//fun MainViewController() = ComposeUIViewController {
//    App()
//}
//
//
//    .apply {
//        // Скрываем статус-бар
//        setNeedsStatusBarAppearanceUpdate()
//    }
