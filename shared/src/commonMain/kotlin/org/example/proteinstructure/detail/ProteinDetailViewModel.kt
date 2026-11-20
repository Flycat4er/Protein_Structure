package org.example.proteinstructure.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.proteinstructure.Screen
import org.example.proteinstructure.data.ProteinRepositoryImpl
import org.example.proteinstructure.domain.ProteinRepository
import org.example.proteinstructure.ui.model.toDetailUi
import org.example.proteinstructure.ui.navigation.Navigator

class ProteinDetailViewModel(
    private val proteinId: String,
    private val navigator: Navigator,
    private val repository: ProteinRepository = ProteinRepositoryImpl(),
) : ViewModel() {

    private val _state = MutableStateFlow(ProteinDetailState(isLoading = true))
    val state: StateFlow<ProteinDetailState> = _state.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadDetails()
    }

    private fun loadDetails() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val protein = repository.getProtein(proteinId)
                val related = repository.getRelatedProteins(proteinId)
                _state.update {
                    it.copy(
                        protein = protein.toDetailUi(related),
                        isLoading = false,
                        errorMessage = null,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to load protein details",
                    )
                }
            }
        }
    }

    fun onIntent(intent: ProteinDetailIntent) {
        when (intent) {
            is ProteinDetailIntent.RelatedProteinClicked -> navigator.addToBackStack(Screen.Detail(intent.id))
            is ProteinDetailIntent.Retry -> loadDetails()
        }
    }
}
