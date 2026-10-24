package org.example.proteinstructure

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun setAppLocale(language: String)

expect fun getAppLocale(): String
