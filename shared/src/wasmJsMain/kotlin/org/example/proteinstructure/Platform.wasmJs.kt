package org.example.proteinstructure

class WasmPlatform : Platform {
    override val name: String = "WebAssembly"
}

actual fun getPlatform(): Platform = WasmPlatform()

private var currentWasmLocale: String? = null

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
private fun jsSetLocale(language: String): Unit = js("""{
    try {
        window.__appLocale = language;
        var langs = [language];
        Object.defineProperty(window.navigator, 'languages', {
            get: function() { return langs; },
            configurable: true
        });
        Object.defineProperty(window.navigator, 'language', {
            get: function() { return language; },
            configurable: true
        });
        if (window.Navigator && window.Navigator.prototype) {
            Object.defineProperty(window.Navigator.prototype, 'languages', {
                get: function() { return langs; },
                configurable: true
            });
            Object.defineProperty(window.Navigator.prototype, 'language', {
                get: function() { return language; },
                configurable: true
            });
        }
    } catch (e) {
        console.error(e);
    }
}""")

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
private fun jsGetLocale(): String = js("""
    (function() {
        try {
            if (window.__appLocale) return window.__appLocale;
            if (window.navigator) {
                if (window.navigator.languages && window.navigator.languages.length > 0) {
                    return window.navigator.languages[0];
                }
                if (window.navigator.language) {
                    return window.navigator.language;
                }
            }
        } catch (e) {}
        return 'ru';
    })()
""")

actual fun setAppLocale(language: String) {
    currentWasmLocale = language
    jsSetLocale(language)
}

actual fun getAppLocale(): String {
    currentWasmLocale?.let { return it }
    return jsGetLocale()
}
