package org.example.proteinstructure.list

sealed interface ProteinListIntent {
    data class CardClicked(val id: String) : ProteinListIntent
    data class QueryChanged(val value: String) : ProteinListIntent
}
