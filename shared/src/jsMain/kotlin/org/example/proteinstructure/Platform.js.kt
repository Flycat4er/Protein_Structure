package org.example.proteinstructure

class JsPlatform : Platform {
    override val name: String = "JavaScript"
}

actual fun getPlatform(): Platform = JsPlatform()

private var currentJsLocale: String? = null

actual fun setAppLocale(language: String) {
    currentJsLocale = language
    js("""
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
    """)
}

actual fun getAppLocale(): String {
    currentJsLocale?.let { return it }
    val detected: String = js("""
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
    """).toString()
    return detected
}
