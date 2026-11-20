import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    jvm()

    js {
        browser()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    android {
        namespace = "org.example.proteinstructure.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
            implementation(libs.ktor.client.okhttp)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.navigation3.runtime)
            implementation(libs.navigation3.ui)
            implementation(libs.kotlinx.coroutinesCore)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.contentNegotiation)
            implementation(libs.ktor.serialization.kotlinxJson)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        jsMain.dependencies {
            implementation(libs.wrappers.browser)
            implementation(libs.ktor.client.js)
        }
        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}

val isWindows = System.getProperty("os.name").lowercase().contains("windows")

// Compose compiler metrics ломаются на Windows из-за бага KT-87355 (двоеточие ':' в имени модуля распознается как NTFS-поток).
// Включаем метрики на Linux/macOS или при явном указании -PcomposeCompilerReports=true.
if (!isWindows || project.findProperty("composeCompilerReports") == "true") {
    composeCompiler {
        reportsDestination = layout.buildDirectory.dir("compose_compiler")
        metricsDestination = layout.buildDirectory.dir("compose_compiler")
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "org.example.proteinstructure.resources"
}

val generateBuildConfig = tasks.register("generateBuildConfig") {
    val searchUrl = providers.gradleProperty("rcsb.search.url").getOrElse("https://search.rcsb.org/rcsbsearch/v2/query")
    val dataUrl = providers.gradleProperty("rcsb.data.url").getOrElse("https://data.rcsb.org/rest/v1/core")
    val outputDir = layout.buildDirectory.dir("generated/buildconfig/org/example/proteinstructure").get().asFile

    inputs.property("searchUrl", searchUrl)
    inputs.property("dataUrl", dataUrl)
    outputs.dir(outputDir)

    doLast {
        outputDir.mkdirs()
        val file = File(outputDir, "BuildConfig.kt")
        file.writeText(
            """
            package org.example.proteinstructure

            object BuildConfig {
                const val SEARCH_API_URL: String = "$searchUrl"
                const val DATA_API_URL: String = "$dataUrl"
            }
            """.trimIndent()
        )
    }
}

kotlin.sourceSets.getByName("commonMain").kotlin.srcDir(layout.buildDirectory.dir("generated/buildconfig"))

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().configureEach {
    dependsOn(generateBuildConfig)
}
