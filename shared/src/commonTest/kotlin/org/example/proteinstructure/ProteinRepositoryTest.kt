package org.example.proteinstructure

import kotlinx.coroutines.runBlocking
import org.example.proteinstructure.data.ProteinRepositoryImpl
import org.example.proteinstructure.data.mockProteins
import org.example.proteinstructure.ui.navigation.Navigator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ProteinRepositoryTest {

    private val repository = ProteinRepositoryImpl()

    @Test
    fun mockProteinsCountIsAtLeastTwenty() {
        assertTrue(mockProteins.size >= 20, "Требуется не менее 20 записей моков, сейчас: ${mockProteins.size}")
    }

    @Test
    fun mockProteinsHaveValidFields() {
        for (protein in mockProteins) {
            assertTrue(protein.id.isNotBlank(), "PDB ID не должен быть пустым")
            assertTrue(protein.shortName.isNotBlank(), "Имя белка не должно быть пустым")
            assertTrue(protein.organism.isNotBlank(), "Организм не должен быть пустым")
            assertTrue(protein.monomerCount > 0, "Количество остатков должно быть положительным для ${protein.id}")
            assertTrue(protein.molecularWeightKDa > 0.0, "Молекулярная масса должна быть положительной для ${protein.id}")
        }
    }

    @Test
    fun searchFilteringWorks() {
        runBlocking {
            val hemoglobinHits = repository.getProteins("hemoglobin")
            assertTrue(hemoglobinHits.isNotEmpty(), "Поиск гемоглобина должен вернуть результаты")
            assertTrue(hemoglobinHits.any { it.id == "4HHB" }, "Должен быть найден 4HHB")

            val idSearch = repository.getProteins("6LU7")
            assertEquals(1, idSearch.size)
            assertEquals("6LU7", idSearch.first().id)
        }
    }

    @Test
    fun getProteinByIdWorks() {
        runBlocking {
            val protein = repository.getProtein("4HHB")
            assertEquals("4HHB", protein.id)
            assertNotNull(protein.title)
        }
    }

    @Test
    fun navigatorBackStackBehavior() {
        val navigator = Navigator()
        assertEquals(listOf(Screen.List), navigator.navStack.value)

        navigator.addToBackStack(Screen.Detail("4HHB"))
        assertEquals(listOf(Screen.List, Screen.Detail("4HHB")), navigator.navStack.value)

        navigator.addToBackStack(Screen.Detail("1HHO"))
        assertEquals(listOf(Screen.List, Screen.Detail("4HHB"), Screen.Detail("1HHO")), navigator.navStack.value)

        navigator.back()
        assertEquals(listOf(Screen.List, Screen.Detail("4HHB")), navigator.navStack.value)

        navigator.back()
        assertEquals(listOf(Screen.List), navigator.navStack.value)

        navigator.back()
        assertEquals(listOf(Screen.List), navigator.navStack.value)
    }
}
