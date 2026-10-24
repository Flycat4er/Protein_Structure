package org.example.proteinstructure

import java.util.Locale

class JVMPlatform : Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual fun setAppLocale(language: String) {
    Locale.setDefault(Locale(language))
}

actual fun getAppLocale(): String {
    return Locale.getDefault().language
}
