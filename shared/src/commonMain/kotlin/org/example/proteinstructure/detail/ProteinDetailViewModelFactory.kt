package org.example.proteinstructure.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import kotlin.reflect.KClass
import org.example.proteinstructure.domain.ProteinRepository
import org.example.proteinstructure.ui.navigation.Navigator

class ProteinDetailViewModelFactory(
    private val navigator: Navigator,
    private val repository: ProteinRepository,
) : ViewModelProvider.Factory {

    companion object {
        val PROTEIN_ID_KEY = object : CreationExtras.Key<String> {}

        fun extrasFor(id: String): CreationExtras =
            MutableCreationExtras().apply { set(PROTEIN_ID_KEY, id) }
    }

    override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
        val id = checkNotNull(extras[PROTEIN_ID_KEY]) { "Protein ID was not provided in CreationExtras" }
        @Suppress("UNCHECKED_CAST")
        return ProteinDetailViewModel(
            proteinId = id,
            navigator = navigator,
            repository = repository,
        ) as T
    }
}
