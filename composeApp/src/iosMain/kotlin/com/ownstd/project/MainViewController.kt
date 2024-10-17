package com.ownstd.project

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController {
    App()
}.apply {
    // Скрываем статус-бар
    setNeedsStatusBarAppearanceUpdate()
}

//class MainViewController : UIViewController(nibName = null, bundle = null) {
//
//    override fun viewDidLoad() {
//        super.viewDidLoad()
//        // Настройка представления Compose
//        setContent { App() }
//
//        // Скрываем статус-бар
//        UIApplication.shared.isStatusBarHidden = true
//    }
//
//    // Скрытие статус-бара
//    override fun prefersStatusBarHidden(): Boolean {
//        return true
//    }
//
//    // Обновление состояния статус-бара
//    override fun viewWillAppear(animated: Boolean) {
//        super.viewWillAppear(animated)
//        setNeedsStatusBarAppearanceUpdate()
//    }
//}


//class AppDelegate: UIResponder(), UIApplicationDelegate {
//
//    var window: UIWindow? = null
//
//    override fun application(application: UIApplication, didFinishLaunchingWithOptions launchOptions: Map<String, Any>?): Boolean {
//        window = UIWindow(frame = UIScreen.main.bounds)
//        window?.rootViewController = MainViewController() // Используем ваш MainViewController
//        window?.makeKeyAndVisible()
//        return true
//    }
//}


// Переопределение методов для управления статус-баром
//override fun prefersStatusBarHidden(): Boolean {
//    return true
//}
//
//override fun prefersHomeIndicatorAutoHidden(): Boolean {
//    return true
//}
