package org.example.proteinstructure

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.example.proteinstructure.resources.Res
import org.example.proteinstructure.resources.app_title
import org.jetbrains.compose.resources.stringResource

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = stringResource(Res.string.app_title),
    ) {
        App()
    }
}
