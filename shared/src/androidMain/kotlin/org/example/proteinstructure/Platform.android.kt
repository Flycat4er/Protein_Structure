package org.example.proteinstructure

import android.os.Build
import java.util.Locale

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun setAppLocale(language: String) {
    Locale.setDefault(Locale(language))
}

actual fun getAppLocale(): String {
    return Locale.getDefault().language
}
