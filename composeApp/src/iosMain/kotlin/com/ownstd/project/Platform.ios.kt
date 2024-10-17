package com.ownstd.project

import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun getOSVersion(): String {
    return UIDevice.currentDevice.systemVersion // Исправлено с current на currentDevice
}