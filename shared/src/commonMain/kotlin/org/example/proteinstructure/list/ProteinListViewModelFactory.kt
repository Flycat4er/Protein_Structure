package org.example.proteinstructure.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import kotlin.reflect.KClass
import org.example.proteinstructure.domain.ProteinRepository
import org.example.proteinstructure.ui.navigation.Navigator

class ProteinListViewModelFactory(
    private val navigator: Navigator,
    private val repository: ProteinRepository,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
        @Suppress("UNCHECKED_CAST")
        return ProteinListViewModel(navigator = navigator, repository = repository) as T
    }
}
