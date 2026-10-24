package org.example.proteinstructure.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.proteinstructure.Screen
import org.example.proteinstructure.data.ProteinRepositoryImpl
import org.example.proteinstructure.domain.ProteinRepository
import org.example.proteinstructure.ui.model.toCardsUi
import org.example.proteinstructure.ui.navigation.Navigator

class ProteinListViewModel(
    private val navigator: Navigator,
    private val repository: ProteinRepository = ProteinRepositoryImpl(),
) : ViewModel() {

    private val _state = MutableStateFlow(ProteinListState())
    val state: StateFlow<ProteinListState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        search(filter = null)
    }

    private fun search(filter: String?) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            val items = repository.getProteins(filter).toCardsUi()
            _state.update { it.copy(items = items) }
        }
    }

    fun onIntent(intent: ProteinListIntent) {
        when (intent) {
            is ProteinListIntent.CardClicked -> navigator.addToBackStack(Screen.Detail(intent.id))
            is ProteinListIntent.QueryChanged -> {
                _state.update { it.copy(query = intent.value) }
                search(filter = intent.value)
            }
        }
    }
}
