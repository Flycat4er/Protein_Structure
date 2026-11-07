package org.example.proteinstructure.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.proteinstructure.Screen
import org.example.proteinstructure.data.ProteinRepositoryImpl
import org.example.proteinstructure.domain.ProteinRepository
import org.example.proteinstructure.ui.model.ProteinDetailUi
import org.example.proteinstructure.ui.model.toDetailUi
import org.example.proteinstructure.ui.navigation.Navigator

class ProteinDetailViewModel(
    private val proteinId: String,
    private val navigator: Navigator,
    private val repository: ProteinRepository = ProteinRepositoryImpl(),
) : ViewModel() {

    private val _state = MutableStateFlow<ProteinDetailUi?>(null)
    val state: StateFlow<ProteinDetailUi?> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val protein = repository.getProtein(proteinId)
            val related = repository.getRelatedProteins(proteinId)
            _state.value = protein.toDetailUi(related)
        }
    }

    fun onIntent(intent: ProteinDetailIntent) {
        when (intent) {
            is ProteinDetailIntent.RelatedProteinClicked -> navigator.addToBackStack(Screen.Detail(intent.id))
        }
    }
}
