package org.example.proteinstructure.list

import org.example.proteinstructure.ui.model.ProteinCardUi

data class ProteinListState(
    val query: String = "",
    val items: List<ProteinCardUi> = emptyList(),
)
