package org.example.proteinstructure.list

import androidx.compose.runtime.Immutable
import org.example.proteinstructure.ui.model.ProteinCardUi

@Immutable
data class ProteinListState(
    val query: String = "",
    val items: List<ProteinCardUi> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
    val canLoadMore: Boolean = true,
)
