package org.example.proteinstructure.detail

sealed interface ProteinDetailIntent {
    data class RelatedProteinClicked(val id: String) : ProteinDetailIntent
}
