package org.example.proteinstructure.detail

import androidx.compose.runtime.Immutable
import org.example.proteinstructure.ui.model.ProteinDetailUi

@Immutable
data class ProteinDetailState(
    val protein: ProteinDetailUi? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
