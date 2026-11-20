package org.example.proteinstructure

import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun setAppLocale(language: String) {}

actual fun getAppLocale(): String = "ru"
