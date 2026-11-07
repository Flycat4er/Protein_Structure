package org.example.proteinstructure

sealed interface Screen {
    data object List : Screen
    data class Detail(val id: String) : Screen
}
