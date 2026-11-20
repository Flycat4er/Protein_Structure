package org.example.proteinstructure.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.example.proteinstructure.Screen
import org.example.proteinstructure.data.ProteinRepositoryImpl
import org.example.proteinstructure.domain.ProteinRepository
import org.example.proteinstructure.ui.model.toCardsUi
import org.example.proteinstructure.ui.navigation.Navigator

class ProteinListViewModel(
    private val navigator: Navigator,
    private val repository: ProteinRepository = ProteinRepositoryImpl(),
) : ViewModel() {

    private val _state = MutableStateFlow(ProteinListState(isLoading = true))
    val state: StateFlow<ProteinListState> = _state.asStateFlow()

    private var searchJob: Job? = null
    private var loadMoreJob: Job? = null
    private var currentPage = 0
    private val pageSize = 20

    init {
        search(filter = null)
    }

    private fun search(filter: String?) {
        searchJob?.cancel()
        loadMoreJob?.cancel()
        currentPage = 0
        searchJob = viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    isLoadingMore = false,
                    errorMessage = null,
                    canLoadMore = true,
                )
            }
            try {
                val proteins = repository.getProteins(filter, page = 0, pageSize = pageSize)
                // Разбор и склейка выполняются вне UI-диспетчера
                val cards = withContext(Dispatchers.Default) {
                    proteins.toCardsUi()
                }
                _state.update {
                    it.copy(
                        items = cards,
                        isLoading = false,
                        errorMessage = null,
                        canLoadMore = proteins.isNotEmpty(),
                    )
                }
            } catch (e: CancellationException) {
                // CancellationException НЕ перехватываем как ошибку (отмена при новом вводе)
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to load proteins",
                    )
                }
            }
        }
    }

    private fun loadMore() {
        val currentState = _state.value
        if (currentState.isLoading || currentState.isLoadingMore || !currentState.canLoadMore) {
            return
        }

        loadMoreJob?.cancel()
        loadMoreJob = viewModelScope.launch {
            _state.update { it.copy(isLoadingMore = true) }
            val nextPage = currentPage + 1
            try {
                val newProteins = repository.getProteins(currentState.query, page = nextPage, pageSize = pageSize)

                // ВАЖНО: Склейка страниц уходит с UI-диспетчера на Dispatchers.Default
                val updatedItems = withContext(Dispatchers.Default) {
                    val newCards = newProteins.toCardsUi()
                    val existingIds = currentState.items.map { it.id }.toSet()
                    currentState.items + newCards.filter { it.id !in existingIds }
                }

                currentPage = nextPage
                _state.update {
                    it.copy(
                        items = updatedItems,
                        isLoadingMore = false,
                        canLoadMore = newProteins.isNotEmpty(),
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(isLoadingMore = false)
                }
            }
        }
    }

    fun onIntent(intent: ProteinListIntent) {
        when (intent) {
            is ProteinListIntent.CardClicked -> navigator.addToBackStack(Screen.Detail(intent.id))
            is ProteinListIntent.QueryChanged -> {
                _state.update { it.copy(query = intent.value) }
                search(filter = intent.value)
            }
            is ProteinListIntent.LoadMore -> loadMore()
            is ProteinListIntent.Retry -> search(filter = _state.value.query)
        }
    }
}
